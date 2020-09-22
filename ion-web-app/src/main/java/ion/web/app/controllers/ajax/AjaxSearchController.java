package ion.web.app.controllers.ajax;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.db.search.IFulltextSearchAdapter;
import ion.web.app.BasicController;
import ion.web.app.util.JSONResponse;

@Controller
public class AjaxSearchController extends BasicController{
	
	@Autowired(required=false)
	IFulltextSearchAdapter adapter;
	
	@RequestMapping(value = "/spa/search", 
			method = RequestMethod.POST,
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse results(
	                 @RequestParam(required=false) String pattern, 
	                 @RequestParam(value="__page", required=false) Integer page, 
	                 Model model) throws UnsupportedEncodingException{
		if(adapter!=null){
			List<String> results = adapter.search(pattern, 20*((page == null?1:page) - 1), 20);
			List<AjaxSearchResult> list = new ArrayList<AjaxSearchResult>(results.size());
			for (String id: results){
				String[] parts = id.split("@");
				IItem i;
				List<AjaxSearchProperty> props = new ArrayList<AjaxSearchController.AjaxSearchProperty>();
				try {
					i = data.getItem(parts[0], parts[1]);
					String nodes = navmodel.getNodeForClassname(parts[0]).replaceAll("\\.", ":");
					if (i != null){
						for (IProperty p : i.getProperties().values())
							if (p.Meta().IndexSearch() && (p.getType() == MetaPropertyType.STRING || p.getType() == MetaPropertyType.TEXT))
								props.add(new AjaxSearchProperty(p.getCaption(),i.Get(p.getName())));
						list.add(new AjaxSearchResult(nodes,i.getClassName(),i.getItemId(), i.toString(), props));
					}
				} catch (IonException e) {
					return new JSONResponse(e.getMessage());
				}
			}
			return new JSONResponse(list);
		}
		return null;
	}

	public class AjaxSearchResult {
		private String classNode;
		private String className;
		private String id;
		private String title;
		private List<AjaxSearchProperty> properties;
		
		public AjaxSearchResult(String node, String className, String id, String title, List<AjaxSearchProperty> properties) {
			this.classNode = node;
			this.className = className;
			this.id = id;
			this.title = title;
			this.properties = properties;
		}
		
		public String getClassNode(){
			return classNode;
		}
		
		public String getClassName() {
			return className;
		}
		
		public String getId(){
			return id;
		}
		
		public String getTitle(){
			return title;
		}
		
		public List<AjaxSearchProperty> getProperties() {
			return properties;
		}
	}
	
	public class AjaxSearchProperty {
		private String caption;
		private Object value;
		public String getCaption() {
			return caption;
		}
		public void setCaption(String caption) {
			this.caption = caption;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public AjaxSearchProperty(String caption, Object value) {
	    this.caption = caption;
	    this.value = value;
    }
	}
}
