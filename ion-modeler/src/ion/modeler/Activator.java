package ion.modeler;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ion.modeler"; //$NON-NLS-1$
	
	public static final String CONSOLE_ID = "ion.modeler.console";

	// The shared instance
	private static Activator plugin;
	
	private static IAdapterFactory resourceAdapter;
	
	/**
	 * The constructor
	 */
	public Activator() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				getImageRegistry().put(IonIcons.ENTITY_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/entity.png"));
				getImageRegistry().put(IonIcons.META_FOLDER_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/meta.png"));
				getImageRegistry().put(IonIcons.VIEW_FOLDER_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/views.png"));
				getImageRegistry().put(IonIcons.NODE_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/node.png"));
				getImageRegistry().put(IonIcons.VIEW_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/view.png"));
				getImageRegistry().put(IonIcons.NODEVIEWS_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/views.png"));
				getImageRegistry().put(IonIcons.USERTYPE_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/usertype.png"));
				getImageRegistry().put(IonIcons.USERTYPE_FOLDER_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/usertype-folder.png"));
				getImageRegistry().put(IonIcons.VALIDATION_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/valid.png"));
				getImageRegistry().put(IonIcons.WORKFLOWS_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/marketing8.png"));
				getImageRegistry().put(IonIcons.WORKFLOW_ICON, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/social24.png"));
			}
		});
	}	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
			    if (event == null || event.getDelta() == null || event.getType() != IResourceChangeEvent.POST_CHANGE)
			        return;
			    
			    try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						@Override
					    public boolean visit(IResourceDelta delta) throws CoreException {							
					        if (delta.getKind() == IResourceDelta.CHANGED){					        	
					            final IResource resource = delta.getResource();
					            if (resource instanceof IProject) {
					            	IProject p = (IProject)resource;
					            	if ((delta.getFlags() & IResourceDelta.OPEN) != 0){
					            		if (!p.getFolder("attrtpl").exists()){
					            			File f = new File(p.getFolder("attrtpl").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("meta").exists()){
					            			File f = new File(p.getFolder("meta").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("meta/types").exists()){
					            			File f = new File(p.getFolder("meta/types").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("navigation").exists()){
					            			File f = new File(p.getFolder("navigation").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("views").exists()){
					            			File f = new File(p.getFolder("views").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("patches").exists()){
					            			File f = new File(p.getFolder("patches").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("validators").exists()){
					            			File f = new File(p.getFolder("validators").getLocation().toString());
					            			f.mkdirs();
					            		}
					            		if (!p.getFolder("workflows").exists()){
					            			File f = new File(p.getFolder("workflows").getLocation().toString());
					            			f.mkdirs();
					            		}
					            	}
					            	return false;
					            }
					        }
					        return true;
					    }
					});
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}	
    
    public static IAdapterFactory getResourceAdapter(){
    	if (resourceAdapter == null)
    		resourceAdapter = new ResourceAdapter();
    	return resourceAdapter;
    }
    
    public static MessageConsole getConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++)
           if (CONSOLE_ID.equals(existing[i].getName()))
              return (MessageConsole) existing[i];
      
        //no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(CONSOLE_ID, null);
        conMan.addConsoles(new IConsole[]{myConsole});
        return myConsole;
     }
}
