package ion.offline.adapter.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public class ViewModelPublisher extends ResourceHttpRequestHandler {
    @Value("${app.viewModelDir}")
    private String vmDir;	
	
    @Override
    protected Resource getResource(HttpServletRequest request) {
    	String path = vmDir + File.separator + (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    	File f = new File(path);
    	if (f.exists())
    		return new FileSystemResource(f);

    	ServletContext context = request.getServletContext();
    	String realpath = context.getRealPath(path);
    	if (realpath  == null){
    		return new ServletContextResource(context,path);
    	}
    	return new FileSystemResource(realpath);
    }
}
