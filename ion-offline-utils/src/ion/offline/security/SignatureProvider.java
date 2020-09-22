package ion.offline.security;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import org.springframework.util.Base64Utils;

import ion.core.IonException;
import ion.offline.util.ISignatureProvider;

public class SignatureProvider implements ISignatureProvider {
	
	private String keyAlgo = "RSA";
	
	private String signAlgo = "SHA256withRSA";
	
	private String securityProvider = "SUN";
	
	private String randomizeAlgo = "SHA1PRNG";
	
	private int keySize = 1024;
	
	private String base64Encode(byte[] input){
		return Base64Utils.encodeToString(input);
	}
	
	private byte[] base64Decode(String input){
		return Base64Utils.decodeFromString(input);
	}
	
	private PrivateKey privFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException{
	    byte[] clear = base64Decode(key/*.replaceAll("/(.{1,64})/g", "$1\n")*/);
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
	    KeyFactory fact = KeyFactory.getInstance(keyAlgo);
	    PrivateKey priv = fact.generatePrivate(keySpec);
	    Arrays.fill(clear, (byte) 0);
	    return priv;		
	}
	
	private String privToString(PrivateKey key) throws NoSuchAlgorithmException, InvalidKeySpecException{
		KeyFactory fact = KeyFactory.getInstance(keyAlgo);
		PKCS8EncodedKeySpec spec = fact.getKeySpec(key,PKCS8EncodedKeySpec.class);
		byte[] packed = spec.getEncoded();
		String key64 = base64Encode(packed);
		Arrays.fill(packed, (byte) 0);
		return key64;		
	}
	
	private PublicKey pubFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException{
	    byte[] data = base64Decode(key);
	    X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
	    KeyFactory fact = KeyFactory.getInstance(keyAlgo);
	    return fact.generatePublic(spec);		
	}
	
	private String pubToString(PublicKey key) throws NoSuchAlgorithmException, InvalidKeySpecException{
	    KeyFactory fact = KeyFactory.getInstance(keyAlgo);
	    X509EncodedKeySpec spec = fact.getKeySpec(key,X509EncodedKeySpec.class);
	    return base64Encode(spec.getEncoded());		
	}

	public String sign(String privKey, String data) throws IonException {
		try {
			PrivateKey key = privFromString(privKey);
			Signature dsa = Signature.getInstance(signAlgo, securityProvider); 			
			dsa.initSign(key);
			dsa.update(data.getBytes());
			return base64Encode(dsa.sign());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | 
				NoSuchProviderException | InvalidKeyException | 
				SignatureException e) {
			throw new IonException(e);
		}
	}

	public String sign(String privKey, InputStream data) throws IonException {
		try {
			PrivateKey key = privFromString(privKey);
			Signature dsa = Signature.getInstance(signAlgo, securityProvider); 			
			dsa.initSign(key);
			
			BufferedInputStream bufin = new BufferedInputStream(data);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bufin.read(buffer)) >= 0) {
			    dsa.update(buffer, 0, len);
			};
			bufin.close();			
			data.close();
			return base64Encode(dsa.sign());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | 
				NoSuchProviderException | InvalidKeyException | 
				SignatureException | IOException e) {
			throw new IonException(e);
		}
	}

	public boolean check(String pubKey, String signature, String data) throws IonException {
		try {
			PublicKey key = pubFromString(pubKey);
			Signature dsa = Signature.getInstance(signAlgo, securityProvider); 			
			dsa.initVerify(key);
			dsa.update(data.getBytes());
			return dsa.verify(base64Decode(signature));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | 
				NoSuchProviderException | InvalidKeyException | SignatureException e) {
			throw new IonException(e);
		}
	}

	public boolean check(String pubKey, String signature, InputStream data) throws IonException {
		try {
			PublicKey key = pubFromString(pubKey);
			Signature dsa = Signature.getInstance(signAlgo, securityProvider); 			
			dsa.initVerify(key);
			BufferedInputStream bufin = new BufferedInputStream(data);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bufin.read(buffer)) >= 0) {
			    dsa.update(buffer, 0, len);
			};
			bufin.close();			
			data.close();
			return dsa.verify(base64Decode(signature));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | 
				NoSuchProviderException | InvalidKeyException | 
				SignatureException | IOException e) {
			throw new IonException(e);
		}
	}

	public String[] createKeyPair() throws IonException {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgo, securityProvider);
			SecureRandom random = SecureRandom.getInstance(randomizeAlgo, securityProvider);
			keyGen.initialize(keySize, random);		
			KeyPair pair = keyGen.generateKeyPair();
			String[] result = new String[2];
			result[0] = privToString(pair.getPrivate());
			result[1] = pubToString(pair.getPublic());
			return result;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
			throw new IonException(e);
		}
	}

	public String getKeyAlgo() {
		return keyAlgo;
	}

	public void setKeyAlgo(String algo) {
		this.keyAlgo = algo;
	}

	public String getSecurityProvider() {
		return securityProvider;
	}

	public void setSecurityProvider(String securityProvider) {
		this.securityProvider = securityProvider;
	}

	public String getRandomizeAlgo() {
		return randomizeAlgo;
	}

	public void setRandomizeAlgo(String randomizeAlgo) {
		this.randomizeAlgo = randomizeAlgo;
	}

	public int getKeySize() {
		return keySize;
	}

	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}

	public String getSignAlgo() {
		return signAlgo;
	}

	public void setSignAlgo(String signAlgo) {
		this.signAlgo = signAlgo;
	}

}
