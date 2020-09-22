package ion.offline.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.binary.Base64;

public class FileUtils {
	public static void delete(File folder){
		if (folder.isDirectory()){
			for (File f: folder.listFiles())
				delete(f);
		}
		folder.delete();
	}
	
	private static void move_req(File dest, File base, File src) throws IOException{
		Path rel = base.toPath().relativize(src.toPath());
		File newFile = new File(dest,rel.toString());
		if (src.isDirectory()){
			newFile.mkdirs();
			for (File f: src.listFiles())
				move_req(dest, base, f);
			FileUtils.delete(src);
		} else {
			Files.move(src.toPath(), newFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
		} 
	}
	
	
	public static void move(File dest, File src) throws IOException{
		move_req(dest,src.getParentFile(),src);
	}
	
	public static String toBase64String(InputStream is) throws IOException {
		if(is != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while((bytesRead = is.read(buffer, 0, buffer.length)) != -1){
				baos.write(buffer, 0, bytesRead);
			}
			is.close();
			return Base64.encodeBase64String(baos.toByteArray());
		}
		return null;
	}
}
