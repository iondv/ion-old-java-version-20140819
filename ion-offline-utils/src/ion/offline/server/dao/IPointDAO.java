package ion.offline.server.dao;

import ion.core.IonException;
import ion.offline.server.entity.Point;

import java.io.Serializable;

public interface IPointDAO {
  public Point addPoint() throws IonException;

  public void updatePoint(Point point) throws IonException;
  
  public void DeletePoint(Point point) throws IonException;
    
  public Point GetPointById(Serializable point_id);
  
  public Point[] GetPoints();

}
