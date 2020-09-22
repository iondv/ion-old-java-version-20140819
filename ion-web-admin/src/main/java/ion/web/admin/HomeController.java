package ion.web.admin;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController extends BasicController {
	
	@RequestMapping("/")
	public String home(Map<String, Object> map) {
		return themeDir+"/home";
	}
	
	@RequestMapping("/index")
	public String index(Map<String, Object> map) {
		return themeDir+"/home";
	}
	
	
	
}
