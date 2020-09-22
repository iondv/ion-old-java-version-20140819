package ion.offline.util;

import java.util.Properties;

import ion.core.IonException;

public interface ITargetSystemAdapterFactory {

	ITargetSystemAdapter getAdapter() throws IonException;
	
	public void setup(Properties properties) throws IonException;
}
