package ion.core;

public class IonException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2209401415069285429L;
	
	public IonException(Throwable e){
		super(e);
	}
	
	public IonException(String message){
		super(message);
	}
	
	public IonException(String message, Throwable e){
		super(message,e);
	}	
}
