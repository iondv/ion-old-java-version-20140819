package ion.web.app.jstl;

import java.io.UnsupportedEncodingException;

import ion.core.IItem;
import ion.core.IonException;
import ion.core.storage.IFileStorage;
import ion.web.app.UrlFactory;
import ion.web.app.util.PageContext;

public final class Urls {
	
	public static IFileStorage storage;
	
	public static String itemUrl(PageContext context, IItem item){
		return context.getLink(item);
	}
	
	public static String collectionUrl(PageContext context, IItem item, String name){
		return context.getLink(item, name);
	}
	
	public static String fileUrl(String id) throws IonException {
		if (id != null){
			try {
				return storage.getUrl(id).toExternalForm();
			} catch (IonException e) {
				
			}
		}
		return null;
	}
	
	public static String fileName(String id) throws IonException {
		return storage.getFile(id).getName();
	}
	
	public static String Url(String path) throws UnsupportedEncodingException{
		return UrlFactory.MakeUrl(path);
	}
	
	public static String parseNodeId(String nodeId) throws UnsupportedEncodingException{
		return nodeId.replace(".", ":");
	}
	
}
