package ion.offline.adapter.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import ion.core.logging.ILogger;
import ion.offline.adapters.SitexServicePortalAdapterFactory;
import ion.offline.server.PackageQueue;
import ion.offline.server.SimpleSyncSession;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class ContextSetup implements ApplicationContextAware {
	
	@Autowired
	private PackageQueue queue;
	
	@Autowired
	private SimpleSyncSession session;

	@Autowired
	private ILogger logger;
	
	@Autowired
	private SitexServicePortalAdapterFactory sitex;
	
	private ApplicationContext context;
	
	private File parsePath(String path, ApplicationContext app) throws IOException {
		Path p = FileSystems.getDefault().getPath(path);
		if (p.isAbsolute())
			return p.toFile();
		
		Resource r = app.getResource(path);
		return r.getFile();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
		try {
			queue.setOutgoingDirectory(parsePath("resources"+File.separator+"files", context));
			queue.setWorkDirectory(parsePath("WEB-INF"+File.separator+"queue"+File.separator+"work", context));
			session.setInputDirectory(parsePath("WEB-INF"+File.separator+"incoming", context));
			sitex.setHashDir(parsePath("WEB-INF"+File.separator+"hash", context).getAbsolutePath());
		} catch (IOException e){
			logger.Error("Ошибка при настройке контекста сервиса!", e);
		}			
	}
}