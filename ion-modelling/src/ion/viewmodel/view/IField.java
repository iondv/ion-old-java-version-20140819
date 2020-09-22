package ion.viewmodel.view;

import java.util.Collection;

public interface IField extends Comparable<IField> {
	String getCaption();
	FieldType getType();
	String getProperty();
	Integer getOrderNumber();
	Boolean getRequired();
	String getVisibilityExpression();
	String getEnablementExpression();
	String getObligationExpression();
	Boolean isReadOnly();
	Collection<String> getValidators();
	String getHint();
	Collection<FieldAction> getActions();
}
