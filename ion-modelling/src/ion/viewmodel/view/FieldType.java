package ion.viewmodel.view;

import ion.core.MetaPropertyType;

public enum FieldType {
	GROUP(0),
	TEXT(1),
	REFERENCE(2),
	COLLECTION(3),
	CHECKBOX(4),
	COMBO(5),
	DATETIME_PICKER(6),
	MULTILINE(7),
	WYSIWYG(8),
	RADIO(9),
	MULTISELECT(10),
	FILE(11),
	PASSWORD(12),
	IMAGE(13),
	NUMBER_PICKER(14),
	DECIMAL_EDITOR(15),
	URL(17),
	PERIOD_PICKER(60),
	MAP(100),
	ATTACHMENTS(110);
	
    private final int v;

    private FieldType(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static FieldType fromInt(int v){
    	switch (v){
	    	case 0:return GROUP;
	    	case 1:return TEXT;
	    	case 2:return REFERENCE;
	    	case 3:return COLLECTION;
	    	case 4:return CHECKBOX;
	    	case 5:return COMBO;
	    	case 6:return DATETIME_PICKER;
	    	case 7:return MULTILINE;
	    	case 8:return WYSIWYG;
	    	case 9:return RADIO;
	    	case 10:return MULTISELECT;
	    	case 11:return FILE;
	    	case 12:return PASSWORD;
	    	case 13:return IMAGE;
	    	case 14:return NUMBER_PICKER;
	    	case 15:return DECIMAL_EDITOR;
	    	case 60:return PERIOD_PICKER;
	    	case 100:return MAP;
	    	case 110:return ATTACHMENTS;
    	}
    	return null;
    }
    
	public static FieldType fromPropertyType(MetaPropertyType t){
		switch (t){
			case BOOLEAN:return FieldType.CHECKBOX;
			case DATETIME:return FieldType.DATETIME_PICKER;
			case IMAGE:return FieldType.IMAGE;
			case FILE:return FieldType.FILE;
			case HTML:return FieldType.WYSIWYG;
			case TEXT:return FieldType.MULTILINE;
			case SET:return FieldType.MULTISELECT;
			case PASSWORD:return FieldType.PASSWORD;
			case REFERENCE:return FieldType.REFERENCE;
			case COLLECTION:return FieldType.COLLECTION;
			case DECIMAL:
			case INT:return FieldType.NUMBER_PICKER;
			case URL:return FieldType.URL;
			case GEO:return FieldType.MAP;
			case FILESLIST:return FieldType.ATTACHMENTS;
			default:return FieldType.TEXT;
		}
	}	    
}