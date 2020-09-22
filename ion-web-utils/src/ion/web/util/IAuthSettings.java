package ion.web.util;

import java.util.Map;

import org.springframework.security.core.AuthenticationException;

public interface IAuthSettings {
	boolean isProvided();
	IAuthSetting[] getSettings();
	void Apply(Map<String, String> settings) throws AuthenticationException;
	void RegisterProvider(IAuthSettingsProvider provider);
}
