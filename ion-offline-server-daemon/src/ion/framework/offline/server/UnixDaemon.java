package ion.framework.offline.server;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class UnixDaemon implements Daemon {
	
	Worker worker;
	
	private static volatile boolean stopped = true;

	@Override
	public void destroy() {
		worker = null;
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		worker = new Worker();
	}

	@Override
	public void start() throws Exception {
		if (worker != null){
			stopped = false;
			try {
				while (stopped == false){
					worker.run();
					Thread.sleep(worker.getInterval()*1000);
				}
			} catch (Exception e) {
				throw e;
			}
		}

	}

	@Override
	public void stop() throws Exception {
		stopped = true;
	}

}
