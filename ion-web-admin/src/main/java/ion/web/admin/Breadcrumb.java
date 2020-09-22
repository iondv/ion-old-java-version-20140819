package ion.web.admin;

public class Breadcrumb {

	private String caption;
	private String url;
	
	
	
	public Breadcrumb(String caption, String url) {
		super();
		this.caption = caption;
		this.url = url;
	}
	
	
	
	public Breadcrumb() {
		super();
		// TODO Auto-generated constructor stub
	}



	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
