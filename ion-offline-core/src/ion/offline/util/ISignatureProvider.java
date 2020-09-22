package ion.offline.util;

import ion.core.IonException;

import java.io.InputStream;

public interface ISignatureProvider {
	String sign(String privKey, String data) throws IonException;
	
	String sign(String privKey, InputStream data) throws IonException;
	
	boolean check(String pubKey, String signature, String data) throws IonException;
	
	boolean check(String pubKey, String signature, InputStream data) throws IonException;
	
	String[] createKeyPair() throws IonException;
}
