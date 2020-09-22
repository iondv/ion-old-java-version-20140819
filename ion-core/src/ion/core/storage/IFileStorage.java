package ion.core.storage;

import ion.core.IonException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public interface IFileStorage {
	String Accept(InputStream content, String name, String contentType) throws IonException;
	String Accept(InputStream content, String name) throws IonException;
	String Accept(File f) throws IonException;
	URL getUrl(String id) throws IonException;
	java.io.File getFile(String id) throws IonException;
}
