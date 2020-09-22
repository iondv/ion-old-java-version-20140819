package ion.core;

import ion.core.Sorting;

import java.util.ArrayList;
import java.util.Collection;

public class ListOptions implements Cloneable {
	Collection<FilterOption> _filter;
	Collection<Sorting> _sorting;
	
	Integer _page_size = null;
	Integer _page = 1;
	Long _total = null;
			
	public ListOptions(Collection<FilterOption> filter, Collection<Sorting> sorting, Integer page_size, Integer page, Long total){
		_filter = (filter != null)?filter:new ArrayList<FilterOption>();
		_sorting = (sorting != null)?sorting:new ArrayList<Sorting>();
		_page_size = page_size;
		_page = page;
		_total = total;
	}

	@SuppressWarnings("unchecked")
	public ListOptions clone() throws CloneNotSupportedException {
		ListOptions lo = (ListOptions)super.clone();
		Collection<FilterOption> filter = lo._filter;
		Collection<Sorting> sorting = lo._sorting;
		
		try {
			lo._filter = filter.getClass().newInstance();
			for (FilterOption c: filter)
				lo._filter.add(c.clone());
			lo._sorting = sorting.getClass().newInstance();
			for (Sorting s: sorting)
				lo._sorting.add(s.clone());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		}
		
		return lo;
	}	
	
	public ListOptions(Collection<FilterOption> filter, Collection<Sorting> sorting, Integer page_size, Integer page){
		this(filter,sorting,page_size,page,null);
	}
	
	public ListOptions(Collection<FilterOption> filter, Collection<Sorting> sorting, Integer page_size){
		this(filter,sorting,page_size,1,null);
	}	
	
	public ListOptions(Collection<FilterOption> filter, Collection<Sorting> sorting){
		this(filter,sorting,null,1,null);
	}
			
	public ListOptions(Collection<FilterOption> filter){
		this(filter,null,null,1,null);
	}
	
	public ListOptions(Collection<FilterOption> filter, Integer page_size){
		this(filter,null,page_size,1,null);
	}
	
	public ListOptions(Collection<FilterOption> filter, Integer page_size, Integer page){
		this(filter,null,page_size,page,null);
	}
	
	public ListOptions(Collection<FilterOption> filter, Integer page_size, Integer page, Long total){
		this(filter,null,page_size,page,total);
	}	
	
	public ListOptions(Integer page_size){
		this(null, null, page_size, 1, null);
	}
	
	public ListOptions(Integer page_size, Integer page){
		this(null, null, page_size, page, null);
	}
	
	public ListOptions(){
		this(null, null, null, 1, null);		
	}
	
	public Collection<FilterOption> Filter() {
		return _filter;
	}
	
	public Collection<Sorting> Sorting() {
		return _sorting;
	}
	
	public Integer Page() {
		return _page;
	}
	
	public Integer PageSize() {
		return _page_size;
	}
	
	public void SetTotalCount(Long n) {
		_total = n;
	}
	
	public Long TotalCount() {
		return _total;
	}

	public void SetPage(Integer page) {
		this._page = page;
	}

	public void SetPageSize(Integer size) {
		this._page_size = size;
	}
}
