package ion.viewmodel.plain;

public class StoredListAction extends StoredAction {

	public boolean isBulk = false;

	public StoredListAction() {
	  super();
  }

	public StoredListAction(String id, String caption, boolean needSelectedItem, boolean isBulk) {
	  super(id, caption, null, null, needSelectedItem, false, false);
	  this.isBulk = isBulk;
  }
	
	
	
}
