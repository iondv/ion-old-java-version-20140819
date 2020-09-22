package ion.modeler.deploy;

import java.util.Set;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;

import org.osgi.framework.Bundle;

import ion.modeler.deploy.JarClassIndexer;

/**
 * Package scan resolver.
 */
public class EmbeddedJarPackageScanClassResolver extends
                DefaultPackageScanClassResolver {

        private final Bundle bundle;

        private final JarClassIndexer indexer;

        public EmbeddedJarPackageScanClassResolver(Bundle bundle) {
                this.bundle = bundle;
                indexer = new JarClassIndexer();
                indexer.addJar(bundle.getEntry("/lib/snakeyaml-1.12.jar"));
        }

        @Override
        protected void find(final PackageScanFilter test, final String packageName,
                        final Set<Class<?>> classes) {
                Set<String> classNames = indexer.getClasses(packageName, true);
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
