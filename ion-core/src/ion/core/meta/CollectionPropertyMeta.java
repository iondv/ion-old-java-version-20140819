package ion.core.meta;

import ion.core.ICollectionPropertyMeta;
import ion.core.IMetaRepository;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class CollectionPropertyMeta extends RelationshipProperty implements ICollectionPropertyMeta {
	private String back_ref;
	
	private String back_coll;
	
	private String bind;
	
	private String itemsClass;
	
	private IMetaRepository mrep;
	
	public CollectionPropertyMeta(String name, String caption, String items_class, String bind, String backref, String backcoll, Integer order_number, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		super(name, caption, MetaPropertyType.COLLECTION, null, null, true, false, false, false, false, hint, null, order_number, null, eager_loading, semantic);
		itemsClass = items_class;
		back_ref = backref;
		back_coll = backcoll;
		this.bind = bind;
		mrep = rep;
	}
	
	public CollectionPropertyMeta(String name, String caption, String items_class, String bind, String backref, String backcoll, boolean eager_loading, String semantic, String hint, IMetaRepository rep){
		this(name, caption, items_class, bind, backref, backcoll, null, eager_loading, semantic, hint, rep);
	}	
	
	public String Binding(){
		return bind;
	}

	
	@Override
	public String BackReference() {
		return back_ref;
	}
	
	@Override
	public String BackCollection() {
		return back_coll;
	}
	
	@Override
	public IStructMeta ItemsClass() throws IonException {
		IStructMeta r = mrep.Get(itemsClass);
		return r;
	}
}
