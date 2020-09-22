package ion.modeler.editors;

public class EditorItemNode extends EditorTreeNode {
	public Object Item;

	public EditorItemNode(Object item, EditorTreeNode parent) {
		super(parent);
		Item = item;
	}
}
