package ion.offline.net;

import java.util.Iterator;

public class DataDelta {
	public String[] deleted;
	
	public Iterator<DataUnit> changes;
	
	public DataDelta(Iterator<DataUnit> ch, String[] del){
		changes = ch;
		deleted = del;
	}
	
	public DataDelta(Iterator<DataUnit> ch){
		this(ch, new String[0]);
	}
}
