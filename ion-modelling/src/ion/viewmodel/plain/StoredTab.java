package ion.viewmodel.plain;

import java.util.ArrayList;
import java.util.Collection;

public class StoredTab {

	public String caption;
	
	public Collection<StoredField> fullFields;
	
	public Collection<StoredField> shortFields;
	
	public StoredTab(String caption,Collection<StoredField> fullFields,Collection<StoredField> shortFields) {
		this.caption = caption;
		this.fullFields = fullFields;
		this.shortFields = shortFields;
	}

	public StoredTab(String caption,Collection<StoredField> fullFields) {
		this(caption,fullFields,new ArrayList<StoredField>());
	}

	public StoredTab() {
		this("",new ArrayList<StoredField>());
	}	
}
