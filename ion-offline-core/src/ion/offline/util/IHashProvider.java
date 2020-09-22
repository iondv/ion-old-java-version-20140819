package ion.offline.util;

import ion.core.IonException;

import java.io.File;

public interface IHashProvider {
	String hash(String source) throws IonException;
	
	String hash(File file) throws IonException;
}
