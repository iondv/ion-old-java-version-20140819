package ion.core.mocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import ion.core.IonException;
import ion.core.storage.IFileStorage;

public class FileStorageMock implements IFileStorage {
	
	private Map<String, InputStream> store;
	private String url_prefix;	

	public FileStorageMock(Map<String, InputStream> store, String url_prefix) {
		super();
		this.store = store;
		this.url_prefix = url_prefix;
	}

	@Override
	public String Accept(InputStream content, String name, String contentType) throws IonException {return this.Accept(content, name);}

	@Override
	public String Accept(InputStream content, String name) throws IonException {store.put(name, content); return name;}

	@Override
	public URL getUrl(String id) throws IonException {
		try {
			return new URL(url_prefix+"/"+id);
		} catch (MalformedURLException e) {
			throw new IonException(e);
		}
	}

	@Override
	public String Accept(File f) throws IonException {
		try {
			store.put(f.getName(), new FileInputStream(f));
		} catch (FileNotFoundException e) {
			throw new IonException(e);
		}
		return f.getName();
	}


	@Override
	public File getFile(String id) throws IonException {
		return null;
	}

}
