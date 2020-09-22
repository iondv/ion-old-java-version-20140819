package ion.offline.util;

import java.util.Iterator;
import java.util.Map;

import ion.offline.net.DataDelta;
import ion.offline.net.DataUnit;
import ion.offline.net.MetaDelta;
import ion.offline.net.NavigationDelta;
import ion.offline.net.UserProfile;
import ion.offline.net.ViewDelta;

public class SyncDelta {
	public UserProfile[] profiles;
	
	public MetaDelta meta;
	
	public NavigationDelta navigation;
	
	public ViewDelta view;
	
	public DataDelta data;
	
	public Map<String, Iterator<DataUnit>> aux;
	
	public SyncDelta(UserProfile[] p, MetaDelta m, NavigationDelta n, ViewDelta v, DataDelta d, Map<String, Iterator<DataUnit>> aux){
		profiles = p;
		meta =  m;
		navigation = n;
		view = v;
		data = d;
		this.aux = aux;
	}
}
