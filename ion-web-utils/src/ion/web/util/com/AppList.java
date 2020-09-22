package ion.web.util.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class AppList implements ServletConfigAware {
	
	private ServletContext context;
	
	private Map<String,String> list;
	
	public Map<String,String> getList(){
		if (list == null){
			list = new LinkedHashMap<String, String>();
			String realpath = context.getRealPath(File.separator + "WEB-INF" + File.separator +"appslinks.properties");
			File f = null;
			try {
				if (realpath == null){
					Resource res = new ServletContextResource(context,File.separator + "WEB-INF" + File.separator +"appslinks.properties");
					f = res.getFile();
				} else 
					f = new File(realpath);
				
				if (f.exists()){
					Properties props = new Properties();
					String key;
					String prefix;
					String value;
					String path;
					String[] name;
					
					Map<String, String> titles = new HashMap<String, String>();
					Map<String, String> mappings = new HashMap<String, String>();
					
					props.load(new FileInputStream(f));				
					for (Entry<Object,Object> entry: props.entrySet()){
						key = entry.getKey().toString();
						value = entry.getValue().toString();
						if (key.contains(".")){
							name = key.split("\\.");
							prefix = name[0];
							key = name[1];
						
							if (key.equals("title"))
								titles.put(prefix, value);
							if (key.equals("mapping"))
								mappings.put(prefix, value);
							
							if (titles.containsKey(prefix) && mappings.containsKey(prefix)){
								path = mappings.get(prefix);
								if (!path.endsWith("/"))
									path = path + "/";
								list.put(titles.get(prefix), ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString());
							}
						}
					}
				}				
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}	
				
		}
		return list;
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		context = servletConfig.getServletContext();
	}

}
