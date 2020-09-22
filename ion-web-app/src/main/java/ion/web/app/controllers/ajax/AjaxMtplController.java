package ion.web.app.controllers.ajax;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ion.core.DACPermission;
import ion.core.IonException;
import ion.viewmodel.navigation.GroupNode;
import ion.viewmodel.navigation.INavigationSection;
import ion.viewmodel.navigation.IClassNode;
import ion.viewmodel.navigation.INode;
import ion.viewmodel.navigation.NodeType;
import ion.web.app.BasicController;
import ion.web.app.jstl.Urls;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

@Controller
public class AjaxMtplController extends BasicController {
	
	@RequestMapping(value="/spa/menu", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse ajaxMenu(Model model, String section){ 
		try {
			Map<String,Object> result = new HashMap<String,Object>();
			INavigationSection s = navmodel.getNavigationSection(section);
			if (s != null)
				result.put("nodes", getMenuNodes(s.getRootNodes()));
			else
				result.put("node", new HashMap<String, String>());
			return new JSONResponse(result);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	@RequestMapping(value="/spa/selection", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getSelectionList(
	                    @RequestParam(value="__id", required=true) String id,
											@RequestParam(value="__property", required=true) String property) throws IonException{
		PageContext context = new PageContext(null, id, meta, navmodel, urlfactory, data);
		return new JSONResponse(data.getPropertySelection(context.Class.getName(), context.Id, property));
	}
	
	@RequestMapping(value="/spa/selections", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getSelectionLists(
	                    @RequestParam(value="__id", required=true) String id,
	                    @RequestParam(value="__reload", required=false) String[] reload,
	                    MultipartHttpServletRequest request) throws IonException{
		PageContext context = new PageContext(null, id, meta, navmodel, urlfactory, data);
		Map<String, Object> values = new HashMap<String, Object>();
		for (Map.Entry<String, String[]> p: request.getParameterMap().entrySet()){
			if (!p.getKey().equals("__id") && !p.getKey().equals("__reload"))
				values.put(p.getKey(), p.getValue()[0]);
		}
		return new JSONResponse(data.getPropertiesSelection(context.Class.getName(), context.Id, values, reload));
	}
		
	private Collection<MenuNode> getMenuNodes(Collection<INode> collection) throws IonException, UnsupportedEncodingException{
		Collection<MenuNode> menu = new LinkedList<AjaxMtplController.MenuNode>();
		for(INode c : collection){
			Collection<MenuNode> subs = null;
			boolean navAccess = data.CheckNavAccess(c.getCode());
			if(c.getType() == NodeType.GROUP){
				subs = getMenuNodes(((GroupNode)c).getChildNodes());
				navAccess = navAccess || (subs.size() > 0); 
			}
			if (navAccess){
  			MenuNode mn = new MenuNode(Urls.parseNodeId(c.getCode()), c.getCaption(), c.getType().toString(), null);
  			if(c.getType() == NodeType.GROUP){
  				mn.setNodes(subs);
  				for (MenuNode smn: mn.nodes){
  					if (smn.writeAccess){
  						mn.setWriteAcces(true);
  						break;
  					}
  				}
  			}
  			if(c.getType() == NodeType.CLASS){
  					mn.setWriteAcces(data.CheckObjectPermissons(meta.Get(((IClassNode)c).getClassName()), new DACPermission[]{DACPermission.USE}));
  			}
  			menu.add(mn);
			}
		}
		return menu;
	}

	public class MenuNode{
		private String id;
		private String caption;
		private String type;
		private Collection<MenuNode> nodes;
		private boolean writeAccess = true;
		
		public boolean isWriteAcces() {
			return writeAccess;
		}
		public void setWriteAcces(boolean writeAcces) {
			this.writeAccess = writeAcces;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getCaption() {
			return caption;
		}
		public void setCaption(String caption) {
			this.caption = caption;
		}
		public Collection<MenuNode> getNodes() {
			return nodes;
		}
		public void setNodes(Collection<MenuNode> nodes) {
			this.nodes = nodes;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public MenuNode(String id, String caption, String type,
				Collection<MenuNode> nodes) {
			super();
			this.id = id;
			this.caption = caption;
			this.type = type;
			this.nodes = nodes;
		}
		public MenuNode(String id, String caption, String type,
		        				Collection<MenuNode> nodes, boolean writeAcces) {
			this(id, caption, type, nodes);
			this.writeAccess = writeAcces;
		}
		public MenuNode() {
			super();
		}
	}
}
