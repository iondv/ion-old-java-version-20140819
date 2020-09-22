package com.svcdelivery.liquibase.eclipse.v3;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;

import org.osgi.framework.Bundle;

public class LbPluginClassResolver extends DefaultPackageScanClassResolver {

	private final Bundle bundle;
	
	public LbPluginClassResolver(Bundle bundle) {
		this.bundle = bundle;
	}
	
	@Override
	protected void find(final PackageScanFilter test, final String packageName, final Set<Class<?>> classes) {
		super.find(test, packageName, classes);
		if (bundle != null){
			Set<String> classNames = new HashSet<String>();
			Enumeration<URL> urls = bundle.findEntries(packageName.replace(".", "/"), "*.class", false);
			if (urls == null)
				urls = bundle.findEntries("bin/"+packageName.replace(".", "/"), "*.class", false);
			
			if (urls != null)
				while (urls.hasMoreElements()){
					File f = new File(urls.nextElement().getFile());
					classNames.add(packageName+"."+f.getName().replace(".class", ""));
				}
			
			for (String className : classNames) {
			        try {
			           Class<?> klass = bundle.loadClass(className);
			           if (test.matches(klass)) {
			                  classes.add(klass);
			           }
			        } catch (ClassNotFoundException e) {
			                e.printStackTrace();
			        }
			}
		}
	}

}