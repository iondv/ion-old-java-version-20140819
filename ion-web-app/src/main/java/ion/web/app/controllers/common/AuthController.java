package ion.web.app.controllers.common;

import ion.web.util.IAuthSetting;
import ion.web.util.IAuthSettings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthController {
    @Value("${app.defaultTitle}")
    private String applicationTitle;
    
    @Value("${app.themeDir}")
    private String themeDir;
    
    @Autowired
    private IAuthSettings authSettings;
    
    @ModelAttribute("ThemeDir")
    protected String ThemeDir(){
    	return (themeDir == null)?"":(themeDir+"/");
    }
    
    @ModelAttribute("AuthSettings")
    protected  IAuthSetting[] getAuthSettings(){
    	return authSettings.getSettings();
    }
    
    @ModelAttribute("HasAuthSettings")
    protected boolean hasAuthSettings(){
    	return authSettings.isProvided();
    }
    
    
	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String login(ModelMap model) {
		return ThemeDir()+"login";
	}
 
	@ModelAttribute("Title")
	public String title(Model model) {
		return applicationTitle;
	}	
	
	@RequestMapping(value="/loginfailed", method = RequestMethod.GET)
	public String loginerror(ModelMap model) {
		model.addAttribute("error", "true");
		return ThemeDir()+"login";
	}
}
