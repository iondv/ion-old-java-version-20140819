package ion.web.app.digisign;

import ion.core.digisign.DataForSign;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataForSigning {
	DataPart[] parts;
	
	Map<String, String> attributes;
	
	public DataForSigning(DataForSign src){
		List<DataPart> prts = new LinkedList<DataPart>();
		for (ion.core.digisign.DataPart dataPart : src.getParts()) {
			prts.add(new DataPart(dataPart));
		}
		parts = prts.toArray(new DataPart[prts.size()]);
		
		attributes = src.getAttributes();
	}
}
