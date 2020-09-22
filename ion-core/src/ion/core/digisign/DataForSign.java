package ion.core.digisign;

import java.util.Map;

public class DataForSign {
	private DataPart[] parts;
	
	private Map<String, String> attributes;
	
	public DataForSign(DataPart[] parts, Map<String, String> attributes){
		this.parts = parts;
		this.attributes = attributes;
	}
	
	public DataForSign(DataPart part, Map<String, String> attributes){
		this.parts = new DataPart[1];
		this.parts[0] = part;
		this.attributes = attributes;
	}

	public DataForSign(String mimeType, String contents, Map<String, String> attributes){
		this(new DataPart(mimeType, contents), attributes);
	}
	
	public boolean isPartitioned(){
		return parts.length > 1;
	}
	
	public String getMimeType(){
		if (parts.length > 0)
			return parts[0].getMimeType();
		return null;		
	}
	
	public byte[] getContents(){
		if (parts.length > 0)
			return parts[0].getContents();
		return null;
	}
	
	public String getString(){
		return toString();
	}
	
	public String toString(){
		String result = "";
		for (int i = 0; i < parts.length; i++)
			result = result + parts[i].getString();
		return result;
	}
	
	public DataPart[] getParts(){
		return parts;
	}
	
	public Map<String, String> getAttributes(){
		return attributes;
	}
}
