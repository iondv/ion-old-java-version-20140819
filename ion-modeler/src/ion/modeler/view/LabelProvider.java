package ion.modeler.view;

import ion.modeler.Activator;
import ion.modeler.IonIcons;
import ion.modeler.resources.IonEntityResource;
import ion.modeler.resources.IonListViewValidators;
import ion.modeler.resources.IonModelResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonPropertyTemplateResource;
import ion.modeler.resources.IonSectionResource;
import ion.modeler.resources.IonValidatorResource;
import ion.modeler.resources.IonViewsResource;
import ion.modeler.resources.IonUserTypeResource;
import ion.modeler.resources.IonViewResource;
import ion.modeler.resources.IonWorkflowResource;
import ion.modeler.resources.ModelProjectAttrTemplates;
import ion.modeler.resources.ModelProjectMeta;
import ion.modeler.resources.ModelProjectUserTypes;
import ion.modeler.resources.ModelProjectViews;
import ion.modeler.resources.ModelProjectWorkflow;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class LabelProvider implements ILabelProvider {
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof IonEntityResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.ENTITY_ICON);
        else if (element instanceof IonSectionResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.NODE_ICON);
        else if (element instanceof IonNodeResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.NODE_ICON);
        else if (element instanceof IonViewsResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.NODEVIEWS_ICON);
        else if (element instanceof IonViewResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.VIEW_ICON);
        else if (element instanceof ModelProjectMeta) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.META_FOLDER_ICON);
        else if (element instanceof ModelProjectAttrTemplates) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.META_FOLDER_ICON);
        else if (element instanceof ModelProjectViews) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.VIEW_FOLDER_ICON);
        else if (element instanceof ModelProjectUserTypes) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.USERTYPE_FOLDER_ICON);
        else if (element instanceof IonUserTypeResource) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.USERTYPE_ICON);
        else if (element instanceof IonListViewValidators) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.VALIDATION_ICON);
        else if (element instanceof IonValidatorResource) 
        	return Activator.getDefault().getImageRegistry().get(IonIcons.VALIDATION_ICON);
        else if (element instanceof ModelProjectWorkflow)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.WORKFLOWS_ICON);
        else if (element instanceof IonWorkflowResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.WORKFLOW_ICON);
        else if (element instanceof IonPropertyTemplateResource)
        	return Activator.getDefault().getImageRegistry().get(IonIcons.ENTITY_ICON);
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        String text = "";
        if (element instanceof IonModelResource) 
            text = ((IonModelResource)element).toString();
        return text;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
 
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
 
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
 
    }	

}
