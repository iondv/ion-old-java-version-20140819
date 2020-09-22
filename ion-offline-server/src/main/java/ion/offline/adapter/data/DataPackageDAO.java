package ion.offline.adapter.data;

import java.io.File;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ion.core.IonException;
import ion.offline.server.dao.IDataPackageDAO;
import ion.offline.server.entity.DataPackage;
import ion.offline.server.entity.Point;

public class DataPackageDAO extends ion.offline.server.dao.DataPackageDAO implements IDataPackageDAO {

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public DataPackage CreateNewPackage(Point point, File directory_destination)
																																							throws IonException {
		return super.CreateNewPackage(point, directory_destination);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void DeletePackage(DataPackage data_package) throws IonException {
		super.DeletePackage(data_package);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public DataPackage AddDataPackage(DataPackage data_package)
																														 throws IonException {
		return super.AddDataPackage(data_package);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void updateDataPackage(DataPackage data_package) throws IonException {
		super.updateDataPackage(data_package);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public DataPackage GetLastPackage(int point) {
		return super.GetLastPackage(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public DataPackage GetFirstPackage(int point) {
		return super.GetFirstPackage(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public List<DataPackage> GetQueue(int count) {
		return super.GetQueue(count);
	}

}
