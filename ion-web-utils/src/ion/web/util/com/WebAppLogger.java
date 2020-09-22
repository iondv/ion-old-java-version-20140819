package ion.web.util.com;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ion.core.logging.AbstractIonLogger;

public class WebAppLogger extends AbstractIonLogger implements ApplicationContextAware {
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		Initialize(applicationContext.getClass());
	}
}
