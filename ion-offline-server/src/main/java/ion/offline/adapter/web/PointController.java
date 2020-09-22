package ion.offline.adapter.web;

import javax.servlet.http.HttpServletRequest;

import ion.core.IAuthContext;
import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.offline.server.dao.IPointDAO;
import ion.offline.server.dao.IUserDAO;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;
import ion.offline.util.ISignatureProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/points")
public class PointController extends BasicController {
	
	@Autowired
	private IPointDAO pointDAO;
	
	@Autowired
	private IUserDAO userDAO;
	
	@Autowired
	private ISignatureProvider signatureProvider;

	@Autowired
	private ILogger logger;
	
	@Autowired
	private IAuthContext authContext;
	
	@RequestMapping(value = "", method={RequestMethod.GET})
	public String list(Model model) throws IonException {
		model.addAttribute("Title", "Список клиентов");
		model.addAttribute("points", pointDAO.GetPoints());
		return themeDir+"/points";
	}
	
	@RequestMapping(value = "/delete", method={RequestMethod.POST})
	public String delete(@RequestParam(value="id", required=true) Integer id, Model model) throws IonException {
		Point p = pointDAO.GetPointById(id);
		User[] users = userDAO.UsersByPoint(p);
		for(int i = 0; i < users.length; i++)
			userDAO.DeleteUser(users[i]);
		pointDAO.DeletePoint(p);
		
		return "redirect:.";
	}
	
	private String keyGen(Point point) throws IonException {
		String[] keys = this.signatureProvider.createKeyPair();		
		point.setOpenKey(keys[1]);
		pointDAO.updatePoint(point);		
		return keys[0];
	}
	
	@RequestMapping(value = "/create", method={RequestMethod.GET})
	public String create(Model model, HttpServletRequest request) throws IonException {
		Point p = pointDAO.addPoint();
		String prk = this.keyGen(p);
		logger.Info("Создан клиент № " + p.getId() + ((authContext.CurrentUser() != null)?("пользователем " + authContext.CurrentUser().getUid()):"анонимусом") + " с адреса " + request.getRemoteAddr());
		model.addAttribute("id", p.getId());
		model.addAttribute("publicKey", p.getOpenKey());	
		model.addAttribute("privateKey", prk);		
		return themeDir+"/point";
	}
	
	@RequestMapping(value = "/regenerate", method={RequestMethod.POST})
	public String regenerate(@RequestParam(value="id", required=true) Integer id, 
	                         Model model, HttpServletRequest request) throws IonException {
		Point p = pointDAO.GetPointById(id);
		String prk = this.keyGen(p);
		logger.Info("Ключи клиента № " + id + " перегенерированы " + ((authContext.CurrentUser() != null)?("пользователем " + authContext.CurrentUser().getUid()):"анонимусом") + " с адреса " + request.getRemoteAddr());
		model.addAttribute("id", p.getId());
		model.addAttribute("publicKey", p.getOpenKey());	
		model.addAttribute("privateKey", prk);		
		return themeDir+"/point";
	}
}