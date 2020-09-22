package ion.modeler.deploy;

import ion.modeler.Activator;
import ion.util.sync.ModelDeployer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;

import liquibase.servicelocator.CustomResolverServiceLocator;
import liquibase.servicelocator.PackageScanClassResolver;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;

import com.svcdelivery.liquibase.eclipse.v3.LbPluginClassResolver;

public class DbScriptGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageConsoleStream out = Activator.getConsole().newMessageStream();
		try {
			IStructuredSelection selection = (IStructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			IProject project = (IProject)selection.getFirstElement();
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
			IConnectionProfile connectionProfile = null;
			Connection c = null;
			if (projectNode != null){
				connectionProfile = ProfileManager.getInstance().getProfileByInstanceID(projectNode.get("connectionProfile",""));
				if (connectionProfile != null){
					if (connectionProfile.getConnectionState() != IConnectionProfile.CONNECTED_STATE)
						connectionProfile.connect();
					
					if (connectionProfile.getConnectionState() == IConnectionProfile.CONNECTED_STATE){
						IConnection mc = connectionProfile.createConnection("java.sql.Connection");
						//IManagedConnection mc = connectionProfile.getManagedConnection("java.sql.Connection");
						if (mc != null){
							c = (Connection)mc.getRawConnection();
						}
					}
				}
			}
			
			if (c != null){
				//IEclipsePreferences global_prefs = ConfigurationScope.INSTANCE.getNode();
				//PackageScanClassResolver resolver = new DefaultPackageScanClassResolver();
	
				PackageScanClassResolver resolver = new LbPluginClassResolver(Platform.getBundle("liquibase.core"));
				
				// TODO Deploy required classes from referenced projects
				
				ModelDeployer md = new ModelDeployer(System.getProperty("user.name"), 
						project.getFolder("meta").getLocation().toString(), 
						project.getFolder("patches").getLocation().toString(),
						new CustomResolverServiceLocator(resolver));
				md.setUseDiscriminator(projectNode.getBoolean("useDiscriminator", false));
				String fn = md.BuildScript(c, connectionProfile.getName());
				if (fn != null) {
					project.getFolder("patches").refreshLocal(IResource.DEPTH_INFINITE, null);
					out.println("Скрипт миграции успешно создан!");
				} else
					out.println("База данных соответствует модели. Скрипт миграции не был создан.");
			} else 
				throw new Exception("Не удалось установить подключение к БД!");
		} catch (Exception e){
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(b));
			out.print(b.toString());
			throw new ExecutionException("Ошибка при генерации скрипта миграции!",e);
		}
		return null;
	}
}
