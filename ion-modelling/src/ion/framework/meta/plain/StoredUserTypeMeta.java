package ion.framework.meta.plain;

/**
 * для сериализации пользовательского типа в Json
 */
public class StoredUserTypeMeta {
	public String	name;
	public String	caption;
	public Integer	type;
	public String	mask;
	public String	mask_name;
	public Short	size;
	public Short	decimals;

	public StoredUserTypeMeta() {
		
	}
	
	public StoredUserTypeMeta(String name, String caption, Integer type,
			Short size, Short decimals) {
		this.name = name;
		this.caption = caption;
		this.type = type;
		this.size = size;
		this.decimals = decimals;
	}

}
