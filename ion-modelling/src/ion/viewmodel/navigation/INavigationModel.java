package ion.viewmodel.navigation;

import ion.core.IonException;

import java.util.Collection;

public interface INavigationModel {
	Collection<INavigationSection> getNavigationSections() throws IonException;
	
	INavigationSection getNavigationSection(String code) throws IonException;
	
	INode getNode(String code) throws IonException;
	
	String getNodeForClassname(String className);
}
