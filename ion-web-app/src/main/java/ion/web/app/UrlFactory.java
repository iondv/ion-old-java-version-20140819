package ion.web.app;

import ion.core.IItem;
import ion.web.util.com.ServletPaths;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

@Component
public class UrlFactory {

	public final static String NODE_MAPPING = "/{node}";

	//public final static String ITEM_MAPPING = "/{id}";
	
	public final static String COLLECTION_MAPPING = "/{id}/{collection}";

	public final static String FULL_ITEM_MAPPING = "/{node}/{id}";	
	
	public final static String FULL_COLLECTION_MAPPING = "/{node}/{id}/{collection}";
	
	private static UrlFactory singleton;
	
	private ServletPaths servletPaths;
	
	public UrlFactory() {
		super();
		singleton = this;
	}	
		
	public static String rootListUrl(String src) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		return UrlFactory.MakeUrl("/"+URLEncoder.encode(urlNode(src),"UTF-8"));
	}
	
	public static String urlNode(String src) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		URL url = new URL(src);
		String appRoot = UrlFactory.MakeUrl("/");
		URI relative = new URL(appRoot).toURI().relativize(url.toURI());
		String[] pth = relative.getPath().split("/");
		return pth[0];
	}		
	
	public static String MakeUrl(String path) throws UnsupportedEncodingException{
		return singleton.Url(path);
	}
	
	public String Url(String path) throws UnsupportedEncodingException{
		if (!path.startsWith("/"))
			path = "/"+path;
		return servletPaths.Url(path);
	}
		
	public String NodeUrl(String node) throws UnsupportedEncodingException{
		return Url(URLEncoder.encode(node.replace(".", ":"),"UTF-8"));
	}
	
	public String Url(String node, IItem item) throws UnsupportedEncodingException{
		return Url(node,item.getClassName(),item.getItemId());
	}
	
	public String Url(String node, IItem item, String collection) throws UnsupportedEncodingException{
		return Url(node,item)+"/"+URLEncoder.encode(collection,"UTF-8");
	}
	
	public String Url(String node, String classname, String itemid) throws UnsupportedEncodingException{
		return NodeUrl(node)+"/"+URLEncoder.encode(classname + ":" + itemid,"UTF-8");
	}
	
	public String Url(String node, String classname, String itemid, String collection) throws UnsupportedEncodingException{
		return Url(node,classname,itemid)+"/"+URLEncoder.encode(collection,"UTF-8");
	}

	public ServletPaths getServletPaths() {
		return servletPaths;
	}

	public void setServletPaths(ServletPaths servletPaths) {
		this.servletPaths = servletPaths;
	}	
}
