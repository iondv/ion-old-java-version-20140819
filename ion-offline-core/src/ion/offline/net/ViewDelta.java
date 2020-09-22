package ion.offline.net;

import java.util.Map;

import ion.viewmodel.plain.StoredViewModel;

public class ViewDelta {
	public String[] deprecated;
	
	public Map<String, StoredViewModel> models;
	
	public ViewDelta(Map<String, StoredViewModel> ch, String[] del){
		models = ch;
		deprecated = del;
	}
	
	public ViewDelta(Map<String, StoredViewModel> ch){
		this(ch, new String[0]);
	}
}
