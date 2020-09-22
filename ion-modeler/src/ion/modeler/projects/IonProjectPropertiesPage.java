package ion.modeler.projects;

import ion.modeler.forms.DirChooser;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.osgi.service.prefs.BackingStoreException;

public class IonProjectPropertiesPage extends PropertyPage implements
		IWorkbenchPropertyPage, ModifyListener, SelectionListener {
	
	Combo connectionProfile;
	
	DirChooser deployLocation;
	
	Spinner referenceDepth;
	
	public IonProjectPropertiesPage() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		String currentProfile = "";
		String currentDeploy = "";
		Integer currentRefDepth = 0;
		
		IScopeContext projectScope = new ProjectScope((IProject)this.getElement().getAdapter(IProject.class));
		IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
		if (projectNode != null){
			currentProfile = projectNode.get("connectionProfile","");
			currentDeploy = projectNode.get("deployLocation", "");
			currentRefDepth = projectNode.getInt("referenceDrillDepth", 0);
		}
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm main = toolkit.createScrolledForm(parent);
		main.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		main.getBody().setLayout(layout);
				
		Label label;
		
		GridData leftcol;
		GridData rightcol;
		
		leftcol = new GridData(GridData.FILL,GridData.CENTER,false,false);
		rightcol = new GridData(GridData.FILL,GridData.CENTER,true,false);
		rightcol.grabExcessHorizontalSpace = true;
		rightcol.widthHint = 300;
					
		label = new Label(main.getBody(), SWT.FLAT);
		label.setText("База данных");
		label.setLayoutData(leftcol);
		label.pack();
		
		connectionProfile = new Combo(main.getBody(), SWT.FLAT | SWT.BORDER | SWT.CAP_SQUARE | SWT.SINGLE | SWT.DROP_DOWN);
		IConnectionProfile[] profiles = ProfileManager.getInstance().getProfiles();
		int i = 0;
		int index = -1;
		for (IConnectionProfile profile: profiles){
			connectionProfile.setData(String.valueOf(i), profile.getInstanceID());
			connectionProfile.add(profile.getName(),i);
			if (currentProfile.equals(profile.getInstanceID()))
				index = i;
			i++;
		}		
		if (index >= 0)
			connectionProfile.select(index);
		connectionProfile.setLayoutData(rightcol);
		connectionProfile.addModifyListener(this);
		connectionProfile.pack();		
		
		leftcol = new GridData(GridData.FILL,GridData.CENTER,false,false);
		rightcol = new GridData(GridData.FILL,GridData.CENTER,true,false);
		rightcol.grabExcessHorizontalSpace = true;
		rightcol.widthHint = 300;
					
		label = new Label(main.getBody(), SWT.FLAT);
		label.setText("Среда Ion");
		label.setLayoutData(leftcol);
		label.pack();
		
		deployLocation = new DirChooser(main.getBody());
		deployLocation.setPath(currentDeploy);
		deployLocation.setLayoutData(rightcol);
		deployLocation.addModifyListener(this);
		deployLocation.pack();
		
		label = new Label(main.getBody(), SWT.FLAT);
		label.setText("Глубина разименования ссылочных атрибутов");
		label.setLayoutData(leftcol);
		label.pack();
		
		referenceDepth = new Spinner(main.getBody(), SWT.FLAT | SWT.BORDER | SWT.SINGLE);
		referenceDepth.setValues(currentRefDepth, 0, 10, 0, 1, 10);
		referenceDepth.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER,false,false));
		referenceDepth.addModifyListener(this);
		referenceDepth.pack();		
				
		setValid(false);
		return main;
	}
	
	@Override
    public boolean performOk() {
		IScopeContext projectScope = new ProjectScope((IProject)this.getElement().getAdapter(IProject.class));
		IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
		if (projectNode != null){
			if (connectionProfile.getSelectionIndex() >= 0)
				projectNode.put("connectionProfile", connectionProfile.getData(String.valueOf(connectionProfile.getSelectionIndex())).toString());
			
			projectNode.put("deployLocation", deployLocation.getText());
			projectNode.put("referenceDrillDepth",referenceDepth.getText());
			try {
				projectNode.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
	        return true;
		}
		return false;
    }

	@Override
	public void modifyText(ModifyEvent e) {
		setValid(true);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		setValid(true);
	}	

}
