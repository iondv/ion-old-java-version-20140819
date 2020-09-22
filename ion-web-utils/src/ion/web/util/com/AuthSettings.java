package ion.web.util.com;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import ion.web.util.IAuthSetting;
import ion.web.util.IAuthSettings;
import ion.web.util.IAuthSettingsProvider;

public class AuthSettings implements IAuthSettings {
	
	private Map<String , IAuthSettingsProvider> providers = new HashMap<String, IAuthSettingsProvider>();
	
	public class AuthSettingWrapper implements IAuthSetting {

		IAuthSetting base;
		
		String provider;
		
		public AuthSettingWrapper(String p, IAuthSetting s) {
			base = s;
			provider = p;
		}
		
		public String getName() {
			return provider + "." + base.getName();
		}
		
		public String getCaption() {
			return base.getCaption();
		}

		public Map<String, String> getOptions() {
			return base.getOptions();
		}

		public String getDefaultValue() {
			return base.getDefaultValue();
		}

		@Override
		public boolean getHasOptions() {
			return base.getHasOptions();
		}		
	}
	
	public void RegisterProvider(IAuthSettingsProvider provider){
		providers.put(provider.getName(), provider);
	}

	public boolean isProvided() {
		if (providers.size() > 0){
			for (IAuthSettingsProvider p: providers.values())
				if (p.isActive())
					return true;
		}
		return false;
	}

	public IAuthSetting[] getSettings() {
		List<IAuthSetting> result = new LinkedList<IAuthSetting>();
		for (IAuthSettingsProvider p: providers.values()){
			for (IAuthSetting s: p.getSettings())
				result.add(new AuthSettingWrapper(p.getName(), s));
		}
		return result.toArray(new IAuthSetting[result.size()]);
	}

	public void Apply(Map<String, String> settings) throws AuthenticationException {
		Map<String, Map<String,String>> temp = new HashMap<String, Map<String,String>>();
		
		for (Map.Entry<String, String> pair: settings.entrySet()){
			String provider = pair.getKey().substring(0, pair.getKey().indexOf("."));
			String setting = pair.getKey().substring(pair.getKey().indexOf(".") + 1);
			if (!temp.containsKey(provider))
				temp.put(provider, new HashMap<String, String>());
			temp.get(provider).put(setting, pair.getValue());
		}
		
		for (Map.Entry<String, Map<String,String>> prov: temp.entrySet()){
			if (providers.containsKey(prov.getKey())){
				IAuthSettingsProvider p = providers.get(prov.getKey());
				boolean applied = p.Apply(prov.getValue());
				if (p.isRequired() && !applied)
					throw new AuthenticationServiceException("Не удалось установить обязательные опции аутентификации!");
			}
		}
	}

}
