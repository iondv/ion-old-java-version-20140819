package ion.modeler.wizards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.resources.IonModelResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonViewsResource;
import ion.viewmodel.view.FieldType;

public class NewJspWizard extends CreationWizard {
	
	public NewJspWizard(){
		super();
	    pageName = "New jsp";
	    pageTitle = "Генератор jsp страниц";
	    pageDescription = "Генератор jsp страниц";
	}

	protected void formCaptions(){
		captions.put("viewType", "Тип представления");
		captions.put("className", "Представление класса");
	}
	
	@SuppressWarnings("serial")
	protected void formSelections(){
		selections.put("viewType", new LinkedHashMap<String,String>(){{
			put("list","список объектов");
			put("form","форма создания/изменения");
		}});
		
		Composer c = new Composer(((IonModelResource)context).Source().getProject());
		
		try {
			Map<String, Object[]> classes = c.ClassMetas(true);
			Map<String, String> sl = new LinkedHashMap<String,String>();
			for (Object[] v: classes.values())
				sl.put(((StoredClassMeta)v[1]).name, ((StoredClassMeta)v[1]).caption);
			selections.put("className", sl);		
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Override
	protected void formModel() {
		model = new NewJspModel();
	}

	protected boolean doPerform(Composer c) throws IOException, CoreException {
		generateJsp(c);
		return true;
	}	

	private void generateJsp(Composer c) throws IOException, CoreException{
		String deployLocation = getDeployLocation(((IonModelResource)context).Source().getProject());
		String newJspLocation = getNewJspLocation(((IonModelResource)context).Source().getProject());
		StoredClassMeta meta = c.getClass(((NewJspModel)model).className);
		if(deployLocation != null && newJspLocation!= null){
			if(((NewJspModel)model).viewType.equals("list")) generateListJsp(meta,deployLocation,newJspLocation);
			if(((NewJspModel)model).viewType.equals("form")) generateFormJsp(meta,deployLocation,newJspLocation);
		}
	}

	private void generateListJsp(StoredClassMeta meta, String deployLocation, String newJspLocation) throws IOException{
		ArrayList<StoredPropertyMeta> cols = new ArrayList<StoredPropertyMeta>() ;
		ArrayList<StoredPropertyMeta> details = new ArrayList<StoredPropertyMeta>();
		for(StoredPropertyMeta property : meta.properties){
			switch (MetaPropertyType.fromInt(property.type)){
				case COLLECTION:
				case HTML:
				case TEXT:
				case IMAGE:
				case FILE:details.add(property);break;
				case STRUCT:{
						cols.add(property);
						details.add(property);					
				}break;
				case REFERENCE:{
					cols.add(property);
					details.add(property);
				}break;
				default:cols.add(property);break;
			}
		}
		File jspfile = new File(deployLocation+"/src/main/webapp/WEB-INF/views/ionweb-default/list.jsp");
		FileInputStream fis = new FileInputStream(jspfile);
		DataInputStream in = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		File newJspFile = new File(newJspLocation + ((NewJspModel)model).className + "_" + "list.jsp");
		if(!newJspFile.getParentFile().exists()) newJspFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(newJspFile);
        BufferedWriter out = new BufferedWriter(fw);
        
        String result = "";
        String line;
        while ((line = br.readLine()) != null) {
           result = result+line+"\n";
        }
        
		Pattern pat = Pattern.compile("(?s)<table>\n" + "(.*)</table>");
		Matcher matcher = pat.matcher(result);
		while(matcher.find()){
			result = matcher.replaceAll(prepareTable(cols));
		}
        
		Pattern detailsPat = Pattern.compile("(?s)<div id=\"details\" class=\"details\">(.*)\n" + "(.*)</div>");
		Matcher detailsMatcher = detailsPat.matcher(result);
		while(detailsMatcher.find()){
			result = detailsMatcher.replaceAll(prepareDetails(details));
		}
		
        out.write(result);
        br.close();
        in.close();
        fis.close();

        out.close();
        fw.close();
	}
	
	private void generateFormJsp(StoredClassMeta meta, String deployLocation, String newJspLocation) throws IOException{
		
		File jspfile = new File(deployLocation+"/src/main/webapp/WEB-INF/views/ionweb-default/form.jsp");
		FileInputStream fis = new FileInputStream(jspfile);
		DataInputStream in = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		File newJspFile = new File(newJspLocation + ((NewJspModel)model).className + "_" + "form.jsp");
		if(!newJspFile.getParentFile().exists()) newJspFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(newJspFile);
        BufferedWriter out = new BufferedWriter(fw);
        
        String result = "";
        String line;
        while ((line = br.readLine()) != null) {
           result = result+line+"\n";
        }
        
		Pattern pat = Pattern.compile("(?s)<c:forEach items=(.*)var=\"tab\">\n" + "(.*)</c:forEach>");
		Matcher matcher = pat.matcher(result);
		while(matcher.find()){
			result = matcher.replaceAll(prepareForm(meta.properties));
		}
 
        out.write(result);
        br.close();
        in.close();
        fis.close();

        out.close();
        fw.close();
	}

	private String prepareTable(ArrayList<StoredPropertyMeta> cols){
		String tableHeaders = "";
		String tableRows = "";
		for(StoredPropertyMeta property : cols){
			tableHeaders = tableHeaders+"<th class=\"overflowed-text\"><c:out value=\""+property.caption+"\" /></th>"+"\n";
			tableRows = tableRows+"<td>"+getListField(property)+"</td>"+"\n";
		}
		String table = "<table>"+"\n"+
						"<tr>"+"\n"+
						"<th><input type=\"checkbox\" class=\"select-all row-selector\" /></th>"+"\n"+
						tableHeaders+
						"</tr>"+"\n"+
						"<c:forEach items=\"\\${list}\" var=\"i\">"+"\n"+
						"<tr id=\"row_<c:out value=\"\\${func:rowItemId(i)}\"/>\""+" nav-id=\"\\${func:navItemId(i)}\">"+"\n"+
							"<td><input type=\"checkbox\" class=\"row-selector\" /></td>"+"\n"+	
							tableRows+
						"</tr>"+"\n"+
						"</c:forEach>"+"\n"+
						"</table>"+"\n";
		
		return table;
	}
	private String getListField(StoredPropertyMeta property){
		if(property.type == MetaPropertyType.TEXT.getValue() || property.type == MetaPropertyType.HTML.getValue()){
			return "<c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" />";
		}
		if(property.type == MetaPropertyType.URL.getValue()){
			return "<a href=\"\\${(func:property(i,'"+property.name+"')).string}\"><c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" /></a>";
		}
		if(property.type == MetaPropertyType.IMAGE.getValue()){
			return "<div class=\"image\">"
					+ "<a href=\"<c:out value=\"\\${func:fileUrl((func:property(i,'"+property.name+"')).value)}\" />\">"
					+ "<img src=\"<c:out value=\"\\${func:fileUrl((func:property(i,'"+property.name+"')).value)}\" />\" />"
					+ "</a>"
					+ "</div>";
		}
		if(property.type == MetaPropertyType.FILE.getValue()){
			return "<a href=\"\\${func:fileUrl((func:property(i,'"+property.name+"')).value)}\"><c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" /></a>";
		}
		if(property.type == MetaPropertyType.INT.getValue() || property.type == MetaPropertyType.REAL.getValue() || property.type == MetaPropertyType.DECIMAL.getValue()){
			return "<span class=\"to-right\"><c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" /></span>";
		}
		if(property.type == MetaPropertyType.DATETIME.getValue()){
			return "<span class=\"to-right\"><c:out value=\"\\${func:dateToStr((func:property(i,'"+property.name+"')).value)}\" /></span>";
		}
		if(property.type == MetaPropertyType.BOOLEAN.getValue()){
			return "<span class=\"to-right\"><c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" /></span>";
		}
		return "<c:out value=\"\\${(func:property(i,'"+property.name+"')).string}\" />";
	}
	
	private String prepareDetails(ArrayList<StoredPropertyMeta> details){
		String detailRows = "";
		for(StoredPropertyMeta detail : details){
			detailRows = detailRows + getDetailField(detail)+"\n";
		}
		String detailsDiv = "<div id=\"details\" class=\"details\">"+"\n"
				+ "<c:forEach items=\"\\${list}\" var=\"i\">"+"\n"
				+ "<div id=\"details_<c:out value=\"\\${func:rowItemId(i)}\"/>\" style=\"display:none;\">"+"\n"
				+detailRows
				+"</div>"+"\n"
				+ " </c:forEach>"+"\n"
				+ " </div>"+"\n";
		
		return detailsDiv;
	}
	
	private String getDetailField(StoredPropertyMeta detail){
		if(detail.type == MetaPropertyType.REFERENCE.getValue()){
			return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
		((detail.back_ref != null)?"<a href=\"<c:out value=\"\\${func:itemUrl(context,(func:property(i,'"+detail.name+"')).referedItem)}\"/>\"><c:out value=\"\\${(func:property(i,'"+detail.name+"')).string}\" /></a>":"");
		}
		if(detail.type == MetaPropertyType.COLLECTION.getValue()){
			return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
					"<a href=\"<c:out value=\"\\${func:collectionUrl(context,item,(func:property(i,'"+detail.name+"')).name)}\"/>\"><c:out value=\"\\${(func:property(i,'"+detail.name+"')).string}\" /> элементов</a>";
		}
		if(detail.type == MetaPropertyType.TEXT.getValue() || detail.type == MetaPropertyType.HTML.getValue()){
			return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
		"<div class=\"value\"><c:out value=\"\\${(func:property(i,'"+detail.name+"')).string}\" /></div>";
		}
		if(detail.type == MetaPropertyType.URL.getValue()){
			return "<a href=\"<c:url value=\"\\${(func:property(i,'"+detail.name+"')).string}\"/>\"><c:out value=\""+detail.caption+"\" /></a>";
		}
		if(detail.type == MetaPropertyType.FILE.getValue()){
			return "<a href=\"<c:out value=\"\\${func:fileUrl((func:property(i,'"+detail.name+"')).value)}\" />\"><c:out value=\""+detail.caption+"\" /></a>";
		}
		if(detail.type == MetaPropertyType.IMAGE.getValue()){
			return "<div class=\"image\">"+"\n"+
			"<a href=\"<c:out value=\"\\${func:fileUrl((func:property(i,'"+detail.name+"')).value)}\" />\">"+"\n"+
				"<img src=\"<c:out value=\"\\${func:fileUrl((func:property(i,'"+detail.name+"')).value)}\" />\" />"+"\n"+
			"</a>"+"\n"+
			"</div>";
		}
		if(detail.type == MetaPropertyType.BOOLEAN.getValue()){
			return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
					"<input type=\"checkbox\" readonly=\"readonly\" <c:if test=\"\\${(func:property(i,'"+detail.name+"')).value}\">checked=\"checked\"</c:if> />";
		}
		if(detail.type == MetaPropertyType.DATETIME.getValue()){
			return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
					"<input type=\"text\" readonly=\"readonly\" value=\"<c:out value=\"\\${func:dateToStr((func:property(i,'"+detail.name+"')).value)}\" />\" />";
		}
		return "<label class=\"overflowed-text\"><c:out value=\""+detail.caption+"\" /></label><b>:</b>"+"\n"+
				"<span class=\"value\"><c:out value=\"\\${(func:property(i,'"+detail.name+"')).string}\" /></span>";
	}
	
	private String prepareForm(Collection<StoredPropertyMeta> properties){
		String formFields = "";
		for(StoredPropertyMeta property : properties){
			formFields = formFields +
					"<div class=\"field\">"+"\n"+
					"<label class=\"overflowed-text\" for=\""+property.name.replace(".", "-")+"\">"+property.caption+"</label><b>:</b>"+"\n"+
					getFormField(property)+"\n"+
					"</div>"+"\n";
		}
		String tab = "<div class=\"tab\">"+"\n"
					+"<div class=\"full-view\">"+"\n"
					+formFields
					+"</div>\n</div>"+"\n";
		return tab;
	}
	
	private String getFormField(StoredPropertyMeta property){
		if(getFieldType(property) == FieldType.MULTILINE){
			return "<textarea id=\""+property.name.replace(".", "-")+"\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\"><c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" /></textarea>";
		}
		if(getFieldType(property) == FieldType.WYSIWYG){
			return "<textarea id=\""+property.name.replace(".", "-")+"\" class=\"wysiwyg\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\"><c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" /></textarea>";
		}
		if(getFieldType(property) == FieldType.IMAGE){
			return "<div class=\"image\"><a href=\"<c:out value=\"\\${func:fileUrl((func:property(item,'"+property.name+"')).value)}\" />\"><img src=\"<c:out value=\"\\${func:fileUrl((func:property(item,'"+property.name+"')).value)}\" />\" /></a></div>"+
					"<c:if test=\"\\${not (func:property(item,'"+property.name+"')).readOnly}\">"+
					"<input id=\""+property.name.replace(".", "-")+"\" type=\"file\" name=\""+property.caption+"\" class=\""+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" />\" />"+
					"</c:if>";
		}
		if(getFieldType(property) == FieldType.FILE){
			return "<c:if test=\"\\${(func:property(item,'"+property.name+"')).value != null}\">"+
					"<a href=\"\\${func:fileUrl((func:property(item,'"+property.name+"')).value)}\"><c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" /></a>"+
					"</c:if>"+					
					"<c:if test=\"\\${not (func:property(item,'"+property.name+"')).readOnly}\">"+
					"<input id=\""+property.name.replace(".", "-")+"\" type=\"file\" name=\""+property.caption+"\" class=\""+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" />\" />"+
					"</c:if>";
		}
		if(getFieldType(property) == FieldType.DATETIME_PICKER){
			return "<input id=\""+property.name.replace(".", "-")+"\" type=\"text\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\"datepicker "+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${func:dateToStr((func:property(item,'"+property.name+"')).value)}\" />\"  />";
		}
		if(getFieldType(property) == FieldType.CHECKBOX){
			return "<input id=\""+property.name.replace(".", "-")+"\" type=\"checkbox\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\" checked=\"<c:choose><c:when test=\"\\${(func:property(item,'"+property.name+"')).value}\">checked</c:when></c:choose>\" />";
		}
		if(getFieldType(property) == FieldType.PASSWORD){
			return "<input id=\""+property.name.replace(".", "-")+"\" type=\"password\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\" value=\"\" />";
		}
		if(getFieldType(property) == FieldType.NUMBER_PICKER){
			return "<input id=\""+property.name.replace(".", "-")+"\" type=\"number\" step=\"1\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" />\" />";
		}
		if(getFieldType(property) == FieldType.DECIMAL_EDITOR){
			return "<input id=\""+property.name.replace(".", "-")+"\" type=\"number\" step=\"0.1\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" />\" />	";
		}
		if(getFieldType(property) == FieldType.REFERENCE){
			return refField(property);
		}
		if(getFieldType(property) == FieldType.COLLECTION){
			return "<a href=\"<c:out value=\"\\${func:collectionUrl(context,item,'"+property.name+"')}\" />\"><c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" /> элементов</a>";
		}
		return "<input id=\""+property.name.replace(".", "-")+"\" type=\"text\" name=\""+property.caption+"\"<c:if test=\"\\${(func:property(item,'"+property.name+"')).readOnly}\"> readonly=\"readonly\"</c:if> class=\""+fieldSize(property.size)+"\" value=\"<c:out value=\"\\${(func:property(item,'"+property.name+"')).string}\" />\" />";
	}
	
	private FieldType getFieldType(StoredPropertyMeta pm){
		switch (MetaPropertyType.fromInt(pm.type)){
			case BOOLEAN:return FieldType.CHECKBOX;
			case DATETIME:return FieldType.DATETIME_PICKER;
			case IMAGE:return FieldType.IMAGE;
			case FILE:return FieldType.FILE;
			case HTML:return FieldType.WYSIWYG;
			case TEXT:return FieldType.MULTILINE;
			case SET:return FieldType.MULTISELECT;
			case PASSWORD:return FieldType.PASSWORD;
			case DECIMAL:
			case INT:return FieldType.NUMBER_PICKER;
			case REFERENCE: return FieldType.REFERENCE;
			case COLLECTION: return FieldType.COLLECTION;
			case GEO: return FieldType.GEO;
			default:return FieldType.TEXT;
		}
	}
	
	private String fieldSize(Short size){
		if (size == null)
			return "medium";
		if (size < 4)
			return "tiny";
		if (size < 10)
			return "short";
		if (size < 20)
			return "medium";
		if (size < 40)
			return "long";
		return "big";
	}
	
	private String getDeployLocation(IProject project){
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
		if (projectNode != null) return projectNode.get("deployLocation","");
		return null;
	}
	
	private String getNewJspLocation(IProject project) throws CoreException{
		String node = "";
		if (context instanceof IonNodeResource)
			node  = ((IonNodeResource) context).getName();
		else if (context instanceof IonViewsResource)
			node = ((IonViewsResource) context).Parent().getName();
		IFolder f = project.getFolder("jsp/"+node.replace(".", "/")+"/");
		return f.getLocation().toString()+"/";
	}
	
	private String refField(StoredPropertyMeta p){
		if(p.readonly){
			return "<a href=\"<c:out value=\"\\${func:itemUrl(context,(func:property(item,'"+p.name+"')).referedItem)}\" />\"><c:out value=\"\\${(func:property(item,'"+p.name+"')).string}\" /></a>";
		}else{
			return "<select id=\""+p.name.replace(".", "-")+"\" name=\""+p.name+"\" class=\""+fieldSize(p.size)+"\">"+"\n"+
					"<c:set var=\"sel\" value=\"\\${func:propertySelection(item,'"+p.caption+"')}\" />"+"\n"+
					"<c:forEach items=\"\\${sel}\" var=\"entry\">"+"\n"+
						"<option value=\"<c:out value=\"\\${entry.key}\" />\"<c:if test=\"\\${func:strcmp(entry.key,(func:property(item,'"+p.name+"')).value)}\"> selected</c:if>><c:out value=\"\\${entry.value}\" /></option>"+"\n"+
					"</c:forEach>"+"\n"+
					"</select>"+"\n"+
					"<c:set var=\"ref\" value=\"\\${func:refItem(item,'"+p.caption+"')}\" />"+"\n"+
					"<c:if test=\"\\${ref != null}\">"+"\n"+
					"<a href=\"<c:out value=\"\\${func:itemUrl(context,ref)}\"/>\">"+"\n"+
						"<button type=\"button\" class=\"icon-button ref-link\"></button>"+"\n"+
					"</a>"+"\n"+
					"</c:if>";
		}
	}
	
}