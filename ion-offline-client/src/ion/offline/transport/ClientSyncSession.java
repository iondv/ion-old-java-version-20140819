package ion.offline.transport;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.offline.net.SyncRequest;
import ion.offline.net.SyncRequestResult;
import ion.integration.core.UserCredentials;
import ion.offline.sync.ISyncEnvironment;
import ion.offline.util.IClientSyncSession;
import ion.offline.util.IHashProvider;
import ion.offline.util.ISignatureProvider;

public class ClientSyncSession implements IClientSyncSession {
	
	private ISignatureProvider signatureProvider;
	
	private IHashProvider hashProvider;
	
	private String transportModuleId;
	
	private String privateKey;
	
	private String adapterUrl;	

	private IDownloadManager downloadManager;
	
	private ISyncEnvironment environment;
	
	private ILogger logger;
	
	private class RequestResult {
		public int status = -1;
		
		public String data;
		
		public RequestResult(int status, String content){
			this.status = status;
			data = content;
		}
	}
	
	@Override
	public SyncRequestResult Init(UserCredentials[] credentials, Integer syncHorizon)
			throws IonException {
		if (credentials != null && credentials.length > 0){
			Gson gson = new Gson();
			SyncRequest sr = new SyncRequest();
			sr.credentials = credentials;
			if(syncHorizon != null)
				sr.syncHorizon = syncHorizon;
			String body = gson.toJson(sr);
			try {
				String transportModuleSignature = signatureProvider.sign(privateKey, body);
				
				Map<String,String> headers = new HashMap<String, String>();
				headers.put("Content-Type", "application/json");
				headers.put("transport-module-id", transportModuleId);
				headers.put("transport-module-signature", transportModuleSignature);
				
				RequestResult r = sendRequest(adapterUrl + (adapterUrl.endsWith("/")?"":"/")+"transfer",null,headers,body,true);
				if (r.status == HttpURLConnection.HTTP_OK){
					SyncRequestResult result = gson.fromJson(r.data,SyncRequestResult.class);
					return result;
				} else logger.Error("Ошибка при запросе к оффлайн-адаптеру!");
			} catch (Exception e) {
				logger.Error("Ошибка при запросе к сервису!", e);
			}
		}
		
		return null;
	}

	@Override
	public File DownloadVolume(String url, String hashSum, int timeout)
			throws IonException, SocketTimeoutException, SocketException  {
		File file = null;
		try {
			if (downloadManager == null)
				downloadManager = new UrlDownloadManager();
			
			if (!downloadManager.isStarted())
				downloadManager.start(timeout, environment.getDownloadDirectory());
			
			file = downloadManager.download(new URL(url));
		} catch (IOException e) {
			logger.Error("Ошибка при загрузке тома [" +url+ "]!", e);
			if (e instanceof SocketTimeoutException)
				throw (SocketTimeoutException)e;
			if (e instanceof SocketException)
				throw (SocketException)e;
		}
			
		if (file != null && file.exists()){
			if (hashSum == null || hashProvider.hash(file).equals(hashSum))
				return file;
			file.delete();
			throw new IonException("Загруженный том [" + url + "] не прошел проверку целостности.");
		}
			
		return null;
	}

	@Override
	public int UploadVolume(String token, File volume, int total,
			int timeout) throws IonException, SocketTimeoutException, SocketException {
		try {
			String hashSum = hashProvider.hash(volume);
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("transport-module-id", transportModuleId);
			headers.put("transport-module-token",token);
			headers.put("transport-module-total",Integer.toString(total));
			headers.put("transport-module-hashsum",hashSum);
			RequestResult r = sendRequest(adapterUrl + (adapterUrl.endsWith("/")?"":"/") + "transfer/upload",timeout,headers,volume,false);
			return r.status;
		} catch (IOException e) {
			logger.Error("Ошибка при запросе к сервису!", e);
			if (e instanceof SocketTimeoutException)
				throw (SocketTimeoutException)e;
			else if (e instanceof SocketException)
				throw (SocketException)e;
			else
				throw new IonException(e);
		}
	}

