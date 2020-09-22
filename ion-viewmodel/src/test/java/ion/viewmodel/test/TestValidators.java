package ion.viewmodel.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.viewmodel.com.JSONViewModelRepository;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.IFormTab;
import ion.viewmodel.view.IFormViewModel;

public class TestValidators {
	JSONViewModelRepository repo = new JSONViewModelRepository();
	String dir = "./src/test/java/ion/viewmodel/test/views/";
	String validatorsDir = "./src/test/java/ion/viewmodel/test/validators/";
	IStructMeta meta = new StructMetaMock("Authors", "Authors", "", null, null);
	
	@Test
	public void testValidatorsInsertedIntoFields(){
		IFormViewModel vm = null;
		try {
			repo.setModelsDirectory(parsePath(dir), parsePath(validatorsDir));
			vm = repo.getCreationViewModel("OBED:AFFTAR", meta);
		} catch (IonException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertNotNull(vm);
		List<String> validators = new ArrayList<String>();
		for(IFormTab tab : vm.getTabs()){
			for(IField f :tab.getFullViewFields()){
				for(String v :f.getValidators()){
					System.out.println(v);
					validators.add(v);
				}
			}
		}
		Assert.assertEquals(2,validators.size());
	}
	
	private File parsePath(String path) throws IOException {
		File f = new File(path);
		return f;
	}
	
	public class StructMetaMock implements IStructMeta {
		
		private String name;
		private String caption;
		private String semantic;
		private IStructMeta ancestor;
		private Map<String, IPropertyMeta> property_metas;
		
		public StructMetaMock(String name, String caption, String semantic,
				IStructMeta ancestor, Map<String, IPropertyMeta> property_metas) {
			super();
			this.name = name;
			this.caption = caption;
			this.semantic = semantic;
			this.ancestor = ancestor;
			this.property_metas = property_metas;
		}

		@Override
		public String getName() {return name;}

		@Override
		public String getCaption() {return caption;}

		@Override
		public String Semantic() {return semantic;}

		@Override
		public IStructMeta getAncestor() throws IonException {return ancestor;}

		@Override
		public IStructMeta checkAncestor(String name) throws IonException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IPropertyMeta PropertyMeta(String name) throws IonException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, IPropertyMeta> PropertyMetas() throws IonException {return property_metas;}

		@Override
		public Collection<IStructMeta> Descendants(Boolean direct)
				throws IonException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<IStructMeta> Descendants() throws IonException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String Version() {
			return "0";
		}

	}

}
