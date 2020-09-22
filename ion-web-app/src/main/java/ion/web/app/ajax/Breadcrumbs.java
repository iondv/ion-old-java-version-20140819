package ion.web.app.ajax;

public class Breadcrumbs {
	String	id;
	String	caption;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Breadcrumbs(String id, String caption) {
		super();
		this.id = id;
		this.caption = caption;
	}

	public Breadcrumbs() {
		super();
		// TODO Auto-generated constructor stub
	}
}
