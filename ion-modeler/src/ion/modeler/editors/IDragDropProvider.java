package ion.modeler.editors;

import java.util.List;

public interface IDragDropProvider {
	public String OnDrag(Object o);
	
	public Object OnDrop(List<Object> list, Object target, String buffer, int location);
}
