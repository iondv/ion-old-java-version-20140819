package ion.web.app;

import ion.core.IItem;
import ion.core.IonException;
import ion.web.app.BasicCreateController;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class AbstractCollectionCreateController extends BasicCreateController {

	public String ajaxForm(String node, String id, String collection, String cc, Model model) throws IonException, ParseException {
		setupForm(node,cc,id,collection,model);
		return ThemeDir()+"ajax/create"; 
	}
	
	public String form(String node, String id, String collection, String cc, Model model) throws IonException, ParseException {
		setupForm(node,cc,id,collection,model);
		return ThemeDir()+"create";
	}
	
	protected Map<String,Object> setContainer(Map<String,Object> posted, String node, String id, String collection) throws IonException {
		PageContext pc = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
		posted.put(pc.CollectionProperty.BackReference(), pc.Id);
		return posted;
	}
	
	public JSONResponse ajaxCreate(String node, String id, String collection, String cc, MultipartHttpServletRequest request, Model model) throws ParseException, IOException{
		try {
			PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
			if (cc == null){
				cc = context.Class.getName();
			}			
			IItem item = data.Create(cc, postedData(cc, LocaleContextHolder.getLocale(), request));
			if (context.CollectionProperty != null)
				data.Put(context.Class.getName(), context.Id, context.CollectionProperty.Name(), item.getClassName(), item.getItemId());
			IItem newItem = data.getItem(item.getClassName(), item.getItemId());
			model.asMap().clear();
			return new JSONResponse(data.ItemToMap(newItem));
		} catch (Exception e){
			e.printStackTrace();
			model.asMap().clear();
			return new JSONResponse(e.getMessage());
		}		
	}	

	public String create(String node, String id, String collection, String cc, MultipartHttpServletRequest request, Model model) {
		try {
			PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
			if (cc == null)
				cc = context.CollectionProperty.ItemsClass().getName();
			IItem item = data.Create(cc, postedData(cc, LocaleContextHolder.getLocale(), request));
			if (context.CollectionProperty != null)
				data.Put(context.Class.getName(), context.Id, context.CollectionProperty.Name(), item.getClassName(), item.getItemId());
			model.asMap().clear();
			return "redirect:"+urlfactory.Url(node, item);
		} catch (Exception e){
			handleException(new IonException(e), model);
		}
		return ThemeDir()+"create";
	}
}
