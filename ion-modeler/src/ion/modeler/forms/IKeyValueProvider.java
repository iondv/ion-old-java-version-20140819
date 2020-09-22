package ion.modeler.forms;

import java.util.Map;

public interface IKeyValueProvider {
	Map<String,String> Provide(Object model);
}
