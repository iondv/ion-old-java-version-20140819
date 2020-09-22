package ion.modeler.builders;

public class IonBuildException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1498723330451328810L;
	
	public IonBuildException(String message) {
		super(message);
	}
	
	public IonBuildException(Throwable e) {
		super(e);
	}
		
	@Override
	public String getMessage() {
		return "Ion build error: "+super.getMessage();
	}

}
