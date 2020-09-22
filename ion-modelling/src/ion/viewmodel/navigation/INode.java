package ion.viewmodel.navigation;

public interface INode extends Comparable<INode> {
	public String getCode();
	public String getCaption();
	public NodeType getType();
	public int getOrderNumber();
	public String getHint();
}
