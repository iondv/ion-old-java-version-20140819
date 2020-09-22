package ion.web.util.com;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class ServletPaths implements ServletConfigAware  {
	
	public ServletRegistration registration;
	
	String servletBasePath;	

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		registration = servletConfig.getServletContext().getServletRegistration(servletConfig.getServletName());
	}
	
	public String getBase(){
		if (servletBasePath == null){
			servletBasePath = "";
			Collection<String> mappings = registration.getMappings();
			
			Pattern extractor = Pattern.compile("^\\/?((?:[^\\/]+\\/)*[^\\/]+)\\/\\*$");
			Matcher matches;
			for (String mapping: mappings){
				matches = extractor.matcher(mapping);
				if (matches.find()){
					servletBasePath = "/"+matches.group(1);
					break;
				}
			}
		}
		return servletBasePath;
	}
	
	public String Url(String path) throws UnsupportedEncodingException{
		if (!path.startsWith("/"))
			path = "/"+path;
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(getBase()+path).build().toUriString();
	}	
	
	
}
