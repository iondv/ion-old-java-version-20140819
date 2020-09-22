package ion.offline.transport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class UrlDownloadManager implements IDownloadManager {

	private File destination;
	
	private int timeout;
	
	private boolean started = false;
	
	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public void start(int timeout, File destination) throws IOException {
		this.destination = destination;
		this.destination.mkdirs();
		this.timeout = timeout;
		started = true;
	}

	@Override
	public File download(URL url) throws IOException {
		String fn = new File(url.toString()).getName();		
		File result = new File(destination,fn);
		InputStream is = null;
		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		URLConnection c = null;
		try {
			fos = new FileOutputStream(result);
			c = url.openConnection();
			c.setConnectTimeout(timeout*1000);
			c.setReadTimeout(timeout*1000);
			is = c.getInputStream();
			rbc = Channels.newChannel(is);
			fos.getChannel().transferFrom(rbc, 0, c.getContentLength());
		} catch (IOException e){
			throw e;
		} finally {
			try {
				if (rbc != null)
					rbc.close();
			} catch (IOException e){
			}
			
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {				
			}
			
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
			}
		}
		return result;
	}

	@Override
	public void stop() throws IOException {
		started = false;
	}

}
