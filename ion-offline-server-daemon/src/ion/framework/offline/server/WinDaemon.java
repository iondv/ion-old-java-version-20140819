package ion.framework.offline.server;

public class WinDaemon {
	
	private static volatile boolean stopped = true;
	
  public static void start(String[] args) {
		Worker worker = null;
		try {
			worker = new Worker();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		
		if (worker != null){
			stopped = false;
			int counter = 0;
			try {
  			while (stopped == false){
  				worker.run();
  				counter++;
  				if (args.length > 0){
  					if ("once".equals(args[0]))
  						break;
  					if (args[0].startsWith("-c")){
  						if (counter >= Integer.parseInt(args[0].replace("-c", "")))
  							break;
  					}
  				}
  				try {
  					Thread.sleep(worker.getInterval()*1000);
  				} catch (InterruptedException e) {
  					e.printStackTrace(System.err);
  				}
  			}
			} catch (Exception e) {
				
			}
		}
  }
 
  public static void stop(String[] args) {
  	stopped = true;
  }
 
  public static void main(String[] args) {
    if (args.length > 0){
    	if ("start".equals(args[0]) || "once".equals(args[0]) || args[0].startsWith("-c"))
  			start(args);
  		else if ("stop".equals(args[0]))
  			stop(args);
  	}
  }
}
