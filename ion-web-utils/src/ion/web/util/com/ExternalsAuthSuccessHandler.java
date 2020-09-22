package ion.web.util.com;

import ion.web.util.IExternalAuthDispatcher;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class ExternalsAuthSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler {
	
	private IExternalAuthDispatcher authDispatcher;
	
	public void onAuthenticationSuccess(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response, Authentication authentication)
              throws javax.servlet.ServletException, IOException {
		authDispatcher.Authenticate(request.getParameter("j_username"), request.getParameter("j_password"));
		super.onAuthenticationSuccess(request, response, authentication);
	}

	public IExternalAuthDispatcher getAuthDispatcher() {
		return authDispatcher;
	}

	public void setAuthDispatcher(IExternalAuthDispatcher authDispatcher) {
		this.authDispatcher = authDispatcher;
	}
}
