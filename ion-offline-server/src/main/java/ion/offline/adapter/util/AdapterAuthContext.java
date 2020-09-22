package ion.offline.adapter.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import ion.core.IAuthContext;
import ion.core.IUserContext;

public class AdapterAuthContext implements IAuthContext {

	@Override
  public IUserContext CurrentUser() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null)
			return null;
		if (a.getPrincipal() == null)
			return null;

		return new AdapterUserContext((UserDetails)a.getPrincipal());
	}

	@Override
  public void enableContextReload(String u) {
  }

}
