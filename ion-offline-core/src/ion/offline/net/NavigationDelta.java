package ion.offline.net;

import ion.viewmodel.plain.StoredNavNode;

public class NavigationDelta {
	public String[] unavailable;
	
	public StoredNavNode[] changes;
	
	public NavigationDelta(StoredNavNode[] ch, String[] del){
		changes = ch;
		unavailable = del;
	}
	
	public NavigationDelta(StoredNavNode[] ch){
		this(ch, new String[0]);
	}
}
