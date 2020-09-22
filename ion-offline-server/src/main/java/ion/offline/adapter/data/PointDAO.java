package ion.offline.adapter.data;

import java.io.Serializable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ion.core.IonException;
import ion.offline.server.dao.IPointDAO;
import ion.offline.server.entity.Point;

public class PointDAO extends ion.offline.server.dao.PointDAO implements IPointDAO {

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public Point addPoint() throws IonException {
		return super.addPoint();
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void updatePoint(Point point) throws IonException {
		super.updatePoint(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void DeletePoint(Point point) throws IonException {
		super.DeletePoint(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public Point GetPointById(Serializable point_id) {
		return super.GetPointById(point_id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public Point[] GetPoints() {
		return super.GetPoints();
	}

}
