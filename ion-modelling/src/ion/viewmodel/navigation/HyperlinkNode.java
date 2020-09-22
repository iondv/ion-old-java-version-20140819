package ion.viewmodel.navigation;

public class HyperlinkNode extends Node implements IHyperlinkNode {

	private String url;
	
	public HyperlinkNode(int onum, String code, String caption, String url, String hint) {
		super(onum, code, caption, NodeType.HYPERLINK, hint);
		this.url = url;
	}

	@Override
	public String getUrl() {
		return url;
	}
}
