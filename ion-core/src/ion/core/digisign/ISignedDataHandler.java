package ion.core.digisign;

import ion.core.IonException;

import java.util.Map;

public interface ISignedDataHandler {
	public void process(String ItemId, String action, Map<String, String> attributes, String data, String signature) throws IonException;
	public void process(String ItemId, String action, Map<String, String> attributes, DataPart[] parts, String[] signatures) throws IonException;
}
