package ion.viewmodel.view;

public class ListReferenceColumn extends ListColumn implements IListReferenceColumn {
	
	protected boolean selectionPaginated = true;
	
	public ListReferenceColumn(String caption, String property, FieldSize size, Boolean sortable, Integer order_number, Boolean readonly, Boolean selectionPaginated, String hint) {
		super(caption, property, FieldType.REFERENCE, size, sortable, order_number, readonly, hint);
		this.selectionPaginated = selectionPaginated;
	}	
		
	@Override
	public Boolean isSelectionPaginated() {
		// TODO Auto-generated method stub
		return selectionPaginated;
	}

}
