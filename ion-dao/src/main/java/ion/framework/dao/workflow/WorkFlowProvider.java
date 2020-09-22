package ion.framework.dao.workflow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import ion.core.Condition;
import ion.core.DACPermission;
import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IReferencePropertyMeta;
import ion.core.ISelectionProvider;
import ion.core.IStructMeta;
import ion.core.IWorkflowMeta;
import ion.core.IWorkflowMetaRepository;
import ion.core.IWorkflowProvider;
import ion.core.IWorkflowState;
import ion.core.IWorkflowStateMeta;
import ion.core.IWorkflowTransitionMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.meta.PropertyMeta;
import ion.core.workflow.WorkflowState;
import ion.core.workflow.WorkflowTransition;
import ion.framework.dao.DaoUtils;

public class WorkFlowProvider implements IWorkflowProvider {
	
	private DataSource dataSource;

	private IWorkflowMetaRepository metaRepository; 
	
	public void setMetaRepository(IWorkflowMetaRepository rep) {
		metaRepository = rep;
	}

	public void setDataSource(DataSource dataSource) throws IonException {
		this.dataSource = dataSource;
		try {
	    createTable();
    } catch (SQLException e) {
    	throw new IonException(e);
    }
	}
	
	private void createTable() throws SQLException{
		String createTableQuery = "CREATE TABLE IF NOT EXISTS `workflow_state` ("+
										  "`item` VARCHAR(200) NOT NULL, "+
										  "`workflow` VARCHAR(200) NOT NULL, "+
										  "`state` VARCHAR(200) NOT NULL, "+
										  "PRIMARY KEY (`item`, `workflow`)) "+
											"DEFAULT CHARSET = utf8;";
		Connection c = dataSource.getConnection();
		Statement statement = c.createStatement();
		statement.execute(createTableQuery);
    statement.close();
    c.close();
	}
	
	private String getCurrentState(String item, String workflow) throws SQLException{
		String currentState = null;
		Connection c = dataSource.getConnection();
		String selectQuery = "SELECT * FROM workflow_state WHERE item=? AND workflow=?;";
		PreparedStatement statement = c.prepareStatement(selectQuery);
		statement.setString(1, item);
		statement.setString(2, workflow);
		ResultSet rs = statement.executeQuery();
		if(rs.next()){
			currentState = rs.getString("state");
		}
		rs.close();
		statement.close();
		c.close();
		return currentState;
	}
	
	private void writeState(String item, String workflow, String state) throws SQLException{
		String currentState = getCurrentState(item,workflow);
		Connection c = dataSource.getConnection();
		String writeQuery = "";
		if(currentState!=null){
			writeQuery = "UPDATE workflow_state SET state=? WHERE item=? AND workflow=?;";
		}else{
			writeQuery = "INSERT INTO workflow_state (state,item,workflow) VALUES (?,?,?)";
		}
		PreparedStatement writeStatement = c.prepareStatement(writeQuery);
		writeStatement.setString(1, state);
		writeStatement.setString(2, item);
		writeStatement.setString(3, workflow);
		writeStatement.executeUpdate();
		writeStatement.close();
		c.close();
	}
	
	private void removeWf(String item, String workflow) throws SQLException{
		Connection c = dataSource.getConnection();
		String deleteQuery = "DELETE FROM workflow_state WHERE item=? AND workflow=?;";
		PreparedStatement statement = c.prepareStatement(deleteQuery);
		statement.setString(1, item);
		statement.setString(2, workflow);
		statement.executeUpdate();
		statement.close();
		c.close();
	}
	
	private IWorkflowMeta getWorkflowModel(IItem item, String name) throws IonException {
		IWorkflowMeta m = null;
		
		m = metaRepository.getGlobalWorkflow(name);
		if (m == null){
  		IStructMeta sm = item.getMetaClass();
  		while (sm != null){
  			m = metaRepository.getWorkflow(sm.getName(), name);
  			sm = sm.getAncestor();
  			if (m != null)
  				break;
  		}
		}
		return m;
	}

