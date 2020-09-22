package ion.web.app.controllers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ion.core.IonException;
import ion.web.app.AbstractListController;
import ion.web.app.UrlFactory;
import ion.web.app.util.JSONResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({UrlFactory.NODE_MAPPING})
public class ListController extends AbstractListController {
		
	@RequestMapping(method = RequestMethod.GET)
	public String list(@PathVariable String node, @RequestParam(required=false) Integer page, HttpServletRequest request, Model model) throws Exception {
		return super.list(node, page, request, model);
	}
	
	@RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse ajaxList(@PathVariable String node,
												@RequestParam(value="className", required=false) String className,
												@RequestParam(value="page", required=false) Integer page,
												@RequestParam(value="__filter", required=false) String options, 
												@RequestParam(value="__sorting", required=false) String sortings,
												Model model) throws IonException {
		return super.ajaxList(node, className, page, options, sortings);
	}
	
	@RequestMapping(value=UrlFactory.COLLECTION_MAPPING, method = RequestMethod.GET)
	public String collection(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam(required=false) Integer page, HttpServletRequest request, Model model) throws Exception{
		return super.collection(node, id, collection, page, request, model);
	}
	
	@RequestMapping(value=UrlFactory.COLLECTION_MAPPING, method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse ajaxCollection(@PathVariable String node, @PathVariable String id, @PathVariable String collection,
													@RequestParam(required=false) Integer page, 
													@RequestParam(value="__filter", required=false) String options, 
													@RequestParam(value="__sorting", required=false) String sorting,
													Model model) throws Exception{
		return super.ajaxCollection(node, id, collection, page, options, sorting);
	}
	
	@RequestMapping(value="/delete", method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse delete(@PathVariable String node, @RequestParam(value="ids[]", required=false) String[] ids, Model model) throws IonException, IOException{
		return super.delete(node, ids, model);
	}	
	
	@RequestMapping(value="/delete/{id}", method = {RequestMethod.GET, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse delete(@PathVariable String node, @PathVariable String id, Model model) throws IonException, IOException {
		return super.delete(node, id, model);
	}
	
	@RequestMapping(value="/print", method = {RequestMethod.GET, RequestMethod.POST})
	public String print(@PathVariable String node, @RequestParam(value="__page",required=false) Integer page, HttpServletRequest request, Model model) throws Exception {
		super.list(node, page, request, model);
		return ThemeDir() + "print";
	}
	
	@RequestMapping(value=UrlFactory.COLLECTION_MAPPING+"/print", method = {RequestMethod.GET, RequestMethod.POST})
	public String print(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam(value="__page", required=false) Integer page, HttpServletRequest request, Model model) throws Exception {
		super.collection(node, id, collection, page, request, model);
		return ThemeDir() + "print";
	}
	
	@RequestMapping(value="/excel", method = {RequestMethod.GET, RequestMethod.POST})
	public void excel(@PathVariable String node, @RequestParam(value="__page", required=false) Integer page, HttpServletRequest request, Model model, HttpServletResponse response) throws Exception {
		super.list(node, page, request, model);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String filename = node + "_" + dateFormat.format(date);
		super.excel(filename, model, response);
	}
	
	@RequestMapping(value=UrlFactory.COLLECTION_MAPPING+"/excel", method = {RequestMethod.GET, RequestMethod.POST})
	public void excel(@PathVariable String node, @PathVariable String id, @PathVariable String collection, @RequestParam(value="__page", required=false) Integer page, HttpServletRequest request, Model model, HttpServletResponse response) throws Exception {
		super.collection(node, id, collection, page, request, model);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String filename = node + "_" + id + "_" + collection + "_" + dateFormat.format(date);
		super.excel(filename, model, response);
	}
}