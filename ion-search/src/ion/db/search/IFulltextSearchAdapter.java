package ion.db.search;

import java.util.List;

public interface IFulltextSearchAdapter {
	void put(String id, String text);
	void remove(String id);
	List<String> search(String searchQuery, int offset, int rows);
}