	private List<String> mergeCurrentItemWorkflows(IItem item,
	                                  Map<String, Collection<WorkflowTransition>> nextTransitions,
	                                  Map<String, Integer> itemPermissions,
	                                  Map<String, Map<String, Integer>> propertyPermissions,
	                                  Map<String, Map<String, ISelectionProvider>> selectionProviders) throws SQLException, IonException {
		List<String> result = new ArrayList<String>();
		Connection c = dataSource.getConnection();
		String selectQuery = "SELECT * FROM workflow_state WHERE item=?;";
		PreparedStatement statement = c.prepareStatement(selectQuery);
		statement.setString(1, item.getClassName()+"@"+item.getItemId());
		ResultSet rs = statement.executeQuery();
		while(rs.next()){
			IWorkflowMeta wf = getWorkflowModel(item, rs.getString("workflow"));
			mergeWorkflow(item, wf, rs.getString("state"), nextTransitions, itemPermissions, propertyPermissions, selectionProviders);
			result.add(wf.getName());
		}
		rs.close();
		statement.close();
		c.close();
		return result;
  }

	@Override
  public IWorkflowState ProcessTransition(IItem item, String workflowName, String transitionName) throws IonException {
		IWorkflowMeta workflow = getWorkflowModel(item, workflowName);
		IWorkflowTransitionMeta transition = workflow.getTransition(transitionName);
		if (transition != null) {
			IWorkflowStateMeta nextState = transition.getDestination();
			for (Map.Entry<String, Object> assignment : transition.getPropertyAssignments().entrySet()) {
				IProperty assignmentProperty = item.Property(assignment.getKey());
				item.Set(assignmentProperty.getName(), DaoUtils.cast(assignment.getValue().toString(), assignmentProperty.getType()));
			}
			if (tryConditions(item, nextState.getConditions())) {
				try {
					if (!nextState.getNextTransitions().isEmpty()) {
						writeState(item.getClassName()+"@"+item.getItemId(), workflow.getName(), nextState.getName());
					} else {
						removeWf(item.getClassName()+"@"+item.getItemId(),workflow.getName());
					}
		      return GetState(item);
	      } catch (SQLException e) {
		      throw new IonException(e.getMessage());
	      }
			}else{
				throw new IonException("не очень!");
			}
		}else{
			throw new IonException("неправильный transition!");
		}
  } 
	
