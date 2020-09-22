package ion.web.app.digisign;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import ion.core.IItem;
import ion.core.IonException;
import ion.core.digisign.DataForSign;
import ion.core.digisign.DataPart;
import ion.core.digisign.IDigiSignDataProvider;
import ion.core.digisign.ISignedDataHandler;
import ion.core.logging.ILogger;
import ion.web.app.BasicController;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.JSONResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/digisign")
public class DigiSignController extends BasicController {
	@Autowired(required=false)
	private IDigiSignDataProvider dataProvider;
	
	@Autowired(required=false)
	private ISignedDataHandler signedDataHandler;
	
	public void setDataProvider(IDigiSignDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public void setSignedDataHandler(ISignedDataHandler dataHandler) {
		this.signedDataHandler = dataHandler;
	}
	
	@RequestMapping(value="/get-data-for-signing",
			method = {RequestMethod.GET}, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	protected @ResponseBody DataForSigning getData(@RequestParam(required = true) String id, @RequestParam(required = true) String action, HttpServletResponse response) throws Exception {
		DataForSign d = null;
		if (dataProvider != null){
			String[] ids = id.split("\\.");	
			IItem item = data.getItem(ids[0], ids[1]);
			if (item != null)
				d = dataProvider.getData(item,action);
		}
		
		if (d == null)
			d = new DataForSign(new DataPart[0], new HashMap<String, String>());
		
		return new DataForSigning(d);
	}
	
	@RequestMapping(value="/process-sign",	method = {RequestMethod.POST}/*, 
			produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE*/)
	public @ResponseBody JSONResponse processSign(@RequestBody final SignatureData signature) throws IonException {
		try {
			if (signedDataHandler == null)
				throw new IonException("Не указан обработчик подписанных данных");
			
			List<DataPart> parts = new LinkedList<DataPart>();
			for (ion.web.app.digisign.DataPart prt: signature.parts){
				parts.add(new DataPart(prt.mimeType, prt.contents));
			}
			
			signedDataHandler.process(signature.id, signature.action, signature.attributes, parts.toArray(new DataPart[parts.size()]), signature.signatures);
			return new JSONResponse();
		} catch (Exception e) {
			e.printStackTrace(logger.Out());
			logger.FlushStream(ILogger.ERROR);
			return new JSONResponse(new IonMessage(e.getLocalizedMessage(), IonMessageType.ERROR));
		}
	}	
}
