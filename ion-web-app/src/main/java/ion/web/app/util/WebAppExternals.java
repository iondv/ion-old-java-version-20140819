package ion.web.app.util;

import org.springframework.context.SmartLifecycle;

import ion.integration.core.com.Externals;

public class WebAppExternals extends Externals implements SmartLifecycle {

	private boolean connected = false;
	
	private boolean enabled = false;

	@Override
	public void start() {
		connected = true;
		Connect();
	}

	@Override
	public void stop() {
		connected = false;
		Disconnect();
	}

	@Override
	public boolean isRunning() {
		return connected;
	}

	@Override
	public int getPhase() {
		return 1;
	}

	@Override
	public boolean isAutoStartup() {
		return enabled;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
