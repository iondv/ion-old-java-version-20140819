package ion.smev.client.repo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.lang3.StringUtils;


import ion.core.IAuthContext;
import ion.core.IClassMeta;
import ion.core.IItem;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.logging.IChangeLogger;
import ion.framework.dao.jdbc.IJdbcConnectionProvider;
import ion.framework.dao.jdbc.JdbcDataRepository;
import ion.util.sync.SyncUtils;

public class JdbcOfflineDataRepository extends JdbcDataRepository {
	
	public JdbcOfflineDataRepository(IJdbcConnectionProvider connectionProvider) {
		super(connectionProvider);
	}

	private IAuthContext authContext;
	
	public void setAuthContext(IAuthContext authContext) {
		this.authContext = authContext;
	}

	protected void setSmevData(IStructMeta cm, Map<String, Object> data) throws IonException {
		if (cm.checkAncestor("FederalService") != null){ 
			if (data.containsKey("pettOrg") && (data.get("pettOrg") != null))
				data.put("offlnDestOrg", data.get("pettOrg"));
			
			if (data.containsKey("rqstOrg") && (data.get("rqstOrg") != null))
				data.put("offlnSrcOrg", data.get("rqstOrg"));			
			
			if (data.containsKey("statusRqst") && (data.get("statusRqst") != null))
				data.put("offlnVirtStatus", "f:" + data.get("statusRqst"));
		}
		else if (cm.checkAncestor("RegionalService") != null) {
			if (data.containsKey("pettOrg") && (data.get("pettOrg") != null))
				data.put("offlnDestOrg", data.get("pettOrg"));

			if (data.containsKey("rqstOrg") && (data.get("rqstOrg") != null)){
				data.put("offlnSrcOrg", data.get("rqstOrg"));
			} /*else if (data.containsKey("rqstOrgCode")){
				try {
  				HashMap<String, Object> filter = new HashMap<String, Object>();
  				filter.put("provider", data.get("rqstOrgCode"));
  				Collection<IItem> orgs = GetList(new JdbcItem(null, filter, metaRepository.Get("egOrganization"), this));
  				for (IItem org: orgs)
  					data.put("offlnSrcOrg", org.getItemId());
				} catch (Exception e ){
					logger.Warning("Не удалось установить токен отправителя для объекта для кода " + data.get("rqstOrgCode"), e);
				}
			}*/

			if (data.containsKey("statusRqst") && (data.get("statusRqst") != null))
				data.put("offlnVirtStatus", "r:" + data.get("statusRqst"));			
			/*
			if (data.containsKey("requestStatus") && (data.get("requestStatus") != null))
				data.put("offlnVirtStatus", "r:" + data.get("requestStatus"));
			*/			
		}
		else if (cm.checkAncestor("Petition") != null) {
			if (data.containsKey("pettOrg") && (data.get("pettOrg") != null))
				data.put("offlnDestOrg", data.get("pettOrg"));
			else if (data.containsKey("recipientOrg") && (data.get("recipientOrg") != null))
				data.put("offlnDestOrg", data.get("recipientOrg"));
			
			if (data.containsKey("state") && (data.get("state") != null))
				data.put("offlnVirtStatus", "p:" + data.get("state"));						
		}
	}
	
	@Override
	public IItem CreateItem(String classname, Map<String, Object> data, IChangeLogger changeLogger) throws IonException {
		IStructMeta cm = metaRepository.Get(classname);
		if (cm != null){
			if (cm.PropertyMeta("timeStamp") != null && !data.containsKey("timeStamp") || data.get("timeStamp") == null)
				data.put("timeStamp", new Date());
			
			if (cm.PropertyMeta("createDate") != null && !data.containsKey("createDate") || data.get("createDate") == null)
				data.put("createDate", new Date());			
		}
		
		if (cm != null && authContext != null && authContext.CurrentUser() != null)
			if (cm.checkAncestor("SmevEntity") != null){
				if (cm.PropertyMeta("rqstOrg") != null 
						&& (!data.containsKey("rqstOrg") || data.get("rqstOrg") == null))
					data.put("rqstOrg", authContext.CurrentUser().getProperties().get("organisation"));
				
				data.put("offlnSrcOrg", authContext.CurrentUser().getProperties().get("organisation"));				
			}
		setSmevData(cm, data);
		return super.CreateItem(classname, data, changeLogger);
	}
	
