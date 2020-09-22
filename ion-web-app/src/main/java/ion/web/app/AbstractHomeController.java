package ion.web.app;

import ion.web.app.BasicController;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

public class AbstractHomeController extends BasicController{
	
	@Value("${nav.defaultPage}")	
	private String defaultPage;	
	
	public String home(Model model) {
		if (defaultPage != null && !defaultPage.trim().equals("") && !defaultPage.trim().equals("/")){
			model.asMap().clear();
			return "redirect:" + defaultPage;
		}
		return ThemeDir()+"home";
	}
	
}
