package ion.web.app.jstl;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class EvalUtils {
	
	private static Gson gs = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
	public static Boolean strcmp(Object o1, Object o2){
		if (o1 == null && o2 == null)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.toString().equals(o2.toString());
	}
	
	public static String jsEscape(String s){
		if (s == null)
			return "";
		return StringEscapeUtils.escapeJson(s);
	}
	
	public static String toJson(Object v){
		return gs.toJson(v);
	}
	
	public static String dateToStr(Date d){
		String result = "";
		if (d != null)
			try {
				result = DateFormat.getDateInstance(DateFormat.SHORT, LocaleContextHolder.getLocale()).format(d);
			} catch (Exception e) {
			}
		return result;
	}
	
	public static Boolean setContains(Set<String> s, String v){
		return s.contains(v);
	}
}
