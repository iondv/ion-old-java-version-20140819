package ion.framework.offline.client;

import ion.offline.util.client.IDaemonStateAware;

public class WinDaemon {
	
	private static volatile boolean stopped = true;
	
    public static void start(String[] args) {
		Worker worker = null;
		try {
			worker = new Worker(new IDaemonStateAware() {
				@Override
				public boolean isStopped() {
					return stopped;
				}
			});
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		if (worker != null){
			stopped = false;
			Integer count = null;
			if (args.length > 1){
				count = Integer.parseUnsignedInt(args[1]);
			} else if("once".equals(args[0]))
				count = 1;
			int i = 1;
			while (stopped == false){
				worker.run();
				if (count != null)
					if (i >= count){
						stopped = true;
						break;
					}
				i++;	
				try {
					Thread.sleep(worker.getInterval()*1000);
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
				}
			}
			worker.stop();
		}
    }
 
    public static void stop(String[] args) {
    	stopped = true;
    }
 
    public static void main(String[] args) {
    	if (args.length > 0){
    		if ("start".equals(args[0]))
    			start(args);
    		else if ("stop".equals(args[0]))
    			stop(args);
    		else if ("once".equals(args[0]))
    			start(args);
    	}
    }
}
