package ion.modeler.projects;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;

import ion.modeler.natures.ProjectNature;
 
public class ProjectManager {

	public ProjectManager() {
	}
	
    /**
     * For this marvelous project we need to:
     * - create the default Eclipse project
     * - add the custom project nature
     * - create the folder structure
     *
     * @param projectName
     * @param location
     * @param natureId
     * @return
     */
    public static IProject createProject(String projectName, URI location) {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0);
 
        IProject project = createBaseProject(projectName, location);
        try {
            addNature(project);
            String[] paths = { "attrtpl", "meta", "meta/types", "navigation", "views", "patches", "validators", "workflows" };
            addToProjectStructure(project, paths);
            addJPA(project);
        } catch (CoreException e) {
            e.printStackTrace();
            project = null;
        } catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
        return project;
    }
 
    /**
     * Just do the basics: create a basic project.
     *
     * @param location
     * @param projectName
     */
    private static IProject createBaseProject(String projectName, URI location) {
        // it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 
        if (!newProject.exists()) {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            desc.setNatureIds(new String[] {
            		JavaCore.NATURE_ID, 
            		"org.eclipse.wst.common.project.facet.core.nature",
            		"com.svcdelivery.liquibase.eclipse"
            		}
            );
            ICommand[] commands = new ICommand[] { desc.newCommand(), desc.newCommand(), desc.newCommand() };
            commands[0].setBuilderName(org.eclipse.jdt.core.JavaCore.BUILDER_ID);
            commands[1].setBuilderName("org.eclipse.wst.common.project.facet.core.builder");
            commands[2].setBuilderName("com.svcdelivery.liquibase.eclipse.LiquibaseBuilder");
            desc.setBuildSpec(commands);  
            
            
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
                projectLocation = null;
            }
 
            desc.setLocationURI(projectLocation);
            try {
                newProject.create(desc, null);
                
                
                
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
                IFolder srcFolder = newProject.getFolder(new Path("src"));
                srcFolder.create(false, true, new NullProgressMonitor());
                IJavaProject javaProject = JavaCore.create(newProject);
                IClasspathEntry src = JavaCore.newSourceEntry(srcFolder.getFullPath());
                IClasspathEntry jre = JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER), new IAccessRule[0], new IClasspathAttribute[] { JavaCore.newClasspathAttribute("owner.project.facets", "java")}, false);
                IClasspathEntry[] entries = new IClasspathEntry[] { src, jre };
                javaProject.setRawClasspath(entries, newProject.getFullPath().append("bin"), new NullProgressMonitor());              
                
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
 
        return newProject;
    }
 
    private static void createFolder(IFolder folder) throws CoreException {
        IContainer parent = folder.getParent();
        parent.refreshLocal(IContainer.DEPTH_ONE, null);
        if (parent instanceof IFolder) {
            createFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            folder.create(false, true, null);
        }
    }
 
    /**
     * Create a folder structure with a parent root, overlay, and a few child
     * folders.
     *
     * @param newProject
     * @param paths
     * @throws CoreException
     */
    private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
        for (String path : paths) {
            IFolder etcFolders = newProject.getFolder(path);
            createFolder(etcFolders);
        }
    }
 
    private static void addNature(IProject project) throws CoreException {
        if (!project.hasNature(ProjectNature.NATURE_ID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = ProjectNature.NATURE_ID;
            description.setNatureIds(newNatures);
            IProgressMonitor monitor = null;
            project.setDescription(description, monitor);
        }
    }
    
    private static void addJPA(IProject project) throws JavaModelException, MalformedURLException, URISyntaxException, IOException{
    	IJavaProject jp = JavaCore.create(project);
		IClasspathEntry[] cp = jp.getRawClasspath();
		IClasspathEntry[] classpath = new IClasspathEntry[cp.length + 2]; 
		System.arraycopy(cp, 0, classpath, 2, cp.length);
		
		Bundle me = Platform.getBundle("ion.modeler");
		URL url = me.getEntry("lib/hibernate-jpa-2.1-api-1.0.0.Final.jar");
		File f = new File(FileLocator.resolve(url).toURI());
		classpath[0] = JavaCore.newLibraryEntry(new Path(f.getAbsolutePath()),null,null,false);
		url = me.getEntry("lib/hibernate-annotations-3.5.6-Final.jar");
		f = new File(FileLocator.resolve(url).toURI());
		classpath[1] = JavaCore.newLibraryEntry(new Path(f.getAbsolutePath()),null,null,false);
		jp.setRawClasspath(classpath, null);
		
    }

}
