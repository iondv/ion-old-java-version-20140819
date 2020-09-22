package ion.web.app.storage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import ion.core.IonException;
import ion.core.storage.IFileStorage;
import ion.core.storage.SimpleFileStorage;
import ion.web.app.jstl.Urls;

public class AppFileStorage extends SimpleFileStorage implements IFileStorage, ApplicationListener<ContextRefreshedEvent> {
	
	private String directory = "resources" + File.separator + "files";
	private String path = "files";

	public AppFileStorage() {
		Urls.storage = this;
	}

	@Override
	public URL getUrl(String id) throws IonException {
		try {
			return new URL(Urls.Url(path + "/" + id.replace(File.separator, "/")));
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			throw new IonException(e);
		}
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext app = event.getApplicationContext();
		
		Resource r = app.getResource(directory);
		try {
			if (r.exists()){
				storageRoot = r.getFile().getAbsolutePath();
			} else {
				Resource resDir = app.getResource("");
				File dir = new File(resDir.getFile().getAbsolutePath() + File.separator + directory);
				dir.mkdirs();
				storageRoot = dir.getAbsolutePath();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
