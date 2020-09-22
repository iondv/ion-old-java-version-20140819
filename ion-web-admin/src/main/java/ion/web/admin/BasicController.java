package ion.web.admin;

import ion.web.util.com.AppList;
import ion.web.util.com.ServletPaths;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

public class BasicController {
  
  @Autowired
  private ServletPaths servletPaths;
  
  @Autowired
  private AppList applist;
  
  @Value("${app.themeDir}")
  protected String themeDir;  
		    
  @ModelAttribute("Root")
  public String CurrentRoot() throws UnsupportedEncodingException{
    return Url("");
  }
    
  @ModelAttribute("AppLinks")
  public Map<String, String> AppLinks(){
  	return applist.getList();
  }    
    
  public String Url(String path) throws UnsupportedEncodingException{
		return servletPaths.Url(path);    	
  }
}

