package ion.web.app.ajax;

import java.util.Collection;

public class ModelInfo {

	String	                classCaption;
	Long	                  pagesCount;
	Collection<Breadcrumbs>	breadcrumbs;

	public Collection<Breadcrumbs> getBreadcrumbs() {
		return breadcrumbs;
	}

	public void setBreadcrumbs(Collection<Breadcrumbs> breadcrumbs) {
		this.breadcrumbs = breadcrumbs;
	}

	public String getClassCaption() {
		return classCaption;
	}

	public void setClassCaption(String classCaption) {
		this.classCaption = classCaption;
	}

	public Long getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(Long pagesCount) {
		this.pagesCount = pagesCount;
	}

	public ModelInfo(String classCaption, Long pagesCount,
	                 Collection<Breadcrumbs> breadcrumbs) {
		super();
		this.classCaption = classCaption;
		this.pagesCount = pagesCount;
		this.breadcrumbs = breadcrumbs;
	}

	public ModelInfo(String classCaption, Long pagesCount) {
		super();
		this.classCaption = classCaption;
		this.pagesCount = pagesCount;
	}

	public ModelInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
}
