package ion.web.app.digisign;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.digisign.DataForSign;
import ion.core.digisign.DataPart;
import ion.core.digisign.IDigiSignDataProvider;


public class JsonDigiSignDataProvider implements IDigiSignDataProvider {

	Gson gson;
	
	public JsonDigiSignDataProvider(){
		gson = new GsonBuilder()
		.serializeNulls()
		.serializeSpecialFloatingPointValues()	
		.setDateFormat(DateFormat.SHORT)
		.create();		
	}
	
	@Override
	public DataForSign getData(IItem item, String action) throws IonException {
		Map<String, String> data = new LinkedHashMap<String, String>();
		for (Map.Entry<String, IProperty> entry: item.getProperties().entrySet())
			data.put(entry.getKey(),entry.getValue().getString());
		return new DataForSign(new DataPart(Base64.encodeBase64String(gson.toJson(data).getBytes())), new HashMap<String, String>());
	}

	@Override
	public boolean hasData(IStructMeta c, String action) throws IonException {
		return true;
	}

}
