package ion.offline.net;

public class ActionResult {
	public boolean success;
	
	public String description;
	
	public ActionResult(){
		success = true;
		description = "";
	}
	
	public ActionResult(String desc){
		success = false;
		description = desc;
	}
	
}
