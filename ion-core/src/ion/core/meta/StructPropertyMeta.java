package ion.core.meta;

import ion.core.IMetaRepository;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class StructPropertyMeta extends PropertyMeta implements IStructPropertyMeta {
	
	private String _sc;
	
	private IMetaRepository _rep;
	
	public StructPropertyMeta(String name, String caption, String struct_class, Boolean read_only,  Integer order_number, String hint, IMetaRepository rep){
		super(name,caption,MetaPropertyType.STRUCT,null,null,false,read_only,true,false,false,hint,null,order_number,null);
		_sc = struct_class;
		_rep = rep;
	}
	
	public StructPropertyMeta(String name, String struct_class, Boolean read_only, String hint, IMetaRepository rep){
		this(name,name,struct_class,read_only,0,hint,rep);
	}
	
	public StructPropertyMeta(String name, String caption, String struct_class, String hint, IMetaRepository rep){
		this(name,caption,struct_class,false,0,hint,rep);
	}	
	
	@Override
	public IStructMeta StructClass() throws IonException {
		return _rep.Get(_sc);
	}
}
