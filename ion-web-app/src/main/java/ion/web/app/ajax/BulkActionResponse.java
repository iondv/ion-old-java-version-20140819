package ion.web.app.ajax;

import ion.web.app.util.IonMessage;

import java.util.ArrayList;
import java.util.List;

public class BulkActionResponse extends ActionResponse {

	private List<String> deleteList;
	private List<String> refreshList;
	
	public BulkActionResponse() {
	  super();
	  this.deleteList = new ArrayList<String>();
	  this.refreshList = new ArrayList<String>();
  }
	
	public BulkActionResponse(IonMessage message) {
	  super(message);
  }
	
	public BulkActionResponse(String error) {
	  super(error);
  }
	
	public BulkActionResponse(List<String> deleteList, List<String> refreshList, String redirect) {
	  this.deleteList = deleteList;
	  this.refreshList = refreshList;
	  this.setRedirect(redirect);
  }
	
	public List<String> getDeleteList() {
		return deleteList;
	}
	
	public void setDeleteList(List<String> deleteList) {
		this.deleteList = deleteList;
	}
	
	public List<String> getRefreshList() {
		return refreshList;
	}
	
	public void setRefreshList(List<String> refreshList) {
		this.refreshList = refreshList;
	}
}
