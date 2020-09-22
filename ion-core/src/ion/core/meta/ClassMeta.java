package ion.core.meta;

import ion.core.CompositeIndex;
import ion.core.HistoryMode;
import ion.core.IClassMeta;
import ion.core.IMetaRepository;
import ion.core.IStructMeta;

public class ClassMeta extends StructMeta implements IClassMeta {
	
	private String[] key = null;
		
	private String container = null;
	
	private String createTracker = null;
	
	private String changeTracker = null;
	
	private HistoryMode history = HistoryMode.NONE;
	
	private boolean journaling = false;
	
	private CompositeIndex[] compositeIndexes = null;
		
	public ClassMeta(String[] keyAttr, String name, String version, String caption, 
	                 String semantic, IMetaRepository repository, String containerAttr, 
	                 String createTracker, String changeTracker, HistoryMode history, 
	                 boolean journaling, CompositeIndex[] compositeIndexes){
		super(name,version,caption,semantic,repository);
		if (keyAttr != null && keyAttr.length > 0)
			this.key = keyAttr;
		if (containerAttr != null && containerAttr.trim().length() > 0)
			container = containerAttr;
		this.createTracker = createTracker;
		this.changeTracker = changeTracker;
		this.history = history;
		this.journaling = journaling;
		this.compositeIndexes = compositeIndexes;
	}
	
	public ClassMeta(String[] key, String name, String caption, String semantic, IMetaRepository repository){
		this(key,name,"",caption, semantic, repository, null, null,null,HistoryMode.NONE, false, new CompositeIndex[0]);
	}
	
	public ClassMeta(String key[], String name, IMetaRepository repository){
		this(key,name,name,null,repository);
	}
	
	public static IClassMeta EmptyMeta(){
		return new ClassMeta(new String[]{""},"",null);
	}
	
	@Override
	public String[] KeyProperties(){
		if (key == null){
			try {
			IStructMeta a = getAncestor();	
			if (a != null && a instanceof IClassMeta)
				return ((IClassMeta)a).KeyProperties();
			} catch (Exception e){
			}
		}
		return key;
	}

	@Override
	public String ContainerReference() {
		return container;
	}

	@Override
	public String CreationTracker() {
		return createTracker;
	}

	@Override
	public String ChangeTracker() {
		return changeTracker;
	}

	@Override
	public HistoryMode History() {
		return history;
	}

	@Override
	public boolean Journaling() {
		return journaling;
	}

	@Override
	public CompositeIndex[] CompositeIndexes() {
		return compositeIndexes;
	}
}
