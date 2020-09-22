package ion.modeler.editors;

public class EditorTreeNode {

	public EditorTreeNode Parent;
	
	public EditorTreeNode[] Children;
	
	public EditorTreeNode() {
		
	}
	
	public EditorTreeNode(EditorTreeNode parent) {
		Parent = parent;
	}
}
