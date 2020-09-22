package ion.offline.adapter.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.*;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.offline.server.dao.*;
import ion.offline.server.entity.*;
import ion.offline.util.ISignatureProvider;
import ion.offline.util.ISyncSession;
import ion.offline.net.SyncRequest;
import ion.offline.net.SyncRequestResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;

@Controller
public class AdapterController extends BasicController {
	
	@Autowired
	private IPointDAO pointDAO;
	
	@Autowired
	private IDataPackageDAO packageDAO;
		
	@Autowired
	private ISyncSession syncSession;
	
	@Autowired
	private ISignatureProvider signatureProvider;
	
	@Autowired
	private ILogger logger;
	
	@RequestMapping(value = "/", method={RequestMethod.GET})
	public String home(Model model) throws IonException {
		model.addAttribute("Title", "Очередь пакетов");
		model.addAttribute("dataPackages", packageDAO.GetQueue(10));
		return themeDir+"/home";
	}	
		
	@RequestMapping(value = "/transfer", consumes="application/json", method={RequestMethod.POST})
	@ResponseBody
	public SyncRequestResult init(
			@RequestHeader("transport-module-id") String id,
			@RequestHeader("transport-module-signature") String signature,
			@RequestBody String body,
			HttpServletResponse response) throws IonException {
		
		Point point = this.pointDAO.GetPointById(Integer.parseInt(id));
		boolean verify = false;
		
		if (point == null)
			throw new IonException("Не найден клиент с идентификатором " + id + "!");
		try {
			verify = signatureProvider.check(point.getOpenKey(), signature, body);
		} catch(Exception e) {
			verify = false;
			logger.Error("Ошибка проверки подписи!", e);
			throw new IonException(e);
		}
		if(verify){
			try {
				SyncRequest sr = new Gson().fromJson(body, SyncRequest.class);
				SyncRequestResult result = syncSession.Init(id, sr.credentials, sr.syncHorizon);
				return result;
			} catch(Exception e) {
				logger.Error("Ошибка обработки запроса!", e);
				throw new IonException(e);
			}			
		} else {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch(Exception e) {
				logger.Error("Ошибка обработки запроса!", e);
				throw new IonException(e);
			}			
		}
		return null;
	}
	
	@RequestMapping(value = "/transfer/completed", method={RequestMethod.GET})
	public void completed(
			@RequestHeader("transport-module-id") String id,
			@RequestHeader("transport-module-token") String token,
			HttpServletResponse response) throws IonException {
		
			try {
				if (!syncSession.DownloadComplete(id, token))
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (IonException | IOException e) {
				logger.Error("Ошибка обработки запроса!", e);
				throw new IonException(e);
			}
	}
	
	@RequestMapping(value = "/transfer/upload", method={RequestMethod.POST})
	@ResponseStatus(value = HttpStatus.OK)
	//@ResponseBody
	public void upload(
			@RequestHeader("transport-module-id") String id,
			@RequestHeader("transport-module-token") String token,
			@RequestHeader("transport-module-total") int total,
			@RequestHeader("transport-module-hashsum") String hash,
			MultipartHttpServletRequest request,
			HttpServletResponse response) throws IonException, IOException {
		try {
			MultipartFile f = request.getFile("volume");
			File tmp = new ServletContextResource(request.getServletContext(),"WEB-INF"+File.separator+"tmp").getFile();
			tmp.mkdirs();
			File volume = new File(tmp,f.getOriginalFilename());
			f.transferTo(volume);
			if (!syncSession.AcceptVolume(id, token, volume, hash, total))
				response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		} catch(Exception e) {
			logger.Error("Ошибка обработки запроса!", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IonException(e);
		}
	}
}