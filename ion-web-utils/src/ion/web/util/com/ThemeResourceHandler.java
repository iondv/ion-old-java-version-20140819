package ion.web.util.com;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public class ThemeResourceHandler extends ResourceHttpRequestHandler {

    private String themeDir = "";
	
    public void setThemeDir(String themeDir) {
			this.themeDir = themeDir;
		}

		@Override
    protected Resource getResource(HttpServletRequest request) {
    	String path = File.separator + "resources" + File.separator;
    	if (themeDir != null && !themeDir.isEmpty())
    		path = path + themeDir + File.separator;
    	path = path + (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    	ServletContext context = request.getServletContext();
    	String realpath = context.getRealPath(path);
    	if (realpath  == null){
    		return new ServletContextResource(context,path);
    	}
    	return new FileSystemResource(realpath);
    }
}
