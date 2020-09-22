package ion.offline.net;

import ion.framework.meta.plain.StoredClassMeta;

public class MetaDelta {
	public String[] removed;
	
	public StoredClassMeta[] changes;
	
	public MetaDelta(StoredClassMeta[] ch, String[] del){
		changes = ch;
		removed = del;
	}
	
	public MetaDelta(StoredClassMeta[] ch){
		this(ch, new String[0]);
	}
}
