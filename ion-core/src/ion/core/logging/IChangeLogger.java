package ion.core.logging;

import ion.core.IonException;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public interface IChangeLogger {
	
	public void LogChange(String objectClass, String objectId) throws IonException;
	
	public void LogChange(ChangelogRecordType type, String objectClass, String objectId, Map<String, Object> updates) throws IonException;

	public Iterator<IChangelogRecord> getChanges(Date since, Date till) throws IonException;

	public Iterator<IChangelogRecord> getChanges(Date since) throws IonException;

	public IChangelogRecord[] getChangesArray(Date since, Date till) throws IonException;

	public IChangelogRecord[] getChangesArray(Date since) throws IonException;
}
