package ion.web.app.controllers;

import ion.core.IonException;
import ion.web.app.AbstractCollectionCreateController;
import ion.web.app.UrlFactory;
import ion.web.app.util.JSONResponse;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping({UrlFactory.FULL_COLLECTION_MAPPING+"/create"})
public class CollectionCreateController extends AbstractCollectionCreateController {

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public String ajaxForm(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam(required=false) String cc, Model model) throws IonException, ParseException {
		return super.ajaxForm(node, id, collection, cc, model); 
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
	public String form(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam(required=false) String cc, Model model) throws IonException, ParseException {
		return super.form(node, id, collection, cc, model);
	}
	
	@RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse ajaxCreate(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam String cc, MultipartHttpServletRequest request, Model model) throws ParseException, IOException{
		return super.ajaxCreate(node, id, collection, cc, request, model);
	}	

	@RequestMapping(method = RequestMethod.POST)
	public String create(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam String cc, MultipartHttpServletRequest request, Model model) {
		return super.create(node, id, collection, cc, request, model);
	}

}
