package ion.web.admin;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import ion.auth.persistence.Authority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthorityController extends BasicController {

	@Autowired
	AdminService service;
	
	@RequestMapping("/auth")
	public String listAuthority(Map<String, Object> map) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Роли пользователей", "/auth");
		breadcrumb.add(br1);
		map.put("breadcrumbs", breadcrumb);
		map.put("authorityList",service.listAuthorities());

		return themeDir+"/authority";
	}
	
	@RequestMapping(value = "/authCreate", method = RequestMethod.GET)
	public String createAuthority(Map<String, Object> map) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Роли пользователей", "/auth");
		Breadcrumb br2 = new Breadcrumb("Создание роли", "/authCreate");
		breadcrumb.add(br1);
		breadcrumb.add(br2);
		map.put("breadcrumbs", breadcrumb);
		map.put("authority", new Authority());
		return themeDir+"/authorityCreate";
	}
	
	@RequestMapping(value = "/authAdd", method = RequestMethod.POST)
	public String addAuthority(@ModelAttribute("authority") Authority authority,
			BindingResult result) throws UnsupportedEncodingException {

		service.addAuthority(authority);

		return "redirect:"+Url("auth");
	}

	@RequestMapping("/authDelete")
	public String deleteAuthority(@RequestParam("auth_ids") Integer[] ids) throws UnsupportedEncodingException {
		service.deleteAuthority(ids);
		return "redirect:"+Url("auth");
	}
	
	@RequestMapping("/authDelete/{authId}")
	public String deleteAuthorityById(@PathVariable("authId") Integer authId) throws UnsupportedEncodingException {
		service.deleteAuthorityById(authId);
		return "redirect:"+Url("auth");
	}
	
	@RequestMapping(value="/authUpdate/{authId}", method = RequestMethod.GET)
	public String editAuthority(@ModelAttribute("authority") Authority authority, Map<String, Object> map, @PathVariable("authId") Integer authId) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Роли пользователей", "/auth");
		Breadcrumb br2 = new Breadcrumb("Редактирование роли", "/authUpdate/"+authId);
		breadcrumb.add(br1);
		breadcrumb.add(br2);
		map.put("breadcrumbs", breadcrumb);
		Authority authToShow = service.getAuthority(authId);
		map.put("authToShow", authToShow);
		return themeDir+"/authorityUpdate";
	}
	
	@RequestMapping(value="/authUpdate/{authId}", method = RequestMethod.POST)
	public String updateAuthority(@ModelAttribute("authority") Authority authority, @PathVariable("authId") Integer authId) throws UnsupportedEncodingException {
		service.updateAuthority(authority, authId);
		return "redirect:"+Url("auth");
	}	
	
}