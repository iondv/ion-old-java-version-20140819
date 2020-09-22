package ion.core.logging;

import java.util.Date;
import java.util.Map;

public interface IChangelogRecord {
	ChangelogRecordType getType();
	
	String getActor();
	
	Date getTime();
	
	String getObjectClass();
	
	String getObjectId();
	
	Map<String, Object> getAttributeUpdates();
}
