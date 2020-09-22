package ion.web.util;

import java.util.Map;

import org.springframework.security.core.AuthenticationException;

public interface IAuthSettingsProvider {
	String getName();
	
	IAuthSetting[] getSettings();
	
	boolean Apply(Map<String, String> settings) throws AuthenticationException;
	
	boolean isRequired();
	
	boolean isActive();
}
