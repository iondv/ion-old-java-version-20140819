package ion.web.app.digisign;

import java.util.Map;

public class SignatureData {
	String id;
	
	String action;
	
	Map<String, String> attributes;
	
	DataPart[] parts;
	
	String[] signatures;
}
