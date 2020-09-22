package ion.util.sync;

public class SyncUtils {
	public static String dbSanitiseName(String nm, String prefix){
		String result = prefix+"_"+nm.replaceAll("[^A-Za-z0-9_\\$]", "").replaceAll("([A-Za-z])_([A-Za-z])","$1__$2").replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
		if (result.length() > 60)
			return result = prefix + "_" +nm.hashCode();
		return result;
	}
	
	public static String RelationshipName(String className, String collection) {
		String result = dbSanitiseName(className, "r") + dbSanitiseName(collection, "");
		if (result.length() > 64)
			return "r_" + (className + "_" + collection).hashCode();
		return result;
	}
}
