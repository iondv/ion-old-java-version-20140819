package ion.offline.server.dao;

import ion.core.IonException;
import ion.offline.server.entity.DataPackage;
import ion.offline.server.entity.Point;

import java.io.File;
import java.util.List;

public interface IDataPackageDAO {
  public DataPackage CreateNewPackage(Point point, File directory_destination) throws IonException;
  public void DeletePackage(DataPackage data_package) throws IonException;
  public DataPackage AddDataPackage(DataPackage data_package) throws IonException;
  public void updateDataPackage(DataPackage data_package) throws IonException;
  public DataPackage GetLastPackage(int point);
  public DataPackage GetFirstPackage(int point);
  public List<DataPackage> GetQueue(int count);
}
