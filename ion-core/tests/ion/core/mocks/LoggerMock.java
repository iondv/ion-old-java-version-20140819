package ion.core.mocks;

import java.io.PrintStream;

import ion.core.logging.ILogger;

public class LoggerMock implements ILogger {

	@Override
	public void Info(String message) {System.out.println("Info: "+message);}

	@Override
	public void Warning(String message) {System.out.println("Warning: "+message);}

	@Override
	public void Error(String message) {System.out.println("Error: "+message);}

	@Override
	public void Debug(String message) {System.out.println("Debug: "+message);}	
	
	@Override
	public void Warning(String message, Throwable e) {System.out.println("Warning: "+message); e.printStackTrace();}

	@Override
	public void Error(String message, Throwable e) {System.out.println("Error: "+message); e.printStackTrace();}

	@Override
	public PrintStream Out() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void FlushStream(short level) {
		// TODO Auto-generated method stub
		
	}
}
