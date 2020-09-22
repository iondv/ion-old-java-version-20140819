package ion.core.storage;

import ion.core.IonException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class SimpleFileStorage implements IFileStorage {
	
	protected String urlPath;
	
	protected String storageRoot;
		
	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
	

	public SimpleFileStorage(){
	}
	
	public String getStorageRoot() {
		return storageRoot;
	}

	public void setStorageRoot(File storageRootFile) {
		if(storageRootFile.exists()){
			this.storageRoot = storageRootFile.getAbsolutePath();
		}
	}
	
	protected String getAcceptDir(){
		Calendar c = new GregorianCalendar();
		String workDir = c.get(Calendar.YEAR) + File.separator 
				+ (c.get(Calendar.MONTH) + 1) + File.separator + c.get(Calendar.DAY_OF_MONTH) + File.separator 
				+ c.get(Calendar.HOUR) + File.separator + c.get(Calendar.MINUTE) + File.separator;
		return workDir;
	}
	
	private File getDest(String fn){
		String wd = getAcceptDir();
		File destdir = new File(storageRoot + File.separator + wd);
		destdir.mkdirs();
		File dest = new File(destdir, fn);
		int i = 0;
		while (dest.exists()){
			i++;
			String nfn = fn;
			if (nfn.contains("."))
				nfn = fn.substring(0, fn.lastIndexOf(".")) + "(" + i + ")" + fn.substring(fn.lastIndexOf("."));
			else
				nfn = nfn + "(" + i + ")";
			dest = new File(destdir, nfn);
		}
		return dest;
	}
	
	@Override
	public String Accept(InputStream content, String name, String contentType) throws IonException {
		return Accept(content,name);
	}

	@Override
	public String Accept(InputStream content, String name) throws IonException {
		FileOutputStream fos;
		try {
			File dest = getDest(name);
			fos = new FileOutputStream(dest);

			int bs = 0;
			byte[] buffer = new byte[1048576];
			
			while ((bs = content.read(buffer)) > 0){
				fos.write(buffer,0, bs);
			}
			fos.close();
			File root = new File(storageRoot);
			return root.toPath().relativize(dest.toPath()).toString();
		} catch (IOException e) {
			throw new IonException(e);
		}
	}

	@Override
	public String Accept(File f) throws IonException {
		try {
			File dest = getDest(f.getName());
			Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			File root = new File(storageRoot);
			return root.toPath().relativize(dest.toPath()).toString();
		} catch (IOException e) {
			throw new IonException(e);
		}
	}

	@Override
	public URL getUrl(String id) throws IonException {
		try {
			return new URL(urlPath+"/"+URLEncoder.encode(id.replace(File.separator, "/"), "UTF-8"));
		} catch(UnsupportedEncodingException | MalformedURLException e) {
			throw new IonException(e);
		}
	}

	@Override
	public File getFile(String id) throws IonException {
		return new File(storageRoot + File.separator + id);
	}

}
