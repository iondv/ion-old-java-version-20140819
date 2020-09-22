package ion.core;

import java.util.HashSet;
import java.util.Set;

public abstract class PrimitiveWrappers {

	@SuppressWarnings("serial")
	private final static Set<Class<?>> types = new HashSet<Class<?>>(){{
		add(String.class);
		add(Boolean.class);
		add(Character.class);
		add(Byte.class);
		add(Short.class);
		add(Integer.class);
		add(Long.class);
		add(Float.class);
		add(Double.class);
		add(Void.class);
	}};
	
	public static boolean is(Object o){
		return types.contains(o.getClass());
	}
}
