package ion.core.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ion.core.logging.ILogger;

public abstract class AbstractIonLogger implements ILogger {
	
	Logger logger;

	private ByteArrayOutputStream outStream;
	
	private PrintStream printStream;
	
	protected void Initialize(String name) {
		logger = LoggerFactory.getLogger(name);
	}
	
	@SuppressWarnings("rawtypes")
  protected void Initialize(Class clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}
	
	@Override
	public void Info(String message) {
		logger.info(message);
	}

	@Override
	public void Warning(String message) {
		logger.warn(message);
	}

	@Override
	public void Error(String message) {
		logger.error(message);
	}
	
	@Override
	public void Debug(String message) {
		logger.debug(message);
	}	

	@Override
	public void Warning(String message, Throwable e) {
		logger.warn(message, e);
	}

	@Override
	public void Error(String message, Throwable e) {
		logger.error(message, e);
	}
	
	@Override
	public PrintStream Out() {
		if (printStream == null) { 
			outStream = new ByteArrayOutputStream();
			try {
				printStream = new PrintStream(outStream, false, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return printStream;
	};

	@Override
	public void FlushStream(short level) {
		if (printStream != null && outStream != null) {
			String message;
			try {
				printStream.flush();
				message = new String(outStream.toByteArray(), "utf-8");
				switch (level){
					case ILogger.INFO:logger.info(message);break;
					case ILogger.DEBUG:logger.debug(message);break;
					case ILogger.WARNING:logger.warn(message);break;
					case ILogger.ERROR:logger.error(message);break;
				}
				outStream.reset();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

}
