package ion.offline.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;

import ion.core.IonException;
import ion.offline.filesystem.FileUtils;
import ion.offline.net.AuthResult;
import ion.offline.net.DataChange;
import ion.offline.net.DataUnit;
import ion.offline.server.dao.IPointDAO;
import ion.offline.server.dao.IUserDAO;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;
import ion.offline.util.IHashProvider;
import ion.offline.util.IVolumeProcessor;
import ion.offline.util.server.SyncSession;

public class SimpleSyncSession extends SyncSession {
	
	private IPointDAO pointDAO;
	
	private IUserDAO userDAO;
	
	private File inputDirectory;
	
	private IVolumeProcessor volumeProcessor;
	
	private IHashProvider hashProvider;

	@Override
	protected void processAuthentication(String client, AuthResult[] auth) throws IonException {
		Point point = pointDAO.GetPointById(Integer.parseInt(client));
		// userDAO.ResetUsersTokens(point);
		for(AuthResult au: auth)
			if(au.result){
				userDAO.attachUser(au.login, point, au.token);
			}

	}

	@Override
  protected void setPointSyncHorizon(String client, Integer syncHorizon) throws IonException {
	  	Point point = pointDAO.GetPointById(Integer.parseInt(client));
	  	if(point.getSyncHorizon() != syncHorizon){
		  	point.setSyncHorizon(syncHorizon);
				pointDAO.updatePoint(point);
	  	}
  }

	@Override
	protected String generateUploadToken(String client)  throws IonException {
		Point point = pointDAO.GetPointById(Integer.parseInt(client));		
		String token = UUID.randomUUID().toString();
		point.setAuthorizationToken(token);
		try {
			pointDAO.updatePoint(point);
		} catch (IonException e) {
			e.printStackTrace();
		}		
		return token;
	}

	@Override
	protected boolean checkUploadToken(String client, String token) throws IonException {
		Point point = this.pointDAO.GetPointById(Integer.parseInt(client));
		return point.getAuthorizationToken().equals(token);
	}

	@Override
	protected boolean accept(String client, File file, String hashSum)
			throws IOException, IonException {
		File dest = new File(inputDirectory,client + File.separator + "volumes" + File.separator + file.getName());
		
		if (hashProvider.hash(file).equals(hashSum)){
			dest.getParentFile().mkdirs();
			Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} else if (dest.exists() && hashProvider.hash(dest).equals(hashSum)){
			file.delete();
			return true;
		}
		file.delete();
		return false;
	}

	@Override
	protected boolean packageReady(String client, int total) throws IonException {
		File dest = new File(inputDirectory,client + File.separator + "volumes");
		return dest.list().length == total;
	}
	
	@Override
	protected boolean packageSent(String client) throws IOException, IonException {
		File dest = new File(inputDirectory,client + File.separator + "volumes");
		for (File f: dest.listFiles())
			f.delete();
		return false;
	}
	
	private Object processJsonElement(JsonElement obj){
		if (obj.isJsonNull())
			return null;
		else if (obj.isJsonObject()){
			Map<String, Object> result = new LinkedTreeMap<String, Object>();
			Set<Entry<String, JsonElement>> map = obj.getAsJsonObject().entrySet();
			for (Entry<String, JsonElement> entry: map){
				if (entry.getKey().equals("timestamp") && map.size() == 1){
					if (entry.getValue().isJsonPrimitive()){
						Long l = entry.getValue().getAsLong();
						return new Date(l);
					}
				}
				result.put(entry.getKey(), processJsonElement(entry.getValue()));
			}
			return result;
		} else if (obj.isJsonPrimitive()){
			JsonPrimitive p = obj.getAsJsonPrimitive();
			if (p.isNumber()){
				Long l = p.getAsLong();
				Double d = p.getAsDouble();
				Object r = 0;
				if (l.doubleValue() == d.doubleValue())
					r = l;
				else
					r = d;
				return r;
			} else if (p.isBoolean())
				return p.getAsBoolean();
			else if (p.isString())
				return p.getAsString();
		} else if (obj.isJsonArray()){
			List<Object> result = new LinkedList<Object>(); 
			Iterator<JsonElement> i = obj.getAsJsonArray().iterator();
			while (i.hasNext()){
				result.add(processJsonElement(i.next()));
			}
			return result.toArray();
		}
		return null;
	}	

	@SuppressWarnings("unchecked")
	@Override
	protected DataUnit[] unPack(String client) throws IOException, IonException {
		DataUnit[] result = null;
		File src = new File(inputDirectory,client + File.separator + "volumes");
		File tmp = new File(inputDirectory,client + File.separator + "data");
		tmp.mkdirs();
		try {
			volumeProcessor.Join(src.listFiles(), tmp);

			File chDir = tmp;
			File[] changes = {};
			
			if (chDir.exists()){
				changes = chDir.listFiles();
				Arrays.sort(changes);
			}
			
			result = new DataUnit[changes.length];
			int i = 0;
			JsonParser parser = new JsonParser();
			for (File f: changes) {
				Map<String, Object> m = (Map<String, Object>)processJsonElement(parser.parse(new InputStreamReader(new FileInputStream(f),"utf-8")));
				
				if (f.getName().endsWith("change.json"))
					result[i] = new DataChange((String)m.get("author"),(String)m.get("action"),(String)m.get("id"), (String)m.get("className"), (Map<String, Object>)m.get("data"));
				else	
					result[i] = new DataUnit((String)m.get("id"), (String)m.get("className"), (Map<String, Object>)m.get("data"));
				i++;
			}
		} catch (IOException e) {
			throw e;
		}
		FileUtils.delete(tmp);
		return result;
	}

	@Override
	protected String getUserAuthToken(String login)  throws IonException {
		try {
			User u = userDAO.getUser(login);
			if (u != null)
				return u.getToken();
		} catch (IonException e) {
			throw new IonException(e);
		}
		return null;
	}

	public IPointDAO getPointDAO() {
		return pointDAO;
	}

	public void setPointDAO(IPointDAO pointDAO) {
		this.pointDAO = pointDAO;
	}

	public IUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(IUserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public void setInputDirectory(File inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public IVolumeProcessor getVolumeProcessor() {
		return volumeProcessor;
	}

	public void setVolumeProcessor(IVolumeProcessor volumeProcessor) {
		this.volumeProcessor = volumeProcessor;
	}

	public IHashProvider getHashProvider() {
		return hashProvider;
	}

	public void setHashProvider(IHashProvider hashProvider) {
		this.hashProvider = hashProvider;
	}

	@Override
	protected void prepare(String client)  throws IonException {
		File vlms = new File(inputDirectory,client + File.separator + "volumes");
		try {
			Files.deleteIfExists(vlms.toPath());
		} catch (IOException e) {
			throw new IonException(e);
		}
	}
}
