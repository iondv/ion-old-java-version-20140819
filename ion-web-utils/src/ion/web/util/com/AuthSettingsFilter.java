package ion.web.util.com;

import java.util.HashMap;
import java.util.Map;

import ion.web.util.IAuthSetting;
import ion.web.util.IAuthSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AuthSettingsFilter extends UsernamePasswordAuthenticationFilter {

	private IAuthSettings authSettings;

    public IAuthSettings getAuthSettings() {
		return authSettings;
	}

	public void setAuthSettings(IAuthSettings authSettings) {
		this.authSettings = authSettings;
	}

	@Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    	if(authSettings != null && authSettings.isProvided()){
    		IAuthSetting[] settings = authSettings.getSettings();
    		Map<String, String> values = new HashMap<String, String>();
    		for (IAuthSetting s: settings){
    			if (request.getParameter(s.getName()) != null)
    				values.put(s.getName(), request.getParameter(s.getName()));
    		}
    		authSettings.Apply(values);
    	}
        return super.attemptAuthentication(request, response); 
    }
    
}
