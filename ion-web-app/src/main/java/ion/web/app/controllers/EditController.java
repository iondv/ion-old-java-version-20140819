package ion.web.app.controllers;

import ion.core.IonException;
import ion.web.app.AbstractEditController;
import ion.web.app.UrlFactory;
import ion.web.app.util.JSONResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping(UrlFactory.FULL_ITEM_MAPPING)
public class EditController extends AbstractEditController {
	
	@RequestMapping(method = RequestMethod.GET)
	public String form(@PathVariable String node, @PathVariable String id, Model model, HttpSession session) {
		getMessageFromSession(session, model);
		return super.form(node, id, model);
	}
	
	@RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD},headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse handleAjax(@PathVariable String node, MultipartHttpServletRequest request, HttpSession session, Model model, @RequestParam String form_action_name) throws IonException, ParseException, IOException{
		return super.handleAjax(node, request, session, model, form_action_name);
	}	

	@RequestMapping(method = RequestMethod.POST)
	public String handle(@PathVariable String node, @PathVariable String id, Locale locale, MultipartHttpServletRequest request, HttpSession session, Model model, @RequestParam String form_action_name) throws ParseException, IOException, IonException{
		return super.handle(node, id, request, session, model, form_action_name);
	}	
}
