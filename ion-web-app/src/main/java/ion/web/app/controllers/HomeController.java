package ion.web.app.controllers;

import ion.web.app.AbstractHomeController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController extends AbstractHomeController{	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		return super.home(model);
	}
	
}
