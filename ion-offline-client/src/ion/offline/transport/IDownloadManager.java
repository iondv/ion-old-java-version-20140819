package ion.offline.transport;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface IDownloadManager {
	boolean isStarted();
	void start(int timeout, File destination) throws IOException;
	File download(URL url) throws IOException;
	void stop() throws IOException;
}
