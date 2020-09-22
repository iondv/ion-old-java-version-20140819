package ion.offline.util;

import java.io.File;
import java.io.IOException;

public interface IVolumeProcessor {
	File[] Split(File src, File dest) throws IOException;
	void Join(File[] volumes, File dest) throws IOException;
}
