package ion.offline.server.dao;

import ion.core.IonException;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;

public interface IUserDAO {
  public void addUser(String name, Point point, String token) throws IonException;

  public void addUser(User user) throws IonException;
  
  public void attachUser(String name, Point point, String token) throws IonException;
  
  public User[] UsersByPoint(Point point) throws IonException;
  
  public User getUser(String login) throws IonException;
  
  public void ResetUsersTokens(Point point) throws IonException;
  
  public void DeleteUser(User user) throws IonException;
}
