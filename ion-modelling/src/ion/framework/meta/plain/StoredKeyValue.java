package ion.framework.meta.plain;

public class StoredKeyValue {
	
	public String key;
	
	public String value;

	public StoredKeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public StoredKeyValue() {
		this.key = null;
		this.value = null;
  }
}