	@Override
	protected boolean checkChange(IPropertyMeta pm, Object oldVal, Object newVal){
		return pm.Name().startsWith("offln") || super.checkChange(pm, oldVal, newVal);
	}	
	
	@Override
	public IItem EditItem(String classname, String id, Map<String, Object> data, IChangeLogger changeLogger) throws IonException {
		IStructMeta cm = metaRepository.Get(classname);
		if (cm != null)
			if (cm.PropertyMeta("timeStamp") != null && !data.containsKey("timeStamp"))
				data.put("timeStamp", new Date());

		setSmevData(cm, data);
		return super.EditItem(classname, id, data, changeLogger);
	}
	
	private long getCounterCount(IClassMeta cm, String whereclause, /*String[] subclasses,*/ ListOptions lo) throws IonException {
		String cndHook = whereclause;
		/*
		if (subclasses.length > 0){
			if (!cndHook.isEmpty())
					cndHook = cndHook + " and ";
			IClassMeta root = (IClassMeta)getRoot(cm);
			cndHook = SyncUtils.dbSanitiseName(root.getName(), tablePrefix) + "._type in ('"+StringUtils.join(subclasses, "','")+"')";	
		}
		*/
		return getItemsCount(GetItem(cm.getName()), lo, null, null, cndHook);
	}	
	
	private void fillCounters(Map<String, Long> result, String classname, /*String[] subclasses,*/ ListOptions lo) throws IonException{
		IClassMeta sm = (IClassMeta)metaRepository.Get(classname);
		String statusTable = SyncUtils.dbSanitiseName("SmevEntity", tablePrefix);
		String statusField = SyncUtils.dbSanitiseName("offlnVirtStatus", fieldPrefix);
		String keyField = SyncUtils.dbSanitiseName(sm.KeyProperties()[0], fieldPrefix);
		
		long cnt;
		cnt = getCounterCount(sm, "", /*subclasses,*/ lo);
		if (result.containsKey("all"))
			cnt = cnt + result.get("all");
		result.put("all", cnt);
		cnt = getCounterCount(sm, statusTable + "." + statusField + " in ('f:created1','r:received3','p:4')", /*subclasses,*/ lo);
		if (result.containsKey("anew"))
			cnt = cnt + result.get("anew");
		result.put("anew", cnt);
		cnt = getCounterCount(sm, statusTable + "." + statusField + " in ('f:expired7','r:expired7') or " + statusTable + ".f_overdue is not null", /*subclasses,*/ lo);
		if (result.containsKey("expired"))
			cnt = cnt + result.get("expired");
		result.put("expired", cnt);
		cnt = getCounterCount(sm, statusTable + "." + statusField + " in ('p:666','p:19')", /*subclasses,*/ lo);
		if (result.containsKey("sendErrors"))
			cnt = cnt + result.get("sendErrors");
		result.put("sendErrors", cnt);
		cnt = getCounterCount(sm, statusTable + "." + statusField + " in ('f:working','r:registered4','p:3')", /*subclasses,*/ lo);
		if (result.containsKey("moderate"))
			cnt = cnt + result.get("moderate");
		result.put("moderate", cnt);
		cnt = getCounterCount(sm, statusTable + "." + keyField + " in (select guid from not_read)", /*subclasses,*/ lo);
		if (result.containsKey("pending"))
			cnt = cnt + result.get("pending");
		result.put("pending", cnt);		
		cnt = getCounterCount(sm, statusTable + "." + statusField + " is null", /*subclasses,*/ lo);
		if (result.containsKey("drafts"))
			cnt = cnt + result.get("drafts");
		result.put("drafts", cnt);
	}
	
	public Map<String, Long> getGroupCounters(String classname, /*String[] subclasses,*/ ListOptions lo) throws IonException {
		Map<String, Long> result = new HashMap<String, Long>();
		if (metaRepository.Get(classname) != null){
  		if (metaRepository.Get(classname).PropertyMeta("offlnVirtStatus") != null)
  			fillCounters(result, classname, /*subclasses,*/ lo);
		}
		return result;
	}
}
