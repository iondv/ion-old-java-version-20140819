package ion.web.app;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.IClassMeta;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.Operation;
import ion.core.OperationType;
import ion.core.Sorting;
import ion.core.SortingMode;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.IListColumn;
import ion.viewmodel.view.IListViewModel;
import ion.viewmodel.view.ListAction;
import ion.viewmodel.view.ListColumn;
import ion.viewmodel.view.ListViewModel;
import ion.viewmodel.view.ViewApplyMode;
import ion.web.app.BasicController;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class AbstractListController extends BasicController {
	
	@Value("${lists.maxPageSize}")	
	private Integer maxPageSize;
	
	protected IFormViewModel getDetailModel(IListViewModel listViewModel, IStructMeta cm) throws IonException {
		boolean useEditModels = (listViewModel.getUseEditModels() == null) || listViewModel.getUseEditModels();
		IFormViewModel viewmodel = this.viewmodel.getDetailViewModel(cm);
		if (viewmodel == null && useEditModels) 
			viewmodel = this.viewmodel.getItemViewModel(cm);
		
		if (viewmodel == null)
			if (cm instanceof IClassMeta) 
				viewmodel = this.defaultViewModel((IClassMeta)cm);

		return viewmodel;
	}
	
	
	protected IListViewModel listSetup(IStructMeta cm, String node, PageContext context, Model model) throws IonException{
		IListViewModel vm = viewmodel.getListViewModel(node, cm);
		if (vm == null)
			vm = viewmodel.getListViewModel(cm);		
		
		if (vm == null){
			ArrayList<IListColumn> cols = new ArrayList<IListColumn>();
			Map<String, ListAction> actions = new LinkedHashMap<String, ListAction>();
			createColsAndFields(cols, actions, cm.PropertyMetas().values());
			vm = new ListViewModel(cols, maxPageSize, false, actions, null, true);
		} else {
			if(vm.getMode() == ViewApplyMode.OVERRIDE){
				ArrayList<IListColumn> colsToOverride = new ArrayList<IListColumn>();
				Map<String, ListAction> actionsToOverride = new LinkedHashMap<String, ListAction>();
				createColsAndFields(colsToOverride, actionsToOverride, cm.PropertyMetas().values());
				
				//overriding Columns
				List<IListColumn> resultCols = new ArrayList<IListColumn>();
				colsToOverride.removeAll(vm.getColumns());
				resultCols.addAll(colsToOverride);
				resultCols.addAll(vm.getColumns());
				
				//Collections.sort(resultCols);
				
				//overriding Actions
				actionsToOverride.putAll(vm.getActions());
				vm = new ListViewModel(resultCols, vm.getPageSize(), vm.getAllowSearch(), actionsToOverride, vm.getMode(), vm.getUseEditModels());
			}
		}
		
		
		model.addAttribute("properties",cm.PropertyMetas());
		model.addAttribute("viewmodel",vm);
		
		ArrayList<IStructMeta> classes = new ArrayList<IStructMeta>();
		if (data.CheckClassInstanciationAccess(cm.getName()))
			classes.add(cm);
		for (IStructMeta sc: cm.Descendants())
			if (data.CheckClassInstanciationAccess(sc.getName()))
				classes.addAll(cm.Descendants());
		
		model.addAttribute("classname",cm.getName());
		model.addAttribute("choose_cc",classes.size() > 1);
		model.addAttribute("creationclasses",classes);
		return vm;
	}
	
	protected void createColsAndFields(ArrayList<IListColumn> cols,
	                                   Map<String, ListAction> actions,
	                                   Collection<IPropertyMeta> properties) throws IonException {
		for (IPropertyMeta pm : properties) {
			String property = pm.Name();
			switch (pm.Type()) {
				case COLLECTION:
				case HTML:
				case TEXT:
				case IMAGE:
				case FILE:
					break;
				case STRUCT:{
					IStructMeta m = ((IStructPropertyMeta)pm).StructClass();
					while (m != null) {
						createColsAndFields(cols, null, m.PropertyMetas().values());						
						m = m.getAncestor();
					}
				}break;
				case INT:
					cols.add(new ListColumn(pm.Caption(), property, FieldType.NUMBER_PICKER, true, pm.OrderNumber(), pm.ReadOnly()));
					break;
				case DECIMAL:
				case REAL:
					cols.add(new ListColumn(pm.Caption(), property, FieldType.DECIMAL_EDITOR, true, pm.OrderNumber(), pm.ReadOnly()));
					break;
				case DATETIME:
					cols.add(new ListColumn(pm.Caption(), property, FieldType.DATETIME_PICKER, true, pm.OrderNumber(), pm.ReadOnly()));
					break;
				case REFERENCE:
					cols.add(new ListColumn(pm.Caption(), property, FieldType.REFERENCE, true, pm.OrderNumber(), pm.ReadOnly()));
					break;
				case BOOLEAN:
					cols.add(new ListColumn(pm.Caption(), property, FieldType.CHECKBOX, true, pm.OrderNumber(), pm.ReadOnly()));					
					break;
				default:
					cols.add(new ListColumn(pm.Caption(), property, true, pm.OrderNumber(), pm.ReadOnly()));
					break;
			}
		}

		// Collections.sort(cols);
		if (actions != null){
			actions.put(ActionType.CREATE.name(), new ListAction(ActionType.CREATE.name(), ActionType.CREATE.getCaption()));
			actions.put(ActionType.EDIT.name(), new ListAction(ActionType.EDIT.name(), ActionType.EDIT.getCaption(), true, false));
			actions.put(ActionType.DELETE.name(), new ListAction(ActionType.DELETE.name(), ActionType.DELETE.getCaption(), false, true));
		}
	}
	
	
	private Object parseListFilterOption(ListFilterOption o){
		if (o.type == null)
			return o.value;
		
		if (o.property == null){
			Collection<FilterOption> ops = new LinkedList<FilterOption>();
			for (ListFilterOption lfo: o.operands)
				ops.add((FilterOption)parseListFilterOption(lfo));
			return new Operation(OperationType.valueOf(o.type),ops.toArray(new FilterOption[ops.size()]));
		} else {
			if (o.valueConditions != null){
				List<Condition> vc = new LinkedList<Condition>();
				for (ListFilterOption lfo: o.valueConditions)
					vc.add((Condition)parseListFilterOption(lfo));
				return new Condition(o.property, ConditionType.valueOf(o.type), o.value, vc);
			} else
				return new Condition(o.property, ConditionType.valueOf(o.type), o.value);
		}
	}
	
	protected ListOptions prepareOptions(IStructMeta cm, PageContext context, IListViewModel vm, Integer page, String filter, String sorting){
		ListOptions lo = context.ListOptions;
		if (lo == null)
			lo = new ListOptions();
				
		if (page != null){
			lo.SetPage(page);
			if (vm == null)
				vm = viewmodel.getListViewModel(context.getNode().getId(), cm);

			if (vm != null && vm.getPageSize() != null)
				lo.SetPageSize(vm.getPageSize());
			else
				lo.SetPageSize(maxPageSize);
		}
		
		lo.SetTotalCount((lo.PageSize() != null)?null:(long)0);
		
		Gson gs = null;
		
		if (filter != null){
			filter = new String(Base64.getDecoder().decode(filter),Charset.forName("UTF-8"));
			gs = new GsonBuilder().serializeNulls().create();
			Type t = new TypeToken<Collection<ListFilterOption>>(){}.getType();
			Collection<ListFilterOption> fo = gs.fromJson(filter, t);
			if (fo.size() > 0){
				Collection<FilterOption> fo2 = new LinkedList<FilterOption>();
				for (ListFilterOption lfo: fo)
					fo2.add((FilterOption)parseListFilterOption(lfo));
				lo.Filter().addAll(fo2);
			}
		}
		
		if (sorting != null){
			sorting = new String(Base64.getDecoder().decode(sorting),Charset.forName("UTF-8"));
			if (gs == null)
				gs = new GsonBuilder().serializeNulls().create();
			
			Type t = new TypeToken<Collection<ListSortingOption>>(){}.getType();
			Collection<ListSortingOption> so = gs.fromJson(sorting, t);
			if (so.size() > 0){
				Collection<Sorting> so2 = new LinkedList<Sorting>();
				for (ListSortingOption lso: so)
					so2.add(new Sorting(lso.property, lso.desc?SortingMode.DESC:SortingMode.ASC));
				lo.Sorting().addAll(so2);
			}			
		}
		
		return lo;
	}
	
	
	protected void getList(IStructMeta cm, ListOptions lo, Model model) throws IonException {
		Collection<IItem> itemlist = data.List(cm.getName(),lo);
		model.addAttribute("list", itemlist);		
		model.addAttribute("curPage",lo.Page());
		model.addAttribute("pageCount",1);
		if (lo.TotalCount() > 0 && lo.PageSize() > 0){
			model.addAttribute("pageCount",(long)Math.ceil(((double)lo.TotalCount())/lo.PageSize()));
		}
	}
	
	public String list(String node, Integer page, HttpServletRequest request, Model model) throws Exception {
		PageContext context = new PageContext(node, meta, navmodel, urlfactory,  data);
		model.addAttribute("context",context);
		model.addAttribute("itemsClass",context.Class);
		model.addAttribute("Title",context.getNode().getCaption());
		model.addAttribute("breadcrumbs",context.BreadCrumbs(data));
		getList(context.Class, prepareOptions(context.Class, context, 
		         listSetup(context.Class, node, context, model), (page == null)?1:page, 
		         						request.getParameter("__filter"), request.getParameter("__sorting")), 
		         model);
		return ThemeDir()+"list";
	}
	
	public JSONResponse ajaxList(String node, 
												String className,
												Integer page,
												String options, 
												String sorting) throws IonException {
		try {
			PageContext context  = new PageContext(node, meta, navmodel, urlfactory, data);
			IStructMeta cm = meta.Get(className);
			return getJsonList(prepareOptions(cm, context, viewmodel.getListViewModel(node, cm), (page == null)?1:page, options, sorting), className);
		} catch (Exception e){
			return new JSONResponse(e.getMessage());
		}
	}
	
	
	public String collection(String node, String id,String collection, Integer page, HttpServletRequest request, Model model) throws Exception{
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
		model.addAttribute("context",context);
		model.addAttribute("Title",context.getNode().getCaption());
		model.addAttribute("breadcrumbs",context.BreadCrumbs(data));
		IListViewModel vm = listSetup(context.CollectionProperty.ItemsClass(),node,context,model);

		IItem container = data.getItem(context.Class.getName(), context.Id);
		
		ListOptions lo = prepareOptions(context.CollectionProperty.ItemsClass(), context, vm, (page == null)?1:page, 
		                                request.getParameter("__filter"), request.getParameter("__sorting"));
		
		Collection<IItem> itemlist = data.GetCollection(container, context.CollectionProperty.Name(),lo);

		model.addAttribute("list", itemlist);		
		model.addAttribute("curPage",lo.Page());
		model.addAttribute("pageCount",1);
		if (lo.TotalCount() > 0 && lo.PageSize() > 0){
			model.addAttribute("pageCount",Math.ceil(((double)lo.TotalCount())/lo.PageSize()));
		}
		
		return ThemeDir()+"list";
	}
	
	
	public JSONResponse ajaxCollection(String node, String id, String collection,
										Integer page, String options, 
										String sorting) throws Exception{
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
		try{
			return getJsonList(context.ListOptions, context.CollectionProperty.ItemsClass().getName());
		} catch (Exception e){
			return new JSONResponse(e.getMessage());
		}

	}
	
	@SuppressWarnings("serial")
	public JSONResponse delete(String node, String[] ids, Model model) throws IonException, IOException{
		try {
			final Collection<String[]> result = new ArrayList<String[]>(); 
			for (String id: ids){
				PageContext context = new PageContext(node, URLDecoder.decode(id,"UTF-8"), meta, navmodel, urlfactory, data);
				String[] itemId = context.Id.split("@");
				data.Delete(context.Class.getName(), itemId[1]);
				result.add(new String[]{context.Class.getName(), context.Id});
			}
			return new JSONResponse(new HashMap<String, Object>(){{ put("ids",result); }});
		} catch (Exception e){
			e.printStackTrace();
			return new JSONResponse(e.getMessage());
		}
	}	
	
	@SuppressWarnings("serial")
	public JSONResponse delete(String node, String id, Model model) throws IonException, IOException{
		PageContext context = new PageContext(node, id, meta, navmodel, urlfactory, data);
		final String delid = context.Id;
		try {
			data.Delete(context.Class.getName(), id);
			return new JSONResponse(new HashMap<String, Object>(){{ put("id",delid); }});
		} catch (Exception e){
			return new JSONResponse(e.getMessage());
		}
	}

/*	
	private ConditionType getConditionType(String condition){
		if(condition == "=" || condition.toUpperCase() == "EQUAL") return ConditionType.EQUAL;
		if(condition == "!=" || condition.toUpperCase() == "NOT_EQUAL") return ConditionType.NOT_EQUAL;
		if(condition == "=null" || condition.toUpperCase() == "EMPTY") return ConditionType.EMPTY;
		if(condition == "!=null" || condition.toUpperCase() == "NOT_EMPTY") return ConditionType.NOT_EMPTY;
		if(condition.toUpperCase() == "LIKE") return ConditionType.LIKE;
		if(condition == "<" || condition.toUpperCase() == "LESS") return ConditionType.LESS;
		if(condition == ">" || condition.toUpperCase() == "MORE") return ConditionType.MORE;
		if(condition == "<=" || condition.toUpperCase() == "LESS_OR_EQUAL") return ConditionType.LESS_OR_EQUAL;
		if(condition == ">=" || condition.toUpperCase() == "MORE_OR_EQUAL") return ConditionType.MORE_OR_EQUAL;
		if(condition.toUpperCase() == "IN") return ConditionType.IN;
		return ConditionType.EQUAL;
	}
*/	

	@SuppressWarnings("serial")
	private JSONResponse getJsonList(ListOptions lo, String className) throws IonException{
		Collection<IItem> list = data.List(className,lo);
		final Collection<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for(IItem i : list){
			result.add(data.ItemToMap(i));
		}
		return new JSONResponse(new HashMap<String, Object>(){{ put("list",result); }});
	}

	public void excel(String filename, Model model, HttpServletResponse response) throws IOException{
		Map<String, Object> modelAttributes = model.asMap();
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
		
    XSSFWorkbook workbook = new XSSFWorkbook();
    String sheetTitle = "page " + ((Integer)modelAttributes.get("curPage")).toString() + " from " + ((Double)modelAttributes.get("pageCount")).intValue();
    XSSFSheet sheet = workbook.createSheet(sheetTitle); 
    
    XSSFCellStyle head_style = workbook.createCellStyle();
    head_style.setBorderBottom(BorderStyle.MEDIUM);
    head_style.setBorderLeft(BorderStyle.MEDIUM);
    XSSFCellStyle head_end_style = workbook.createCellStyle();
    head_end_style.setBorderBottom(BorderStyle.MEDIUM);
    XSSFCellStyle cell_style = workbook.createCellStyle();
    cell_style.setBorderLeft(BorderStyle.MEDIUM);
    XSSFFont bold_font = workbook.createFont();
    bold_font.setBold(true);
    head_style.setFont(bold_font);
    head_end_style.setFont(bold_font);
    
    Collection<IListColumn> columns = ((IListViewModel)modelAttributes.get("viewmodel")).getColumns();
    @SuppressWarnings("unchecked")
		Collection<IItem> items = (Collection<IItem>)modelAttributes.get("list");
    short rowCounter = 0;
    int cellCounter = 0;

    XSSFRow head = sheet.createRow(rowCounter);
    
    for(IListColumn column : columns){
    	XSSFCell head_cell = head.createCell(cellCounter);
    	head_cell.setCellValue(column.getCaption());
    	if(cellCounter < columns.size())
    		head_cell.setCellStyle(head_style);
    	else
    		head_cell.setCellStyle(head_end_style);    	
    	cellCounter++;
    }
    cellCounter = 0;
    rowCounter++;

    for(IItem item: items){
      XSSFRow row = sheet.createRow(rowCounter);
      for(IListColumn column : columns){
      	XSSFCell cell = row.createCell(cellCounter);
      	
      	Object value = null;
      	if (column.getProperty().equals("class"))
      		value = item.getMetaClass().getCaption();
      	else {
      		IProperty p = null;
					try {
						p = item.Property(column.getProperty());
					} catch (IonException e) {
					}
      		if (p != null)
      			value = p.getString();
      	}
      	
      	if(value == null)
      		value = "";
      	cell.setCellValue(value.toString());
      	if(cellCounter < columns.size())
      		cell.setCellStyle(cell_style);
      	if(rowCounter == items.size())
        	sheet.autoSizeColumn(cellCounter);
      	cellCounter++;
      }
      cellCounter = 0;
      rowCounter++;
    }
          
		workbook.write(response.getOutputStream());
		workbook.close();
		response.flushBuffer();
	}
}