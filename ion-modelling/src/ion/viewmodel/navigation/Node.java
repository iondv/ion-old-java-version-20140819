package ion.viewmodel.navigation;

public abstract class Node implements INode {
	
	protected String code;
	
	protected String caption;
	
	protected NodeType type;
	
	protected int orderNumber;
	
	protected String hint;

	public Node(int onum, String code, String caption, NodeType type, String hint){
		this.orderNumber = onum;
		this.code = code;
		this.caption = caption;
		this.type = type;
		this.hint = hint;
	}
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public NodeType getType() {
		return type;
	}
	
	@Override
	public int getOrderNumber(){
		return orderNumber;
	}
	
	@Override
	public String getHint(){
		return hint;
	}
	
	@Override
	public int compareTo(INode node) {
		return this.getOrderNumber() - node.getOrderNumber();
	}
	
}
