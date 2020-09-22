package ion.modeler.editors;

public class EditorItemGroup extends EditorTreeNode {

	public String Type;
	
	public String Text;
	
	public EditorItemGroup(String type, String text, EditorTreeNode parent) {
		super(parent);
		Type = type;
		Text = text;
	}

	public EditorItemGroup(String type, EditorTreeNode parent) {
		this(type,type,parent);
	}	
}
