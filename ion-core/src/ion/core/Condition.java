package ion.core;

import java.util.ArrayList;
import java.util.List;

public class Condition extends FilterOption implements Cloneable{
	
	private String _property;
	
	private ConditionType _type;
	
	private Object _value = null;
	
	private List<Condition> _valueConditions;
	
	public Condition(String property, ConditionType type, Object value, List<Condition> valueConditions){
		_property = property;
		_type = type;
		_value = value;
		_valueConditions = valueConditions;
	}
	
	public Condition(String property, ConditionType type, Object value) {
		this(property,type,value,new ArrayList<Condition>());
	}
	
	public Condition(String property, ConditionType type) {
		this(property,type,null);
	}
	
	public Condition clone() throws CloneNotSupportedException {
		return (Condition)super.clone();
	}

	public String Property() {
		return _property;
	}
	
	public ConditionType Type() {
		return _type;
	}
	
	public Object Value() {
		return _value;
	}
	
	public List<Condition> ValueConditions(){
		return _valueConditions;
	}
}
