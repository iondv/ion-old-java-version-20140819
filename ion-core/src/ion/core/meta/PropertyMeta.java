package ion.core.meta;

import ion.core.IPropertyMeta;
import ion.core.ISelectionProvider;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class PropertyMeta implements IPropertyMeta{
	String _caption;
	
	String _name;
	
	MetaPropertyType _type;
	
	Short _size;
	
	Short _decimals;
	
	Boolean _nullable;
	
	Boolean _read_only;
	
	Boolean _indexed;
	
	Boolean _unique;
	
	Boolean _autoassigned;
	
	Object _default;
	
	String _hint;
	
	ISelectionProvider _selection;
	
	Integer _order_number;
	
	Boolean _index_search;
	
	String _formula;
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Short decimals, Boolean nullable, Boolean read_only, Boolean indexed, Boolean unique, Boolean autoassigned, String hint, Object dflt, ISelectionProvider selection, Integer order_number, Boolean is, String formula){
		_name = name;
		_caption = caption;
		_type = type;
		_size = size;
		_decimals = decimals;
		_nullable = nullable;
		_read_only = read_only;
		_indexed = indexed;
		_unique = unique;
		_autoassigned = autoassigned;
		_default = dflt;
		_selection = selection;
		_order_number = order_number;
		_index_search = is;
		_hint = hint;
		_formula = formula;
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Short decimals, Boolean nullable, Boolean read_only, Boolean indexed, Boolean unique, String hint, Object dflt){
		this(name,caption,type,size,decimals,nullable,read_only,indexed,unique,false,hint,dflt,null,0,null,null);
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Short decimals, Boolean nullable, Boolean read_only, Boolean indexed, Boolean unique, Boolean autofilled, String hint, Object dflt, Integer order_number, Boolean is){
		this(name,caption,type,size,decimals,nullable,read_only,indexed,unique,autofilled,hint,dflt,null,order_number,is,null);
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Short decimals, Boolean read_only){
		this(name,caption,type,size,decimals,true,read_only,false,false,false,null,null,null,0,null,null);
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Boolean read_only){
		this(name,caption,type,size,null,true,read_only,false,false,null,null);
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size, Short decimals){
		this(name,caption,type,size,decimals,true,false,false,false,null,null);
	}	
	
	public PropertyMeta(String name, String caption, MetaPropertyType type, Short size){
		this(name,caption,type,size,null,true,false,false,false,null,null);
	}
	
	public PropertyMeta(String name, String caption, MetaPropertyType type){
		this(name,caption,type,null,null,true,false,false,false,null,null);
	}	

	public PropertyMeta(String name, MetaPropertyType type, Short size){
		this(name,name,type,size,null,true,false,false,false,null,null);
	}	

	@Override
	public String Caption() {
		return _caption;
	}
	
	@Override
	public String Name() {
		return _name;
	}
	
	@Override
	public MetaPropertyType Type() {
		return _type;
	}
	
	@Override
	public Short Size() {
		return _size;
	}
	
	@Override
	public Short Decimals() {
		return _decimals;
	}
	
	@Override
	public Boolean Nullable() {
		return _nullable;
	}
	
	@Override
	public Boolean ReadOnly() {
		return _read_only;
	}
	
	public Object DefaultValue(){
		return _default;
	}
	
	
	@Override
	public ISelectionProvider Selection() throws IonException {
		return _selection;
	}
	
	public void SetSelection(ISelectionProvider provider) {
		_selection = provider;
	}

	@Override
	public Boolean Unique() {
		return _unique;
	}

	@Override
	public Boolean Indexed() {
		return _indexed;
	}

	@Override
	public Integer OrderNumber() {
		return _order_number;
	}

	@Override
	public Boolean AutoAssigned() {
		return _autoassigned;
	}
	
	@Override
	public String Hint() {
		return _hint;
	}

	@Override
	public Boolean IndexSearch() {
		return (_index_search == null)?false:_index_search;
	}

	@Override
	public String Formula() {
		return _formula;
	}
}
