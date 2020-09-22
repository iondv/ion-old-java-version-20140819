package ion.web.app.util;

import ion.core.IonException;
import java.util.HashMap;
import java.util.Map;

public class JSONResponse {

	private IonMessage message;
	
	private Object data;
	
	public JSONResponse() {
		data = new HashMap<String, Object>();
	}
	
	public JSONResponse(String error) {
		this();
		this.message = new IonMessage(error, IonMessageType.ERROR);
	}	
	
	public JSONResponse(IonMessage message) {
		this();
		this.message = message;
	}

	public JSONResponse(Object d) {
		this.data = d;
	}	
	
	public JSONResponse(Object d, IonMessage message) {
		this.data = d; 
		this.message = message;
	}	
	
	public JSONResponse(Map<String, Object> data) throws IonException {
		this.data = data; 
	}
	
	public JSONResponse(Map<String, Object> data, IonMessage message) throws IonException {
		this.data = data;
		this.message = message;
	}

	public IonMessage getMessage() {
		return message;
	}

	public void setMessage(IonMessage message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
