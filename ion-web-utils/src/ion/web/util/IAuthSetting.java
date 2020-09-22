package ion.web.util;

import java.util.Map;

public interface IAuthSetting {
	String getName();
	
	String getCaption();
	
	Map<String, String> getOptions();
	
	String getDefaultValue();
	
	boolean getHasOptions();
}