	@Override
	public int DownloadComplete(String token, int timeout) throws IonException, SocketTimeoutException {
		try{
			downloadManager.stop();
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("transport-module-id", transportModuleId);
			headers.put("transport-module-token", token);
			RequestResult r = sendRequest(adapterUrl + (adapterUrl.endsWith("/")?"":"/") + "transfer/completed", timeout, headers, null,false);
			return r.status;
		} catch(IOException e){
			logger.Error("Ошибка при запросе к сsервису!", e);
			if (e instanceof SocketTimeoutException)
				throw (SocketTimeoutException)e;
			else
				throw new IonException(e);
		}		
	}

	
	private RequestResult sendRequest(String url, Integer timeout, Map<String,String> headers, Object data, boolean read) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
		if(timeout != null) {
			connection.setConnectTimeout(timeout*1000);
			connection.setReadTimeout(timeout*1000);
		}
		
		connection.setDoInput(true);
		connection.setDoOutput(data != null);
		connection.setUseCaches(false);  
		connection.setRequestProperty("User-Agent", "Mozilla/5.0"); 
		for(Entry<String,String> header : headers.entrySet()){
			connection.setRequestProperty(header.getKey(), header.getValue());
		}
		
		if (data != null){
			connection.setRequestMethod("POST");
			
			String lineEnd = "\r\n"; 
			String twoHyphens = "--"; 
			String boundary =  "*****";
			
			if (data instanceof File){
				connection.setRequestProperty("Content-Type", "multipart/form-data, boundary="+boundary);
				connection.setRequestProperty("Connection", "Keep-Alive"); 
			}
			
			OutputStream out = connection.getOutputStream();
			
			if (data instanceof String){
				OutputStreamWriter w = new OutputStreamWriter(out);
				w.write((String)data);
				w.flush();
				w.close();
			}
			
			if (data instanceof File){
				File volume = (File)data;
				
				int bytesRead, bytesAvailable, bufferSize; 
				byte[] buffer; 
				
				int maxBufferSize = 1024*1024;

				DataOutputStream dos = new DataOutputStream(connection.getOutputStream()); 

				// Send a binary file
				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=\"volume\"; filename=\"" + volume.getName() +"\"" + lineEnd); 
				dos.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(volume.getName()) + lineEnd);
		        dos.writeBytes("Content-Transfer-Encoding: binary\r\n");
		        dos.writeBytes(lineEnd);
				// create a buffer of maximum size 

				FileInputStream fis = new FileInputStream(volume);
				
				bytesAvailable = fis.available(); 
				bufferSize = Math.min(bytesAvailable, maxBufferSize); 
				buffer = new byte[bufferSize]; 
				bytesRead = fis.read(buffer, 0, bufferSize);
				while (bytesRead > 0) 
				{ 
					dos.write(buffer, 0, bufferSize); 
					bytesAvailable = fis.available(); 
					bufferSize = Math.min(bytesAvailable, maxBufferSize); 
					bytesRead = fis.read(buffer, 0, bufferSize); 
				} 

				// send multipart form data necesssary after file data... 

				dos.writeBytes(lineEnd); 
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd); 

				// close streams 
				fis.close(); 
				dos.flush(); 
				dos.close();				
			}
		} else
			connection.setRequestMethod("GET");
		
		RequestResult result = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}
		br.close();
		result = new RequestResult(connection.getResponseCode(), response.toString());
		connection.disconnect();
		return result;
	}

	public String getTransportModuleId() {
		return transportModuleId;
	}

	public void setTransportModuleId(String transportModuleId) {
		this.transportModuleId = transportModuleId;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public IDownloadManager getDownloadManager() {
		return downloadManager;
	}

	public void setDownloadManager(IDownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	public ISignatureProvider getSignatureProvider() {
		return signatureProvider;
	}

	public void setSignatureProvider(ISignatureProvider signatureProvider) {
		this.signatureProvider = signatureProvider;
	}

	public IHashProvider getHashProvider() {
		return hashProvider;
	}

	public void setHashProvider(IHashProvider hashProvider) {
		this.hashProvider = hashProvider;
	}

	public String getAdapterUrl() {
		return adapterUrl;
	}

	public void setAdapterUrl(String adapterUrl) {
		this.adapterUrl = adapterUrl;
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public ISyncEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(ISyncEnvironment environment) {
		this.environment = environment;
	}
}
