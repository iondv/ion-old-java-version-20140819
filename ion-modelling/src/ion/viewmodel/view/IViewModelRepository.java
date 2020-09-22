package ion.viewmodel.view;

import java.util.Map;

import ion.core.IStructMeta;

public interface IViewModelRepository {
	IListViewModel getListViewModel(String node, IStructMeta meta);
	IListViewModel getListViewModel(IStructMeta meta);
	IListViewModel getCollectionViewModel(String node, IStructMeta meta, String collection);
	IListViewModel getCollectionViewModel(IStructMeta meta, String collection);	
	IFormViewModel getItemViewModel(String node, IStructMeta meta);
	IFormViewModel getItemViewModel(IStructMeta meta);
	IFormViewModel getCreationViewModel(String node, IStructMeta meta);
	IFormViewModel getCreationViewModel(IStructMeta meta);
	IFormViewModel getDetailViewModel(String node, IStructMeta meta);
	IFormViewModel getDetailViewModel(IStructMeta meta);
	String getMask(String name);
	Map<String,Validator> getValidators();
}
