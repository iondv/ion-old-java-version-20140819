package ion.framework.util;

import ion.core.IonException;
import ion.util.sync.HibernateMappingGenerator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.xml.parsers.ParserConfigurationException;

import org.h2.store.fs.FileUtils;

public class ModelToJPACompiler {
	
	private static List<File> getCompilationClassPath(){
		List<File> result = new LinkedList<File>();
		String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
		for (String path: paths)
			result.add(new File(path));
		return result;
	}	

    public static void compile(String[] args){
    	String sSrc = args[0];
    	String sDest = args[1];
    	String domain = args[2];
    	Boolean hbm = args.length > 3 && args[3].equals("hbm");
    	Boolean discriminator = args.length > 4 && args[4].equals("discr");
    	Boolean lazyload = args.length > 5 && args[5].equals("ll");
    	
    	File src = new File(sSrc);
    	File dest = new File(sDest);
    	if (!src.exists())
    		src.mkdirs();
    	if (!dest.exists())
    		dest.mkdirs();
    	else {
    		File domainDir = new File(dest, domain.replace(".", File.separator));
    		if (domainDir.exists())
    			FileUtils.deleteRecursive(domainDir.getAbsolutePath(), false);
    	}

    	HibernateMappingGenerator sourcesGen = new HibernateMappingGenerator();
    	sourcesGen.useDiscriminator = discriminator;
    	//sourcesGen.useLazyLoading = lazyload;
		File sourcesDir = new File(src,"src");
		sourcesDir.mkdirs();
		
		
		for (File model: src.listFiles(new FilenameFilter() {	
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".class.json");
			}
		})){
			try {
				sourcesGen.generateSourceFile(model, src, sourcesDir, domain);
				if (hbm)
					sourcesGen.generateHbmFile(model, src, dest, domain);
			} catch (IonException | IOException | ParserConfigurationException e) {
				e.printStackTrace(System.err);
			}
		}
		
		try {
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null)
				throw new IonException("Не удалось инициализировать компилятор Java!");
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(dest));
			fileManager.setLocation(StandardLocation.CLASS_PATH, getCompilationClassPath());
			
			final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourcesDir.listFiles()));
			
			boolean success = compiler.getTask(new OutputStreamWriter(System.out), fileManager, diagnostics, new LinkedList<String>(){
					private static final long serialVersionUID = 1L;	
					{
						add("-verbose");
						add("-source");
						add("1.7");
						add("-target");
						add("1.7");
					}}, null, compilationUnits).call();
				
			fileManager.flush();
			fileManager.close();
			 
			if (!success) {
		       for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
		                System.out.println("Code: " + diagnostic.getCode());
		                System.out.println("Kind: " + diagnostic.getKind());
		                System.out.println("Position: " + diagnostic.getPosition());
		                System.out.println("Start Position: " + diagnostic.getStartPosition());
		                System.out.println("End Position: " + diagnostic.getEndPosition());
		                System.out.println("Source: " + diagnostic.getSource());
		                System.out.println("Message: " + diagnostic.getMessage(Locale.getDefault()));
		            }
			}				
		} catch (IOException | IonException e) {
				e.printStackTrace(System.err);
		}    	
    }
    
    public static void main(String[] args) {
    	if (args.length > 0){
    		compile(args);
    	}
    }    
	
}
