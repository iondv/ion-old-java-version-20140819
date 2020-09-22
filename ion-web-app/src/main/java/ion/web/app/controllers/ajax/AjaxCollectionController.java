package ion.web.app.controllers.ajax;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.web.app.AbstractCollectionCreateController;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

@Controller
public class AjaxCollectionController extends AbstractCollectionCreateController {
	
	@RequestMapping(value="/spa/colcreate",
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse collectionCreate(MultipartHttpServletRequest request, Model model, 
	                                                   @RequestParam(value="__container", required=true) String id,
	                                                   @RequestParam(value="__collection", required=true) String collection,
	                                                   @RequestParam(value="__cc", required=true) String cc) {
				try {
					return ajaxCreate(null, id, collection, cc, request, model);
				} catch (ParseException | IOException e) {
					return new JSONResponse(e.getLocalizedMessage());
				}
	}

	@RequestMapping(value="/spa/coldelete",
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse collectionDel(MultipartHttpServletRequest request,
                                                  @RequestParam(value="__container", required=false) String id,
                                                  @RequestParam(value="__collection", required=false) String collection,	                                                
	                                                 @RequestParam(value="ids", required=true) String[] ids
	                                               ) throws IonException {
		Collection<String[]> result = new ArrayList<String[]>();
		String[] containerId = null;
		if (id != null)
			containerId = id.split(":");
		try {
			for (String i: ids){
				String[] itemId = i.split("@");
				if (id != null)
					data.Eject(containerId[0], containerId[1], collection, itemId[0], itemId[1]);
				else
					data.Delete(itemId[0], itemId[1]);
				result.add(itemId);
			}
			return new JSONResponse(result);
		} catch (IonException e) {
			return new JSONResponse(e.getLocalizedMessage());
		}
	}	
	
	@RequestMapping(value="/spa/colappend",
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse collectionAppend(MultipartHttpServletRequest request, Model model, @RequestParam(value="__node", required=true) String node, 
	                                                     @RequestParam(value="__id", required=true) String id,
	                                                     @RequestParam(value="__collection", required=true) String collection,
	                                                     @RequestParam(value="__items", required=true) String[] items) throws IonException, ParseException, IOException{
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
		for(String i : items){
			String[] itemInfo = i.split("@");
			data.Put(context.Class.getName(), context.Id, context.CollectionProperty.Name(), itemInfo[0], itemInfo[1]);
		}
		IItem newItem = data.getItem(context.Class.getName(), context.Id);
		return new JSONResponse(data.ItemToMap(newItem));
	}	
	
	@RequestMapping(value="/spa/collection/dummy", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getCollectionItemDummy(@RequestParam(value="__node", required=true) String node,
	  	                                                     @RequestParam(value="__container", required=true) String container,
	  	                                                     @RequestParam(value="__collection", required=true) String collection,
	  	                                                     @RequestParam(value="__class", required=false) String cc, 
	  	                                                     Model model){
		try {
			PageContext context = new PageContext(node, container, collection, meta, navmodel, urlfactory, data);
			if (cc == null){
				cc = context.CollectionProperty.ItemsClass().getName();
			}		
			IItem item = data.getDummy(cc);
			data.initItem(item, true);
			IPropertyMeta pm = context.Class.PropertyMeta(collection);
			if(pm instanceof ICollectionPropertyMeta){
				String br = ((ICollectionPropertyMeta)pm).BackReference();
				if (br != null && !br.isEmpty())
					item.Set(br, idToKey(cc,br, container));
			}
			return new JSONResponse(data.ItemToMap(item));
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/collection/classinfo", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getCollectionInfo(@RequestParam(value="className", required=true) String className,
											@RequestParam(value="property", required=true) String property,
											Model model) throws IonException{
		IStructMeta m = meta.Get(className);
		IPropertyMeta pm = m.PropertyMeta(property);
		if (pm instanceof ICollectionPropertyMeta) {
			String[] info = new String[2];
			info[0] = ((ICollectionPropertyMeta) pm).ItemsClass().getName();
			info[1] = ((ICollectionPropertyMeta) pm).BackReference();
			return new JSONResponse(info);
		}
		return null;
	}
	
	protected Object idToKey(String classname, String property, String id) throws IonException{
		if (id == null)
			return null;
		IClassMeta cm = (IClassMeta)meta.Get(classname);
		switch (cm.PropertyMeta(property).Type()){
			case BOOLEAN:return (id == "1")?true:false;
			case DATETIME:return new Date(Long.parseLong(id));
			case REAL:
			case DECIMAL:return Float.parseFloat(id);
			case INT:
			case SET:return Integer.parseInt(id);
			default:return id;
		}
	}
}
