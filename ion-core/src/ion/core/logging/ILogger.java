package ion.core.logging;

import java.io.PrintStream;

public interface ILogger {
	
	public final short INFO = 1;
	public final short DEBUG = 2;
	public final short WARNING = 3;
	public final short ERROR = 4;
	
	
	public void Info(String message);
	public void Debug(String message);
	public void Warning(String message);
	public void Error(String message);
	
	public void Warning(String message, Throwable e);
	public void Error(String message, Throwable e);
	
	public PrintStream Out();
	public void FlushStream(short level);
}
