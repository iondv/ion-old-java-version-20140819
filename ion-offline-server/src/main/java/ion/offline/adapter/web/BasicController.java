package ion.offline.adapter.web;

import java.util.Map;

import ion.web.util.com.AppList;
import ion.web.util.com.ServletPaths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class BasicController {
	
	@Autowired
	protected AppList applist;
	
	@Autowired
	protected ServletPaths servletPaths;
	
	@Value("${app.themeDir}")
	protected String themeDir;
	
  @ModelAttribute("AppLinks")
  public Map<String, String> AppLinks(){
  	return applist.getList();
  }
  
  @ModelAttribute("AppRoot")
  public String AppRoot(){
  	return ServletUriComponentsBuilder.fromCurrentContextPath().path(servletPaths.getBase()).build().toUriString();
  }
}
