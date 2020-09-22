package ion.web.app.ajax;

import ion.core.IonException;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;

import java.util.HashMap;
import java.util.Map;

public class ActionResponse {

	private IonMessage	message;
	private String	redirect;
	private Object	item;

	public ActionResponse() {
		this.item = new HashMap<String, Object>();
	}
	
	public ActionResponse(String error){
		this.message = new IonMessage(error, IonMessageType.ERROR); 
	}
	
	public ActionResponse(IonMessage message){
		this.message = message;
	}
	
	public ActionResponse(Object object, String redirect){
		this.item = object;
		this.redirect = redirect;
	}
	
	public ActionResponse(Map<String, Object> data, String redirect) throws IonException{
		this.item = data;
		this.redirect = redirect;
	}
	
	public ActionResponse(Object object, String redirect, IonMessage message) throws IonException{
		this.item = object;
		this.redirect = redirect;
		this.message = message;
	}
	
	public ActionResponse(Map<String, Object> data, String redirect, IonMessage message) throws IonException{
		this.item = data;
		this.redirect = redirect;
		this.message = message;
	}

	public IonMessage getMessage() {
		return message;
	}

	public void setMessage(IonMessage message) {
		this.message = message;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

}
