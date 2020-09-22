package ion.web.app.util;

//import java.io.IOException;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

//import ion.core.IDataRepository;
//import ion.core.IMetaRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import ion.auth.dao.UserDaoImpl;
import ion.auth.persistence.User;
import ion.auth.persistence.UserProperty;
import ion.core.IUserContext;
//import ion.core.IonException;
import ion.core.IAuthContext;


//import ion.viewmodel.navigation.INavigationModel;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
/*import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.bind.annotation.SessionAttributes;*/
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AuthContext implements 
			IAuthContext/*, 
			AuthenticationSuccessHandler, 
			LogoutSuccessHandler, 
			AuthenticationFailureHandler,
			ApplicationListener<SessionDestroyedEvent>*/{
	
	@Autowired(required=false)
	UserDaoImpl userDao;
	
	private Set<String> reloadUserProps = new HashSet<String>();
	
	@Override
	public void enableContextReload(String u){
		reloadUserProps.add(u);
	}
	
	@Override
	@Transactional
	public IUserContext CurrentUser() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null)
			return null;
		if (a.getPrincipal() == null)
			return null;
		
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession sess = attr.getRequest().getSession();
    
		String login = ((UserDetails)a.getPrincipal()).getUsername();
		
		Gson gs = new GsonBuilder().serializeNulls().create();
		
    @SuppressWarnings("unchecked")
		Map<String, Object> props = (Map<String, Object>)sess.getAttribute("user-context-properties");
    if ((props == null || reloadUserProps.contains(login)) && userDao != null){
    	reloadUserProps.remove(login);
    	props = new HashMap<String, Object>();
    	User u = userDao.getUser(login);
    	for (UserProperty p: u.getProperties())
      	props.put(p.getName(), (p.getValue() != null)?gs.fromJson(p.getValue(),Object.class):null);
    	sess.setAttribute("user-context-properties",props);
    }
		return new S2UserContext((UserDetails)a.getPrincipal(), props);
	}

	/*
	@Override
	public void onAuthenticationSuccess(HttpServletRequest arg0,
			HttpServletResponse arg1, Authentication arg2) throws IOException,
			ServletException {
		UserDetails u = (UserDetails)arg2.getPrincipal(); 
		setContext(u);
		SimpleUrlAuthenticationSuccessHandler h = new SimpleUrlAuthenticationSuccessHandler();
		h.onAuthenticationSuccess(arg0, arg1, arg2);
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest arg0,
			HttpServletResponse arg1, Authentication arg2) throws IOException,
			ServletException {
		setContext(null);
		SimpleUrlLogoutSuccessHandler h = new SimpleUrlLogoutSuccessHandler();
		h.onLogoutSuccess(arg0, arg1, arg2);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest arg0,
			HttpServletResponse arg1, AuthenticationException arg2)
			throws IOException, ServletException {
		setContext(null);
		SimpleUrlAuthenticationFailureHandler h = new SimpleUrlAuthenticationFailureHandler();
		h.onAuthenticationFailure(arg0, arg1, arg2);
	}

	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		setContext(null);
	}
	*/
}
