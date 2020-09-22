package ion.core.meta;

import ion.core.IClassMeta;
import ion.core.IMetaRepository;
import ion.core.IReferencePropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class ReferencePropertyMeta extends RelationshipProperty implements IReferencePropertyMeta {
	
	private String rc;
	
	private IMetaRepository mrep;
	
	public ReferencePropertyMeta(String name, String caption, String refclass, Boolean nullable, Boolean read_only, Boolean unique, Integer order_number, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		super(name,caption,MetaPropertyType.REFERENCE,null,null,nullable,read_only,true,unique,false,hint,null,order_number,null, eager_loading, semantic);
		rc = refclass;
		mrep = rep;
	}
	
	public ReferencePropertyMeta(String name, String refclass, Boolean nullable, Boolean read_only, Boolean unique, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		this(name,name,refclass,nullable,read_only,unique,0,eager_loading,semantic,hint,rep);
	}
	
	public ReferencePropertyMeta(String name, String caption, String refclass, Boolean nullable, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		this(name,caption,refclass,nullable,false,false,0, eager_loading, semantic, hint,rep);
	}	
	
	public ReferencePropertyMeta(String name, String caption, String refclass, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		this(name,caption,refclass,true,false,false,0, eager_loading, semantic, hint, rep);
	}	
	
	@Override
	public IClassMeta ReferencedClass() throws IonException {
		return (IClassMeta)mrep.Get(rc);
	}
}
