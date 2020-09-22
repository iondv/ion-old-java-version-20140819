package ion.util.sync;

public interface IDBNameProvider {
	public String getName(String nm, DBNameType t);
	public String getName(String[] nm, DBNameType t);
	public String[] sortWords(String[] Words);
}
