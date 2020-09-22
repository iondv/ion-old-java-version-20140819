package ion.util.sync;

import java.util.Arrays;
import java.util.Comparator;

public class IonDBNameProvider implements IDBNameProvider {
	
	private boolean upper_case;
	
	private static String separator = "_";
		
	public IonDBNameProvider(boolean upper_case) {
		this.upper_case = upper_case;
	}	

	private String toSnakeCase(String word){
		return word.replaceAll("[^A-Za-z0-9_]", "").replaceAll("([A-Za-z])_([A-Za-z])","$1__$2").replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
	
	@Override
	public String getName(String nm, DBNameType type) {
		return getName(new String[]{nm}, type);
	}

	@Override
	public String getName(String[] nm, DBNameType type) {
		String result;
		if(type != null)
			switch(type){
				case TABLE: result="t"+separator; break;
				case COLUMN: result="f"+separator; break;
				case FOREIGN_KEY: result="fk"+separator; break;
				case INDEX: result="ix"+separator; break;
				default: result = "";
			}
		else
			result = "";
		for(int i = 0; i< nm.length; i++)
			result += toSnakeCase(nm[i]) + ((i<(nm.length-1))?separator:"");
		return upper_case?result.toUpperCase():result;
	}

	@Override
	public String[] sortWords(String[] words) {
		Arrays.sort(words, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		return words;
	}

}
