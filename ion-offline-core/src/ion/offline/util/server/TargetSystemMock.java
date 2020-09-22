package ion.offline.util.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.integration.core.UserCredentials;
import ion.offline.net.ActionResult;
import ion.offline.net.AuthResult;
import ion.offline.net.DataDelta;
import ion.offline.net.DataUnit;
import ion.offline.net.MetaDelta;
import ion.offline.net.NavigationDelta;
import ion.offline.net.UserProfile;
import ion.offline.util.ITargetSystemAdapter;
import ion.offline.util.SyncDelta;
import ion.viewmodel.plain.StoredNavNode;

public class TargetSystemMock implements ITargetSystemAdapter {
	Random r = new Random();
	
	private String className;

	private String classCaption;
		
	private String keyName;
	
	private String semanticName;
	
	private String navCode;
	
	private String navTitle;
	
	private Map<String, String> properties;
	
	private int changeCount;
	
	private boolean metaSent = false;
	
	private int maxQueueLength = 0;
	
	private int callCount = 0;

	@Override
	public AuthResult[] authenticate(UserCredentials[] credentials) {
		AuthResult[] result = new AuthResult[credentials.length];
		for (int i = 0; i < credentials.length; i++)
			result[i] = new AuthResult(credentials[i].login, true, "aaa");
		return result;
	}
	
	private Object[] parsetType(String type){
		MetaPropertyType t = MetaPropertyType.STRING;
		short size = 0;
		if (type.contains(":")){
			String[] parts = type.split(":");
			type = parts[0];
			if (!parts[1].isEmpty())
				size = Short.parseShort(parts[1]);
		}
		
		if (type.equals("int"))
			t = MetaPropertyType.INT;
		else if (type.equals("bool"))
			t = MetaPropertyType.BOOLEAN;
		return new Object[]{t,size};
	}
	
	@SuppressWarnings("serial")
	@Override
	public SyncDelta getDelta(String client, String[] users, Date since, Integer syncHorizon) throws IonException {
		if (callCount >= maxQueueLength && maxQueueLength > 0)
			return null;
		UserProfile[] p = new UserProfile[users.length];
		for (int i = 0; i < users.length; i++)
			p[i] = new UserProfile(users[i]);

		StoredClassMeta[] cm = null;
		StoredNavNode[] nav = null;
		if (!metaSent){
			cm = new StoredClassMeta[1];
			List<StoredPropertyMeta> props = new ArrayList<StoredPropertyMeta>();
			for (Map.Entry<String, String> prop: properties.entrySet()){
				Object[] type = parsetType(prop.getValue());
				props.add(new StoredPropertyMeta(prop.getKey(),prop.getKey(),((MetaPropertyType)type[0]).getValue(),(short)type[1]));
			}
		
			cm[0] = new StoredClassMeta(false, new LinkedList<String>(){{ add(keyName); }}, className, classCaption, semanticName, props);	
			nav = new StoredNavNode[2];
			nav[0] = new StoredNavNode(0,navCode, navTitle,null);
			nav[1] = new StoredNavNode(0,navCode+"."+className, classCaption, className);
			metaSent = true;
		}
		
		LinkedList<DataUnit> changes = new LinkedList<DataUnit>();
		for (int i = 0; i < changeCount; i++){
			String id = String.valueOf(r.nextInt());
			Map<String, Object> data = new HashMap<String, Object>();
			for (Map.Entry<String, String> prop: properties.entrySet()){
				Object v = null;
				Object[] type = parsetType(prop.getValue());
				MetaPropertyType t = (MetaPropertyType)type[0];
				short size = (short)type[1];
				
				if (t == MetaPropertyType.STRING)
					v = "s"+r.nextInt();
					if (size > 0 && size < v.toString().length())
						v = v.toString().substring(0, size);
				else if (t == MetaPropertyType.INT)
					v = r.nextInt();
				else if (t == MetaPropertyType.BOOLEAN)
					v = r.nextBoolean();
				if (prop.getKey().equals(keyName))
					id = v.toString();
				data.put(prop.getKey(), v);
			}
			DataUnit du = new DataUnit(id, className, data);
			changes.add(du);
		}
		callCount++;
		return new SyncDelta(p, (cm != null)?new MetaDelta(cm):null, (nav != null)?new NavigationDelta(nav):null, null, new DataDelta(changes.iterator()), new HashMap<String, Iterator<DataUnit>>());
	}

	@Override
	public ActionResult push(String authToken, DataUnit data) {
		System.out.println("Приняты изменения в объекте " + data.className + "@" + data.id + ".");
		return new ActionResult();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public int getChangeCount() {
		return changeCount;
	}

	public void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getSemanticName() {
		return semanticName;
	}

	public void setSemanticName(String semanticName) {
		this.semanticName = semanticName;
	}

	public String getNavCode() {
		return navCode;
	}

	public void setNavCode(String navCode) {
		this.navCode = navCode;
	}

	public String getNavTitle() {
		return navTitle;
	}

	public void setNavTitle(String navTitle) {
		this.navTitle = navTitle;
	}

	public int getMaxQueueLength() {
		return maxQueueLength;
	}

	public void setMaxQueueLength(int maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}

	public String getClassCaption() {
		return classCaption;
	}

	public void setClassCaption(String classCaption) {
		this.classCaption = classCaption;
	}
}
