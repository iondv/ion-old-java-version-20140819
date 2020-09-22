package ion.web.admin;

import ion.auth.persistence.User;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class UserController extends BasicController {

	@Autowired
	private AdminService service;
	
	private Integer pageSize = 20;

	@RequestMapping("/user")
	public String listUsers(Map<String, Object> map) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Пользователи", "/user");
		breadcrumb.add(br1);
		map.put("breadcrumbs", breadcrumb);
		
		PagedListHolder<User> pagedList = service.listUsersPage(1, pageSize);
		map.put("user", new User());
		map.put("userList", pagedList);
		map.put("authorityList", service.listAuthorities());
		
		return themeDir+"/user";
	}
	
	@RequestMapping("/user/{pageNumber}")
	public String listUsersPage(Map<String, Object> map, @PathVariable("pageNumber") Integer pageNumber) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Пользователи", "/user");
		breadcrumb.add(br1);
		map.put("breadcrumbs", breadcrumb);
		
		PagedListHolder<User> pagedList = service.listUsersPage(pageNumber, pageSize);
		map.put("user", new User());
		map.put("userList", pagedList);
		map.put("authorityList", service.listAuthorities());
		
		return themeDir+"/user";
	}
	
	
	
	@RequestMapping(value="/userCreate", method = RequestMethod.GET)
	public String createUser(Map<String, Object> map){
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Пользователи", "/user");
		Breadcrumb br2 = new Breadcrumb("Создать пользователя", "/userCreate");
		breadcrumb.add(br1);
		breadcrumb.add(br2);
		map.put("breadcrumbs", breadcrumb);
		map.put("user", new User());
		map.put("authorityList", service.listAuthorities());
		return themeDir+"/userCreate";
	}
	
	@RequestMapping(value = "/userAdd", method = RequestMethod.POST)
	public String addUser(@ModelAttribute("user") User user,
			@RequestParam(value="userAuthorities", required=false) Integer[] userAuthorities, 
			BindingResult result) throws UnsupportedEncodingException {
		Integer userId = service.addUser(user, userAuthorities);
		return "redirect:"+Url("userUpdate/"+userId);
	}
	
	@RequestMapping("/userDelete")
	public String deleteUser(@RequestParam(value="ids") Integer[] ids) throws UnsupportedEncodingException {
		service.deleteUser(ids);
		return "redirect:"+Url("user");
	}
	
	@RequestMapping("/userDelete/{userId}")
	public String deleteUserById(@PathVariable("userId") Integer userId) throws UnsupportedEncodingException {
		service.deleteUserById(userId);
		return "redirect:"+Url("user");
	}
	
	@RequestMapping(value="/userUpdate/{userId}", method = RequestMethod.GET)
	public String editUser(@ModelAttribute("user") User user ,@PathVariable("userId") Integer userId, Map<String, Object> map) {
		
		ArrayList<Breadcrumb> breadcrumb = new ArrayList<Breadcrumb>();
		Breadcrumb br1 = new Breadcrumb("Пользователи", "/user");
		Breadcrumb br2 = new Breadcrumb("Редактирование пользователя", "/userUpdate/"+userId);
		breadcrumb.add(br1);
		breadcrumb.add(br2);
		map.put("breadcrumbs", breadcrumb);
		User userToShow = service.getUser(userId);
		map.put("userToShow", userToShow);
		map.put("authorityList", service.listAuthorities());
		
		return themeDir+"/userUpdate";
	}
	
	@RequestMapping(value="/userUpdate/{userId}", method = RequestMethod.POST)
	public String updateUser (@ModelAttribute("user") User user, @PathVariable("userId") Integer userId, @RequestParam(value="authIds[]", required=false) Integer[] authIds) throws UnsupportedEncodingException {
		service.updateUser(user, userId, authIds);
		return "redirect:"+Url("userUpdate/"+userId);
	}
	
	@RequestMapping(value="/updateUserPassword/{userId}", method = RequestMethod.POST)
	public String updateUserPassword (@PathVariable("userId") Integer userId, @RequestParam("password") String password) throws UnsupportedEncodingException {
		service.updateUserPassword(userId, password);
		return "redirect:"+Url("userUpdate/"+userId);
	}
	
}