package ion.core;

import java.util.Map;

public interface IInputValidator {
	Map<String,String> validate(IItem item, Map<String,Object> newValues);
}
