package ion.modeler.deploy;

import ion.modeler.Activator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;

public class WebDeployHandler extends AbstractHandler {
	
	private String hibernateDialect(String product){
		if (product.equalsIgnoreCase("mysql"))
			return "org.hibernate.dialect.MySQL5Dialect";
		else if (product.equalsIgnoreCase("oracle"))
			return "org.hibernate.dialect.Oracle10gDialect";
		else if (product.equalsIgnoreCase("microsoft sql server") || product.equalsIgnoreCase("SQLOLEDB"))
			return "org.hibernate.dialect.SQLServer2008Dialect";
		else if (product.equalsIgnoreCase("firebird"))
			return "org.hibernate.dialect.FirebirdDialect";
		else if (product.equalsIgnoreCase("db2"))
			return "org.hibernate.dialect.DB2Dialect";
		else if (product.equalsIgnoreCase("postgresql"))
			return "org.hibernate.dialect.PostgresPlusDialect";		
		return "";
	}
	
	private void deployProject(IProject project, String deployLocation) throws IOException{
		File src = new File(project.getFolder("meta").getLocation().toString());
		File dst = new File(deployLocation+"/meta");
		if (dst.exists()) FileUtils.cleanDirectory(dst);
		if (src.exists()) FileUtils.copyDirectory(src, dst);
		
		src = new File(project.getFolder("navigation").getLocation().toString());
		dst = new File(deployLocation+"/navigation");
		if (dst.exists()) FileUtils.cleanDirectory(dst);
		if (src.exists()) FileUtils.copyDirectory(src, dst);

		src = new File(project.getFolder("views").getLocation().toString());
		dst = new File(deployLocation+"/view");
		if (dst.exists()) FileUtils.cleanDirectory(dst);
		if (src.exists()) FileUtils.copyDirectory(src, dst);		
		
		src = new File(project.getFolder("validators").getLocation().toString());
		dst = new File(deployLocation+"/validators");
		if (dst.exists()) FileUtils.cleanDirectory(dst);
		if (src.exists()) FileUtils.copyDirectory(src, dst);
		
		src = new File(project.getFolder("workflows").getLocation().toString());
		dst = new File(deployLocation+"/workflows");
		if (dst.exists()) FileUtils.cleanDirectory(dst);
		if (src.exists()) FileUtils.copyDirectory(src, dst);		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageConsoleStream out = Activator.getConsole().newMessageStream();
		IStructuredSelection selection = (IStructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		IProject project = (IProject)selection.getFirstElement();
		String lf = "\n";
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			lf = "\r\n";
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
			if (projectNode != null){
				IConnectionProfile connectionProfile = ProfileManager.getInstance().getProfileByInstanceID(projectNode.get("connectionProfile",""));
				String deployLocation = projectNode.get("deployLocation","");
				Writer w;
				if (connectionProfile != null){
					
					if (connectionProfile.getConnectionState() != IConnectionProfile.CONNECTED_STATE)
						connectionProfile.connect();
					
					if (connectionProfile.getConnectionState() == IConnectionProfile.CONNECTED_STATE){
						IConnection mc = connectionProfile.createConnection("java.sql.Connection");
						String product = "";
						if (mc != null){
							Connection c = (Connection)mc.getRawConnection();
							product = c.getMetaData().getDatabaseProductName();
						}						
					
						//Properties dbprops = connectionProfile.getBaseProperties();
						File dbProps = new File(deployLocation+"/WEB-INF/db.properties");
						w = new OutputStreamWriter(new FileOutputStream(dbProps),"UTF-8");
						w.write("db.dialect=" + hibernateDialect(product) + lf);//$NON-NLS-1$
						w.write("jndi.auth=auth" + lf);//$NON-NLS-1$
						w.write("jndi.db=" + connectionProfile.getName() + lf);//$NON-NLS-1$
						w.flush();
						w.close();
					} else throw new ExecutionException("Can not connect to database!");
				}
				
				// String pkg = "ion.domain."+project.getName();
				File f = new File(deployLocation+"/WEB-INF/model.properties");
				String config = "";
				if (f.exists()){
					FileInputStream fi = new FileInputStream(f);
					config = IOUtils.toString(fi)/*.replaceAll("domain.package=\\w+(\\.\\w+)*", "domain.package=" + pkg)*/;
					fi.close();
				} else {
					config = "paths.metaDirectory=meta"+lf+"paths.navigationDirectory=navigation"+lf+"paths.viewDirectory=view"+lf+"paths.validatorsDirectory=validators"+lf+"wf.directory=workflows";		
				}
				
				w = new OutputStreamWriter(new FileOutputStream(deployLocation+"/WEB-INF/model.properties"),"UTF-8");
				w.write(config);
				w.flush();
				w.close();
				
				deployProject(project, deployLocation);
				for (IProject rp: project.getReferencedProjects())
					deployProject(rp, deployLocation);
			
				out.println("deploy complete");
			}
		} catch (Exception e) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(b));
			out.print(b.toString());
			throw new ExecutionException("Не удалось развернуть модель!", e);
		} 		
		return null;
	}
}
