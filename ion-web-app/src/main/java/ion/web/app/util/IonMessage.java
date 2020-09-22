package ion.web.app.util;

public class IonMessage {
	private String message;
	private IonMessageType type;

	public IonMessage(String message, IonMessageType type) {
	  super();
	  this.message = message;
	  this.type = type;
  }
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public IonMessageType getType() {
		return type;
	}
	public void setType(IonMessageType type) {
		this.type = type;
	}
	
	
}
