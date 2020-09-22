package ion.core;

public enum MetaPropertyType {
	STRING(0),
	TEXT(1),
	HTML(2),
	URL(3),
	IMAGE(4),
	FILE(5),
	INT(6),
	REAL(7),
	DECIMAL(8),
	DATETIME(9),
	BOOLEAN(10),
	PASSWORD(11),
	GUID(12),
	REFERENCE(13),
	COLLECTION(14),
	SET(15),
	STRUCT(16),
	CUSTOM(17),
	USER(18),
	PERIOD(60),
	GEO(100),
	FILESLIST(110);
	
    private final int v;

    private MetaPropertyType(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static MetaPropertyType fromInt(int v){
    	switch (v){
	    	case 0:return STRING;
	    	case 1:return TEXT;
	    	case 2:return HTML;
	    	case 3:return URL;
	    	case 4:return IMAGE;
	    	case 5:return FILE;
	    	case 6:return INT;
	    	case 7:return REAL;
	    	case 8:return DECIMAL;
	    	case 9:return DATETIME;
	    	case 10:return BOOLEAN;
	    	case 11:return PASSWORD;
	    	case 12:return GUID;
	    	case 13:return REFERENCE;
	    	case 14:return COLLECTION;
	    	case 15:return SET;
	    	case 16:return STRUCT;
	    	case 17:return CUSTOM;
	    	case 18:return USER;
	    	case 60:return PERIOD;
	    	case 100:return GEO;
	    	case 110:return FILESLIST;
    	}
    	return null;
    }    
}
