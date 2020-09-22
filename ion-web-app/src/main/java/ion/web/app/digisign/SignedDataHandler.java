package ion.web.app.digisign;

import java.text.DateFormat;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.IAuthContext;
import ion.core.IonException;
import ion.core.digisign.DataPart;
import ion.core.digisign.ISignedDataHandler;
import ion.framework.digisign.DigitalSignatureDAO;

public class SignedDataHandler implements ISignedDataHandler {
	
	@Autowired
	private IAuthContext authContext;
	
	private DigitalSignatureDAO signDao;
	
	private Gson gson;
	
	public SignedDataHandler(){
		gson = new GsonBuilder()
		.serializeNulls()
		.serializeSpecialFloatingPointValues()	
		.setDateFormat(DateFormat.SHORT)
		.create();		
	}
	
	public DigitalSignatureDAO getDigitalSignatureDAO() {
	  return signDao;
  }

	public void setDigitalSignatureDAO(DigitalSignatureDAO value) {
	  this.signDao = value;
  }
	
	protected String processSignedData(Map<String, String> attributes, String data, String signature) throws IonException {
		return data;
	}
	
	protected String processSignature(Map<String, String> attributes, String data, String signature) throws IonException {
		return signature;
	}
	
	protected void processDataItem(String className, String id) throws IonException {
		
	}
	
	protected void saveSignature(String uid, String className, String objId, int index, String action, 
	                             Map<String, String> attributes, String signature, String data){
		signDao.addSign(uid, className, objId, index, action, 
		                gson.toJson(attributes), signature.getBytes(),data.getBytes());				
	}

	@Override
	@Transactional
	public void process(String ItemId, String action, Map<String, String> attributes, String data, String signature) throws IonException {
		String[] ids = ItemId.split("\\.");
		String className = ids[0];
		String objId = ids[1];
		processDataItem(className, objId);
		
		saveSignature(authContext.CurrentUser().getUid(), className, objId, 0, action, attributes, 
		                processSignature(attributes, data, signature), 
		                processSignedData(attributes, data, signature));
	}

	@Override
	@Transactional
	public void process(String ItemId, String action, Map<String, String> attributes, DataPart[] parts, String[] signatures)
																																				 throws IonException {
		String[] ids = ItemId.split("\\.");
		String className = ids[0];
		String objId = ids[1];
		
		processDataItem(className, objId);
		
		for (int i = 0; i < signatures.length; i++)
			saveSignature(authContext.CurrentUser().getUid(), className, objId, i, action, 
			                attributes, 
			                processSignature(attributes, parts[i].getString(), signatures[i]), 
			                processSignedData(attributes, parts[i].getString(), signatures[i]));		
	}
}
