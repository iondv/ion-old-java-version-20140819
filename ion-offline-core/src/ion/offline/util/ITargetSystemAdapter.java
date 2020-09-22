package ion.offline.util;

import java.util.Date;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.ActionResult;
import ion.offline.net.AuthResult;
import ion.offline.net.DataUnit;

public interface ITargetSystemAdapter {
	AuthResult[] authenticate(UserCredentials[] credentials) throws IonException;
	
	SyncDelta getDelta(String client, String[] users, Date since, Integer syncHorizon) throws IonException;
		
	ActionResult push(String authToken, DataUnit data) throws IonException;	
}
