package ion.offline.util;

import java.io.IOException;

import ion.core.IonException;

public interface ITargetSystemRequestor {
	void QueuePackages() throws IonException, IOException;
}
