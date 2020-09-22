package ion.core;

import java.util.HashSet;
import java.util.Set;

public enum DACPermission {
	READ (1),
	WRITE (2),
	DELETE (4),
	USE (8),
	FULL (31);
	
    private final int v;

    private DACPermission(final int code) {
        v = code;
    }

    public int getValue() { return v; }	
    
    public static Set<DACPermission> parseInt(Integer v){
		Set<DACPermission> result = new HashSet<DACPermission>();
		for (DACPermission p : DACPermission.values())
			if ((p.getValue() & v.intValue()) == p.getValue())
				result.add(p);		
		return result;    	
    }
}
