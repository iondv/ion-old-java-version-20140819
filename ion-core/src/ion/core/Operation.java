package ion.core;

public class Operation extends FilterOption{
	private OperationType type;
	private FilterOption[] operands;
	
	public OperationType getType() {
		return type;
	}
	public void setType(OperationType type) {
		this.type = type;
	}
	public FilterOption[] getOperands() {
		return operands;
	}
	public void setOperands(FilterOption[] operands) {
		this.operands = operands;
	}
	
	public Operation() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Operation(OperationType type, FilterOption[] operands) {
		super();
		this.type = type;
		this.operands = operands;
	}
	
	
}
