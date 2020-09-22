package ion.web.app.digisign;

public class DataPart {
	String mimeType;
	
	String contents;
	
	public DataPart(ion.core.digisign.DataPart src){
		mimeType = src.getMimeType();
		contents = src.getString();
	}
}
