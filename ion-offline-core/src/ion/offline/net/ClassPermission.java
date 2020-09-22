package ion.offline.net;

import java.util.HashSet;
import java.util.Set;

public enum ClassPermission {
	READ (1),
	CREATE (3),
	UPDATE (5),
	DELETE (9);
	
    private final int v;

    private ClassPermission(final int code) {
        v = code;
    }

    public int getValue() { return v; }	
    
    public static Set<ClassPermission> parseInt(Integer v){
		Set<ClassPermission> result = new HashSet<ClassPermission>();
		for (ClassPermission p : ClassPermission.values())
			if ((p.getValue() & v.intValue()) != 0)
				result.add(p);		
		return result;    	
    }
}