	private void mergeWorkflow(IItem item, IWorkflowMeta currentWorkflow,
	                           String currentStateName,
                             Map<String, Collection<WorkflowTransition>> nextTransitions,
                             Map<String, Integer> itemPermissions,
                             Map<String, Map<String, Integer>> propertyPermissions,
                             Map<String, Map<String, ISelectionProvider>> selectionProviders) throws IonException {
		IWorkflowStateMeta currentState = null;
		if (currentStateName == null) {
			IWorkflowStateMeta state = currentWorkflow.getStartState();
			if (tryConditions(item, state.getConditions())) {
				currentStateName = state.getName();
				currentState = state;
			}
		}
		
		if (currentStateName != null) {
			if (currentState == null) 
				currentState = currentWorkflow.getState(currentStateName);
			for (IWorkflowTransitionMeta t : currentState.getNextTransitions()) {
				if (tryConditions(item,t.getConditions())) {
						WorkflowTransition wt = new WorkflowTransition(currentWorkflow.getName()+ "." + t.getName(), t.getCaption(), 
						                                               t.getSignBefore(), t.getSignAfter(), 
						                                               t.getPropertyAssignments());
						if (t.getPermittedRoles() != null && !t.getPermittedRoles().isEmpty()) {
							for(String role : t.getPermittedRoles()){
								DaoUtils.merge(nextTransitions, role, wt);
							}
						} else {
							DaoUtils.merge(nextTransitions, WorkflowState.EVERYBODY, wt);
						}
				}
			}
			
			for (Map.Entry<String, Set<DACPermission>> p: currentState.getItemPermissions().entrySet()){
				int permMask = 0;
				for (DACPermission perm: p.getValue())
					permMask = permMask | perm.getValue();
				if (itemPermissions.containsKey(p.getKey()))
					permMask = permMask | itemPermissions.get(p.getKey());
				
				itemPermissions.put(p.getKey(), permMask);
			}
			
			for (Map.Entry<String, Map<String, Set<DACPermission>>> lvl1: currentState.getPropertyPermissions().entrySet()){
				for (Map.Entry<String, Set<DACPermission>> lvl2: lvl1.getValue().entrySet()){
					int permMask = 0;
					for (DACPermission perm: lvl2.getValue())
						permMask = permMask | perm.getValue();
					
					if (!propertyPermissions.containsKey(lvl2.getKey()))
						propertyPermissions.put(lvl2.getKey(), new HashMap<String, Integer>());
					
					Map<String, Integer> perms = propertyPermissions.get(lvl2.getKey());
					if (perms.containsKey(lvl1.getKey()))
						permMask = permMask | perms.get(lvl1.getKey());
					
					perms.put(lvl1.getKey(), permMask);
				}
			}
			
			for (Map.Entry<String, Map<String,ISelectionProvider>> lvl1: currentState.getSelectionProviders().entrySet()){
				for (Map.Entry<String,ISelectionProvider> lvl2: lvl1.getValue().entrySet()){
					if (!selectionProviders.containsKey(lvl2.getKey()))
						selectionProviders.put(lvl2.getKey(), new HashMap<String, ISelectionProvider>());
					selectionProviders.get(lvl2.getKey()).put(lvl1.getKey(), lvl2.getValue());
				}
			}
		}
  }

	private boolean tryConditions(IItem item, Collection<Condition> conditions) throws IonException {
	  for(Condition sc : conditions){
	  	if(!tryCondition(item,sc)) return false;
	  }
	  return true;
  }

