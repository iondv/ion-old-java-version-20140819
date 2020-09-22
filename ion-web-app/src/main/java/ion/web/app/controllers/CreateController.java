package ion.web.app.controllers;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpSession;

import ion.core.IonException;
import ion.web.app.AbstractCreateController;
import ion.web.app.UrlFactory;
import ion.web.app.util.JSONResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping({UrlFactory.NODE_MAPPING+"/create"})
public class CreateController extends AbstractCreateController {
			
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
	public String form(@PathVariable String node, @RequestParam(required=false) String cc, Model model) throws ParseException {
		return super.form(node, cc, model);
	}
	
	@RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse handleAjax(@PathVariable String node, MultipartHttpServletRequest request, HttpSession session, Model model, @RequestParam String form_action_name) throws IonException, ParseException, IOException{
		return super.handleAjax(node, request, session, model, form_action_name);
	}	

	@RequestMapping(method = RequestMethod.POST)
	public String handle(@PathVariable String node, MultipartHttpServletRequest request, HttpSession session, Model model, @RequestParam String form_action_name) throws ParseException, IOException, IonException {
		return super.handle(node, request, session, model, form_action_name);
	}
}