package ion.core.digisign;

public class DataPart {
	private String mimeType;
	
	private byte[] contents;
	
	public DataPart(String mimeType, byte[] contents){
		this.mimeType = mimeType;
		this.contents = contents;
	}
	
	public DataPart(String mimeType, String contents){
		this.mimeType = mimeType;
		this.contents = contents.getBytes();
	}
	
	public DataPart(byte[] contents){
		this("text/plain",contents);
	}
	
	public DataPart(String contents){
		this("text/plain",contents);
	}
	
	public String getMimeType(){
		return mimeType;
	}
	
	public byte[] getContents(){
		return contents;
	}
	
	public String getString(){
		return new String(this.contents);
	}
}
