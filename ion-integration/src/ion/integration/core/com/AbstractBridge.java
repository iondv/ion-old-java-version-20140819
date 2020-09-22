package ion.integration.core.com;

import ion.integration.core.ISysBridge;

public abstract class AbstractBridge implements ISysBridge {
	
	private String name;
	
	public AbstractBridge(Externals externals){
		externals.RegisterBridge(this);
	}

	public void setName(String name) {
		this.name = name;
	}
		
	public String getName() {
		return name;
	}
}
