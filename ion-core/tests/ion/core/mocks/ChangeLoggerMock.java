package ion.core.mocks;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import ion.core.IonException;
import ion.core.logging.ChangelogRecordType;
import ion.core.logging.IChangeLogger;
import ion.core.logging.IChangelogRecord;

public class ChangeLoggerMock implements IChangeLogger {

	@Override
	public void LogChange(String objectClass,
			String objectId) throws IonException {
		// TODO Auto-generated method stub

	}

	@Override
	public void LogChange(ChangelogRecordType type,
			String objectClass, String objectId, Map<String, Object> updates)
			throws IonException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<IChangelogRecord> getChanges(Date since, Date till)
			throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IChangelogRecord> getChanges(Date since)
			throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChangelogRecord[] getChangesArray(Date since, Date till)
			throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChangelogRecord[] getChangesArray(Date since) throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void LogChange(ChangelogRecordType type,
												String masterClass, String masterId, String collection,
												String detailClass, String detailId)
																														throws IonException {
		// TODO Auto-generated method stub
		
	}

}
