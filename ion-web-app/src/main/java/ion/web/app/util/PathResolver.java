package ion.web.app.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import ion.core.IonException;
import ion.framework.meta.IonMetaRepository;
import ion.viewmodel.com.JSONNavigationModel;
import ion.viewmodel.com.JSONViewModelRepository;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

public class PathResolver implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {
	
	IonMetaRepository metaRepository;
	
	JSONNavigationModel navigationModel;
	
	JSONViewModelRepository viewModelRepository;
	
	String metaDirectory;
	
	String navigationDirectory;
	
	String viewDirectory;
	
	String validatorsDirectory;
	
	ApplicationContext appContext;

	public PathResolver(IonMetaRepository metaRepository,
						JSONNavigationModel navigationModel,
						JSONViewModelRepository viewModelRepository,
						String metaDirectory,
						String navigationDirectory,
						String viewDirectory,
						String validatorsDirectory) {
		this.metaRepository = metaRepository;
		
		this.navigationModel = navigationModel;
		
		this.viewModelRepository = viewModelRepository;
		
		this.metaDirectory = metaDirectory;
		
		this.navigationDirectory = navigationDirectory;
		
		this.viewDirectory = viewDirectory;
		
		this.validatorsDirectory = validatorsDirectory;
	}
	
	private File parsePath(String path, ApplicationContext app) throws IOException {
		Path p = FileSystems.getDefault().getPath(path);
		if (p.isAbsolute())
			return p.toFile();
		
		Resource r = app.getResource(path);
		return r.getFile();
	}
	
	public File Resolve(String path) throws IOException{
		if (appContext != null)
			return parsePath(path, appContext);
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		appContext = applicationContext;	
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent){
			try {
				metaRepository.setMetaDirectory(parsePath(metaDirectory,appContext));
			} catch (IOException | IonException e) {
				e.printStackTrace();
			} 
			
			try {
				navigationModel.setModelDirectory(parsePath(navigationDirectory,appContext));
			} catch (IOException | IonException e) {
				e.printStackTrace();
			} 
			
			try {
				viewModelRepository.setModelsDirectory(parsePath(viewDirectory,appContext),parsePath(validatorsDirectory,appContext));
			} catch (IOException | IonException e) {
				e.printStackTrace();
			}
		}
	}
}
