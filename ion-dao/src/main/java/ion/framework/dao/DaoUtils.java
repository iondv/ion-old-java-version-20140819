package ion.framework.dao;

import ion.core.IonException;
import ion.core.MetaPropertyType;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class DaoUtils {
	@SuppressWarnings("incomplete-switch")
	public static Object cast(String value, MetaPropertyType t) throws IonException {
		if (!t.equals(MetaPropertyType.STRING)
				&& (value == null || value.trim().isEmpty()))
			return null;
		// TODO Здесь try - это костыль для смэв-оффлайна, на случай некорректных
		// входящих значений.
		try {
			switch (t) {
				case BOOLEAN:
					return Boolean.parseBoolean(value);
				case DATETIME: {
					SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
					SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
					SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat format4 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					try {
						return format1.parse(value);
					} catch (ParseException e) {
					}
					try {
						return format2.parse(value);
					} catch (ParseException e) {
					}
					try {
						return format3.parse(value);
					} catch (ParseException e) {
					}
					try {
						return format4.parse(value);
					} catch (ParseException e) {
						throw new IonException(e);
					}
				}
				case REAL:
					return Float.parseFloat(value);
				case DECIMAL:
					return BigDecimal.valueOf(Double.parseDouble(value));
				case SET:
				case INT:
					return Integer.parseInt(value);
			}
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public static <T> void merge(Map<String, Collection<T>> map, String key, T value){
		if(map.containsKey(key)){
			map.get(key).add(value);
		}else{
			Collection<T> collection = new LinkedList<T>();
			collection.add(value);
			map.put(key, collection);
		}
	}
}
