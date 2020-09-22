package ion.viewmodel.view;

import java.util.Collection;

public interface IFormTab {
	String getCaption();
	Collection<IField> getFullViewFields();
	Collection<IField> getShortViewFields();
}
