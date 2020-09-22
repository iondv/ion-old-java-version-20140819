package ion.offline.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ion.core.IonException;
import ion.offline.util.IHashProvider;

public class HashProvider implements IHashProvider {
	
	private String algo = "MD5";
	
	public String getAlgo() {
		return algo;
	}

	public void setAlgo(String algo) {
		this.algo = algo;
	}

	private String toHex(byte[] bytes){
        StringBuffer hexString = new StringBuffer();
    	for (int i = 0; i < bytes.length; i++) {
    		String hex=Integer.toHexString(0xff & bytes[i]);
   	     	if (hex.length() == 1) 
   	     		hexString.append('0');
   	     	hexString.append(hex);
    	}		
    	return hexString.toString();
	}
	
	public String hash(String source) throws IonException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			throw new IonException(e);
		}
		md.reset();
	    md.update(source.getBytes());
		String result = toHex(md.digest());
		md.reset();
		return result;
	}

	public String hash(File file) throws IonException {
		MessageDigest md;
		FileInputStream fis;
		try {
			md = MessageDigest.getInstance(algo);
	        fis = new FileInputStream(file);
		} catch (NoSuchAlgorithmException | FileNotFoundException e) {
			throw new IonException(e);
		}
 
		md.reset();
        byte[] dataBytes = new byte[1024];
        int nread = 0; 
        try {
			while ((nread = fis.read(dataBytes)) != -1) {
			  md.update(dataBytes, 0, nread);
			}
			fis.close();
		} catch (IOException e) {
			throw new IonException(e);
		}
    	String result = toHex(md.digest());
    	md.reset();
    	return result;
	}

}
