package ion.modeler.editors;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.forms.IKeyValueProvider;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.view.FieldType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ViewModelEditor extends IonEditor {

	protected String className = "";
	
	protected String nodeName = "";
	
	protected String type = "";
	
	protected Class<?> plainClass;
	
	class PropProvider implements IKeyValueProvider {
		
		MetaPropertyType filter = null;
		
		public PropProvider(MetaPropertyType f){
			filter = f;
		}

		@Override
		public Map<String, String> Provide(Object model) {
			Composer c = getComposer();
			StoredClassMeta cm = null;
			try {
				cm = c.getClass(className);
				return getClassProperties(c, cm, filter, null, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new HashMap<String, String>();
		}
	}
	
	class FieldPropProvider implements IKeyValueProvider {
		
		String collection;
		
		MetaPropertyType filter;
		
		public FieldPropProvider(String coll, MetaPropertyType f){
			collection = coll;
			filter = f;
		}
		
		
		private String getClassContext(EditorTreeNode n, Composer c) throws IOException{
			if (n.Parent == null)
				return className;
			
			if (n instanceof EditorItemNode){
				Object item = ((EditorItemNode) n).Item;
				if (item instanceof StoredField){
					if (
						((StoredField) item).type == FieldType.REFERENCE.getValue()
						||
						((StoredField) item).type == FieldType.COLLECTION.getValue()
					){
						String cn = getClassContext(n.Parent,c);
						String pn = ((StoredField) item).property;
						if (pn != null){
							StoredClassMeta cm = c.getClass(cn);
							while (cm != null){
								for (StoredPropertyMeta pm: cm.properties)
									if (pm.name != null && pm.name.equals(pn)){
										if (pm.type == MetaPropertyType.REFERENCE.getValue() 
												|| pm.type == MetaPropertyType.STRUCT.getValue())
											return pm.ref_class;
										if (pm.type == MetaPropertyType.COLLECTION.getValue())
											return pm.items_class;
									}
								if (cm.ancestor != null && !cm.ancestor.isEmpty())
									cm = c.getClass(cm.ancestor);
								else
									cm = null;
							}
						}
					}
				}
			}
			
			return getClassContext(n.Parent, c); 
		}
		
		
		
		@Override
		public Map<String, String> Provide(Object model) {
			Composer c = getComposer();
			IStructuredSelection selection = collectionSelection(collection);
			if (selection != null && selection.getFirstElement() instanceof EditorItemNode){
				StoredClassMeta cm = null;
				try {
					cm = c.getClass(getClassContext((EditorTreeNode)selection.getFirstElement(), c));
					return getClassProperties(c, cm, filter, null, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return new HashMap<String, String>();
		}		
	}
	
	public ViewModelEditor() {
	}

	@Override
	protected Object loadModel(IFile f) throws IOException {
		Composer c = new Composer(f.getProject());
		IFolder vf = f.getProject().getFolder("views");
		type = f.getName().replace(".json", "");
		IPath p = f.getProjectRelativePath().makeRelativeTo(vf.getProjectRelativePath()).removeLastSegments(1);
		className = p.lastSegment();
		nodeName = p.removeLastSegments(1).toString().replace("/", ".");
		return c.Read(f.getLocation().toString(), plainClass);
	}
	
	protected void performSaving(Composer c) throws IOException, CoreException{
		c.saveViewModel(model, nodeName, className, type);
	}	

	@Override
	protected String mainPageText() {
		return "Параметры";
	}

	@Override
	protected String formPartName() {
		return className+": "+nodeName;
	}

}
