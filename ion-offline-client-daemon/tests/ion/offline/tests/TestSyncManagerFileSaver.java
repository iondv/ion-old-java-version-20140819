package ion.offline.tests;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Test;

import ion.core.IonException;
import ion.framework.offline.client.OfflineSyncEnvironment;
import ion.offline.filesystem.FileUtils;
import ion.offline.net.DataUnit;

public class TestSyncManagerFileSaver {

	OfflineSyncEnvironment syncEnv;
	String propertyFileLocation = "";
	String testFileLocation = "";
	
	@Test
	public void testFileSaver() throws IonException, ClassNotFoundException, SQLException, PropertyVetoException, IOException{
		
		Properties props = new Properties();
		try {
			File f = new File(propertyFileLocation);
			if (!f.exists())
				throw new IonException("Не найден конфигурационный файл!");
			props.load(new FileInputStream(f));
		} catch (IOException e) {
			throw new IonException(e);
		}
		syncEnv = new OfflineSyncEnvironment(props);
		
		final File testFile = new File(testFileLocation);
		
		DataUnit testDataUnit = new DataUnit("1", "Books", new HashMap<String,Object>(){{
			put("Title", "Test Title");
			put("File", new HashMap<String,Object>(){{
				put("fileName","motherland-calls_905.jpg");
				put("fileSize", testFile.length());
				put("fileType", "file");
				put("content",FileUtils.toBase64String(new FileInputStream(testFile)));
			}});
		}});
		
		syncEnv.acceptDataUnit(testDataUnit);
		
	}
	
}
