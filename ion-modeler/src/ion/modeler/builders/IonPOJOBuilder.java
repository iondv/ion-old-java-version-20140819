package ion.modeler.builders;

import ion.core.IonException;
import ion.framework.meta.plain.StoredClassMeta;
import ion.modeler.Activator;
import ion.modeler.Composer;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredNavSection;
import ion.framework.validators.JSONValidator;
import ion.util.sync.HibernateMappingGenerator;
import ion.util.sync.ModelLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

//import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
//import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//import org.eclipse.core.runtime.preferences.IEclipsePreferences;
//import org.eclipse.core.runtime.preferences.IScopeContext;

public class IonPOJOBuilder extends IncrementalProjectBuilder {
	
	public static final String ID = "ion.modeler.builders.IonPOJOBuilder";
	
	HibernateMappingGenerator hbmGenerator = new HibernateMappingGenerator();
	ModelLoader loader = new ModelLoader();

	public IonPOJOBuilder() {
		
	}

/*	
	private void buildPOJO(IResource source, IFolder destination) throws IonBuildException, IOException {
		if (source instanceof IFile){
			if (source.getName().endsWith(".class.json")){
				Composer c = new Composer(source.getProject());
				StoredClassMeta cm = (StoredClassMeta)c.Read(source.getLocation().toString(), StoredClassMeta.class);
				buildPOJO(cm, c, source.getProject(), destination);
			}	
		}
	}
*/	
	
	private void buildPOJO(StoredClassMeta cm, Composer c, IProject p, IFolder dest) throws IonBuildException, IOException {
		/*
		try {
			File mappingDestination = getMappingFileLocation(p);
			IEclipsePreferences prefs = getProjectPreferences(p);
			hbmGenerator.useDiscriminator = prefs.getBoolean("useDiscriminator", false);
			//hbmGenerator.useLazyLoading = prefs.getBoolean("useLazyLoading", false);
			
			hbmGenerator.generateSourceFile(cm, new File(p.getFolder("meta").getLocation().toString()), new File(dest.getLocation().toString()), "ion.domain."+p.getName());
			if (mappingDestination != null)
				hbmGenerator.generateHbmFile(cm, new File(p.getFolder("meta").getLocation().toString()), mappingDestination, "ion.domain."+p.getName());
		} catch (IonException e) {
			throw new IonBuildException(e);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}	
	
	private void forceDirectory(IFolder dir, IProgressMonitor monitor) throws CoreException{
		if (!dir.exists()){
			IResource parent = dir.getParent();
			if (parent instanceof IFolder)
				forceDirectory((IFolder)parent, monitor);
			dir.create(true, true, monitor);
		}
	}
	
	private void processSourceFile(JSONValidator validator, Composer c, IProject p, IFolder dest, IFile f) throws IOException, IonBuildException, CoreException, IonException{
		String[] result = null;
		if (f.getName().endsWith(".class.json")){
			StoredClassMeta cm = (StoredClassMeta)c.Read(f.getLocation().toString(), StoredClassMeta.class);
			result = validator.Validate(cm);
			File metadir = new File(f.getLocation().toPortableString()).getParentFile();
			loader.expandUserTypes(cm, new File(metadir, "types"));
			buildPOJO(cm, c, p, dest);
		} else {
			String folder = f.getProjectRelativePath().segment(0);
			if (folder.equals("navigation")){
				if(f.getName().endsWith(".section.json")){
					StoredNavSection section = (StoredNavSection)c.Read(f.getLocation().toString(), StoredNavSection.class);
					result = validator.Validate(section);
				} else if(f.getProjectRelativePath().segmentCount() > 1) {
  				StoredNavNode node = (StoredNavNode)c.Read(f.getLocation().toString(), StoredNavNode.class);
  				result = validator.Validate(node);
				}
			} else if (folder.equals("views")){
				if (f.getName().endsWith("list.json")){
					StoredListViewModel m = (StoredListViewModel)c.Read(f.getLocation().toString(), StoredListViewModel.class);
					result = validator.Validate(m);
				}
				else
				if (f.getName().endsWith("item.json") || f.getName().endsWith("create.json")){
					StoredFormViewModel m = (StoredFormViewModel)c.Read(f.getLocation().toString(), StoredFormViewModel.class);
					result = validator.Validate(m);
				}			
			}
		}
		
		if (f.exists()) {
			f.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

			IMarker problem;
			if (result != null)
				for (String msg : result) {
					System.out.println(msg);
					problem = f.createMarker(IMarker.PROBLEM);
					problem.setAttribute(IMarker.MESSAGE, msg);
					problem.setAttribute(IMarker.PRIORITY,
							IMarker.PRIORITY_HIGH);
					problem.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_ERROR);
					problem.setAttribute(IMarker.SOURCE_ID, Activator.PLUGIN_ID);
				}
		}
	}
	
	
	private void fullBuild(IProgressMonitor monitor) throws IOException, CoreException, IonBuildException, IonException {
		IProject p = getProject();
		IFolder meta = p.getFolder("meta");
		IFolder dest = p.getFolder("src/ion/domain/" + p.getName());
		forceDirectory(dest, monitor);
		Composer c = new Composer(p);
		final JSONValidator validator = new JSONValidator();
			
		for (IResource f: meta.members()){
			if (f instanceof IFile){
				processSourceFile(validator, c, p, dest, (IFile)f);
			}
		}
		dest.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
	
	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws IOException, CoreException {
		final IProject p = getProject();
		final IFolder dest = p.getFolder("src/ion/domain/" + p.getName());
		forceDirectory(dest, monitor);		
		final Composer c = new Composer(p);	
		final JSONValidator validator = new JSONValidator();

		delta.accept(new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				IResource f = delta.getResource();
				if (f instanceof IFile)
					try {
						processSourceFile(validator, c, p, dest, (IFile) f);
					} catch (IOException | IonBuildException | IonException
							| CoreException e) {
						e.printStackTrace();
					}
				return true;
			}
		});
		dest.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		try {
	        if (kind == IncrementalProjectBuilder.FULL_BUILD) {
	            fullBuild(monitor);
	         } else {
	            IResourceDelta delta = getDelta(getProject());
	            if (delta == null) {
	               fullBuild(monitor);
	            } else {
	               incrementalBuild(delta, monitor);
	            }
	         }	
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Ion build failed!", e));
		}
        return null;
	}
	/*
	private File getMappingFileLocation(IProject p){
		IScopeContext projectScope = new ProjectScope(p);
		IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
		String generateMappings = projectNode.get("generateMappings","");
		if(Boolean.valueOf(generateMappings)){
			return new File(projectNode.get("mappingFileLocation",""));
		}
		return null;
	}
	
	private IEclipsePreferences getProjectPreferences(IProject p){
		IScopeContext projectScope = new ProjectScope(p);
		return projectScope.getNode("ion.modeler");
	}	
	*/
}
