package ion.core.data;

import ion.core.ICollectionProperty;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IonException;

import java.util.Collection;

public class CollectionProperty extends Property implements ICollectionProperty {

	IItem[] items;

	Long total_count;

	public CollectionProperty(String name, Item item, ICollectionPropertyMeta meta) {
		super(name, item, meta);
	}

	@Override
	public Object getValue() throws IonException {
		return (Object) getItems();
	}

	@Override
	public String getString() {
		try {
			return String.valueOf(getItemCount());
		} catch (IonException e) {
		}
		return "0";
	}

	@Override
	public IItem[] getItems() throws IonException {
		if (items == null) {
				Collection<IItem> tmp = ((Item) container).rep.GetAssociationsList(container, getName());
				if (tmp != null)
					items = tmp.toArray(new IItem[tmp.size()]);
		}
		return items;
	}

	@Override
	public long getItemCount() throws IonException {
		if (total_count == null) {
			if (items != null)
				total_count = (long) items.length;
			else
				total_count = ((Item) container).rep.GetAssociationsCount(container, getName());
		}
		return (total_count != null) ? total_count : 0;
	}
}
