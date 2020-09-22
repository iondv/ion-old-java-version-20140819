package ion.web.app.ajax;

public class NodeClass {
	String	name;
	String	caption;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public NodeClass(String name, String caption) {
		super();
		this.name = name;
		this.caption = caption;
	}

	public NodeClass() {
		super();
		// TODO Auto-generated constructor stub
	}
}
