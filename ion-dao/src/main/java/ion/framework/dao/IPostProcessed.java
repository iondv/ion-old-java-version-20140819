package ion.framework.dao;

public interface IPostProcessed {
	void AfterPersist();
	void AfterUpdate();
	void AfterDelete();
}
