package ion.core.meta;

import java.util.Collection;
import java.util.Map;

import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;

public class StructMeta implements IStructMeta {
	
	private String version;
	
	private String name;
	
	private String caption;
	
	private String semantic = null;
		
	protected IMetaRepository rep;
	
	protected IStructMeta anc;
	
	protected Map<String, IPropertyMeta> propertyMetas;
	
	protected Collection<IStructMeta> descendants = null;
	
	public StructMeta(String name, String version, String caption, String semanticAttr, IMetaRepository repository){
		this.name = name;
		this.version = version;
		this.caption = caption;
		this.rep = repository;
		if (semanticAttr != null && semanticAttr.trim().length() > 0)
			this.semantic = semanticAttr;		
	}
	
	public StructMeta(String name, IMetaRepository repository){
		this(name,"",name,"",repository);
	}
	
	public static IStructMeta EmptyMeta(){
		return new StructMeta("","","","",null);
	}
	
	
	@Override
	public String getCaption() {
		return caption;
	}
	
	@Override
	public String Semantic() {
		return semantic;
	}	
	
	@Override
	public String getName() {
		return name;
	};
	
	@Override
	public IStructMeta getAncestor() throws IonException {
		if (anc == null){
			anc = rep.Ancestor(name);
			if (anc == null){
				anc = EmptyMeta();
				return null;
			}
		} else if (anc.getName().equals(""))
			return null;
		return anc;
			
	}
	
	public IStructMeta checkAncestor(String name) throws IonException {
		if(this.name.equals(name))
			return this;
		IStructMeta parent = getAncestor();
		if(parent != null)
			return parent.checkAncestor(name);
		return null;
	}
	
	@Override
	public Collection<IStructMeta> Descendants(Boolean direct) throws IonException {
		if (direct){
			if (descendants == null)
				descendants = rep.List(getName(), true);
			return descendants;
		}
		return rep.List(getName(), direct);
	}
	
	@Override
	public Collection<IStructMeta> Descendants() throws IonException {
		return Descendants(false);
	}	
		
	@Override
	public Map<String, IPropertyMeta> PropertyMetas() throws IonException {
		if (propertyMetas == null);
			propertyMetas = rep.PropertyMetas(name);
		return propertyMetas;
	}
	
	@Override
	public IPropertyMeta PropertyMeta(String name) throws IonException {
		if (name.contains(".")){
			int dot = name.indexOf(".");
			IPropertyMeta pm = PropertyMeta(name.substring(0, dot));
			if (pm != null){
				IStructMeta next = null;
				if (pm instanceof IReferencePropertyMeta)
					next = ((IReferencePropertyMeta) pm).ReferencedClass();
				
				if (next != null)
					return next.PropertyMeta(name.substring(dot + 1));
			}
		}
		
		if (name.contains("$")){
			int dollar = name.indexOf("$");
			IPropertyMeta pm = PropertyMeta(name.substring(0, dollar));
			if (pm != null){
				IStructMeta next = null;
				if (pm instanceof IStructPropertyMeta)
					next = ((IStructPropertyMeta) pm).StructClass();
				
				if (next != null)
					return next.PropertyMeta(name.substring(dollar + 1));
			}			
		}
		
		if (!PropertyMetas().containsKey(name))
			if (getAncestor() != null)
				return getAncestor().PropertyMeta(name);
		return PropertyMetas().get(name);
	}

	@Override
	public String Version() {
		return version;
	}	
}

