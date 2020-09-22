package ion.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController extends BasicController {

	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String login(ModelMap model) {
		return themeDir+"/login";
	}
	
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logout(ModelMap model) {
		return themeDir+"/login";
	}
	
	@RequestMapping(value="/loginfailed", method = RequestMethod.GET)
	public String loginerror(ModelMap model) {
		model.addAttribute("error", "true");
		return themeDir+"/login";
	}
	
	@RequestMapping(value="/403", method = RequestMethod.GET)
	public String accessDenied(ModelMap model) {
		return themeDir+"/403";
	}
	
}
