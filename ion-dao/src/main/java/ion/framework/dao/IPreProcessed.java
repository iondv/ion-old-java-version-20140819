package ion.framework.dao;

public interface IPreProcessed {
	Boolean BeforePersist();
	Boolean BeforeUpdate();
	Boolean BeforeDelete();
}
