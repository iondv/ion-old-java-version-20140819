package ion.core.digisign;

import ion.core.IItem;
import ion.core.IStructMeta;
import ion.core.IonException;

public interface IDigiSignDataProvider {
	boolean hasData(IStructMeta c, String action) throws IonException;
	
	DataForSign getData(IItem item, String action) throws IonException;
}
