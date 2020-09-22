package ion.viewmodel.view;

import java.util.HashMap;
import java.util.Map;

public enum ActionType {
	CREATE(1),
	ADD(2),
	EDIT(4),
	SAVE(8),
	CANCEL(16),
	REMOVE(32),
	DELETE(64),
	REFRESH(128);
	
    private final int v;

    private ActionType(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static ActionType fromInt(int v){
    	switch (v){
	    	case 1:return CREATE;
	    	case 4:return EDIT;
	    	case 64:return DELETE;
	    	case 8:return SAVE;
	    	case 16:return CANCEL;
	    	case 128:return REFRESH;
	    	case 2:return ADD;
	    	case 32:return REMOVE;
    	}
    	return null;
    }
    
	@SuppressWarnings("serial")
	private static Map<ActionType, String> captions = new HashMap<ActionType, String>(){{
		put(ActionType.CREATE,  "Создать");
		put(ActionType.EDIT,    "Изменить");
		put(ActionType.DELETE,  "Удалить");
		put(ActionType.SAVE,    "Сохранить");
		put(ActionType.CANCEL,  "Отменить");
		put(ActionType.REFRESH, "Обновить");
		put(ActionType.ADD, "Добавить");
		put(ActionType.REMOVE, "Убрать");
	}};
    
    public String getCaption() {
    	return captions.get(this);
    }
}