	private boolean tryCondition(IItem item, Condition sc) throws IonException {
	  IProperty property = item.Property(sc.Property());
	  Object currentValue = property.getValue();
	  switch(sc.Type()){
	  	case EMPTY: 
	  		return currentValue == null;
	  	case EQUAL:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) == 0;
	  	case IN: 
	  		Set<String> set = new HashSet<String>();
	  		for(String s : sc.Value().toString().split(",")){
	  			set.add(s.trim());
	  		}
	  		return set.contains(currentValue.toString());
	  	case LESS:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) < 0;
	  	case LESS_OR_EQUAL:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) <= 0;
	  	case LIKE: 
	  		//заглушка, есть предложение убрать эту опцию из спеки
	  		return false;
	  	case MORE:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) > 0;
	  	case MORE_OR_EQUAL:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) >= 0;
	  	case NOT_EMPTY:
	  		return currentValue != null;
	  	case NOT_EQUAL:
	  		return compareObject(currentValue, sc.Value(), property.Meta()) != 0;
	  	case CONTAINS:return false;
	  }
	  return false;
  }

	private Integer compareObject(Object o, Object s, IPropertyMeta p) throws IonException{
		switch(p.Type()){
	  	case BOOLEAN: 
	  		return ((Boolean) o).compareTo(((Boolean) DaoUtils.cast(s.toString(), p.Type())));
			case DATETIME: 
				return ((Date) o).compareTo(((Date) DaoUtils.cast(s.toString(), p.Type())));
			case DECIMAL: 
				return ((BigDecimal) o).compareTo(((BigDecimal) DaoUtils.cast(s.toString(), p.Type())));
			case INT:
				if (p.Size() == null || p.Size() > 11) {
					return ((Long) o).compareTo(((Long) DaoUtils.cast(s.toString(), p.Type())));
				} else {
					return ((Integer) o).compareTo(((Integer) DaoUtils.cast(s.toString(), p.Type())));
				}
			case REAL:
				return ((Float) o).compareTo(((Float) DaoUtils.cast(s.toString(), p.Type())));
			case SET:
				return ((Short) o).compareTo(((Short) DaoUtils.cast(s.toString(), p.Type())));
			case TEXT:
			case FILE:
			case GUID:
			case HTML:
			case IMAGE:
			case PASSWORD:
			case STRUCT:
			case URL:
			case STRING:
				return ((String) o).compareTo(((String) DaoUtils.cast(s.toString(), p.Type())));
			case REFERENCE:
				IClassMeta cm = ((IReferencePropertyMeta) p).ReferencedClass();
				MetaPropertyType refPropType = cm.PropertyMeta(cm.KeyProperties()[0]).Type();
				Short refPropSize = cm.PropertyMeta(cm.KeyProperties()[0]).Size();
				return compareObject(o,s,new PropertyMeta(p.Name(), p.Caption(), refPropType, refPropSize));
			case COLLECTION:
				String backRef = ((ICollectionPropertyMeta) p).BackReference();
				IStructMeta colMeta = ((ICollectionPropertyMeta) p).ItemsClass();
				return compareObject(o,s,colMeta.PropertyMeta(backRef));
	  	default: return 0;
		}
	}

	@Override
  public IWorkflowState GetState(IItem item) throws IonException {
		IStructMeta itemMeta = item.getMetaClass();
		
		Map<String, Collection<WorkflowTransition>> nextTransitions = new HashMap<String, Collection<WorkflowTransition>>();
		Map<String, Integer> itemPermissions = new HashMap<String, Integer>();
		Map<String, Map<String, Integer>> propertyPermissions = new HashMap<String, Map<String,Integer>>();
		Map<String, Map<String, ISelectionProvider>> selectionProviders = new HashMap<String, Map<String,ISelectionProvider>>();
		
		try {
			
			boolean stateExists = false;
			
			List<String> currentWorkflows = mergeCurrentItemWorkflows(item, nextTransitions, itemPermissions, propertyPermissions, selectionProviders);
			
			if (currentWorkflows.size() > 0)
				stateExists = true;
			
			IStructMeta currentMeta = itemMeta;
  		while (true){
  			Collection<IWorkflowMeta> metas = null;
  			if (currentMeta == null){
  				metas = metaRepository.getGlobalWorkflows();
  			} else
  				metas = metaRepository.getClassWorkflows(currentMeta.getName());
  			if (metas.size() > 0)
  				stateExists = true;
  			for (IWorkflowMeta m: metas)
  				if (!currentWorkflows.contains(m.getName()))
  					mergeWorkflow(item,m,null,nextTransitions,itemPermissions,propertyPermissions,selectionProviders);
  			if (currentMeta != null)
  				currentMeta = currentMeta.getAncestor();
  			else
  				break;
  		}
		  
  		if (stateExists){
  			Map<String, WorkflowTransition[]> transitions = new HashMap<String, WorkflowTransition[]>();
  			for(Entry<String, Collection<WorkflowTransition>> e : nextTransitions.entrySet()){
  				transitions.put(e.getKey(), e.getValue().toArray(new WorkflowTransition[e.getValue().size()]));
  			}
		  
  			return new WorkflowState(item, transitions, itemPermissions, propertyPermissions, selectionProviders);
  		}
  		return null;
		} catch (SQLException e1) {
			throw new IonException(e1.getMessage());
    }
  }

	@Override
	public boolean HasWorkflows(IStructMeta classMeta) throws IonException {
		IStructMeta currentMeta = classMeta;
		Collection<IWorkflowMeta> metas = metaRepository.getGlobalWorkflows();
		if (!metas.isEmpty())
			return true;
		
		while (currentMeta != null){
			metas = metaRepository.getClassWorkflows(currentMeta.getName());
			if (!metas.isEmpty())
				return true;
			currentMeta = currentMeta.getAncestor();
		}
		return false;
	}

}
