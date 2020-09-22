package ion.framework.offline.client;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jdbc.Work;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import ion.auth.dao.UserDaoImpl;
import ion.auth.persistence.Authority;
import ion.auth.persistence.User;
import ion.auth.persistence.UserProperty;
import ion.auth.persistence.UserPropertyId;
import ion.core.DACPermission;
import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.auth.UserContext;
import ion.core.logging.IChangelogRecord;
import ion.core.logging.ILogger;
import ion.core.logging.IonLogger;
import ion.core.meta.StructPropertyMeta;
import ion.core.storage.SimpleFileStorage;
import ion.framework.acl.DbAcl;
import ion.framework.acl.dao.DbAclDao;
import ion.framework.acl.entity.AccessRecord;
import ion.framework.acl.entity.AccessRecordId;
import ion.framework.changelog.IonChangelog;
import ion.framework.changelog.domain.StoredChangelogRecord;
import ion.framework.dao.jdbc.IJdbcConnectionProvider;
import ion.framework.digisign.DigitalSignature;
import ion.framework.digisign.DigitalSignatureDAO;
import ion.framework.meta.IonMetaRepository;
import ion.offline.filesystem.FileUtils;
import ion.offline.net.ClassPermission;
import ion.offline.net.DataChange;
import ion.offline.net.DataChangeType;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;
import ion.offline.sync.ISyncEnvironment;
import ion.smev.client.entity.NotReadStatus;
import ion.smev.client.repo.JdbcOfflineDataRepository;
import ion.util.sync.ModelDeployer;
//import ion.viewmodel.com.JSONNavigationModel;
//import ion.viewmodel.com.JSONViewModelRepository;

public class OfflineSyncEnvironment implements ISyncEnvironment {
	
	private ILogger logger;
	
	private SimpleFileStorage fileStorage;	
	
	private DbAclDao aclDao;
	
	private DbAcl acl;
	
	private DataSource dataSource;

	private JdbcOfflineDataRepository dataRepo;
	
	private UserDaoImpl userdao;
	
	private DigitalSignatureDAO signDao;
		
	private IonChangelog changeLog;
	
	private IonMetaRepository metaRepo;
	
	//private JSONNavigationModel navModel;
	
	//private JSONViewModelRepository viewModel;
	
	private SessionFactory sessionFactory;
	
	private boolean needProfileReload = false;

	private boolean needMetaReload = false;
		
	private Properties settings;
	
	private File classesDir;
	
	private File workDir;
	
	private File tmpDir;
	
	//private File mappingDir;
		
	private File auxDataDir;
	
	private URL reloadProfilesUrl;
	
	private URL reloadMetaUrl;
	
	private URL credentialsUrl;
	
	private URL offlineSyncInfoUrl;
	
	private ClassLoader threadClassLoader;
	
	private void loadOrm() throws IonException, MalformedURLException, ClassNotFoundException{
		if (threadClassLoader == null)
			threadClassLoader = Thread.currentThread().getContextClassLoader();
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{classesDir.toURI().toURL()},threadClassLoader);

		Thread.currentThread().setContextClassLoader(urlClassLoader);		
		sessionFactory = createSessionFactory(settings.getProperty("model.package"));
	}
	
	public OfflineSyncEnvironment(Properties props) throws IonException, MalformedURLException, ClassNotFoundException, SQLException, PropertyVetoException {
		settings = props;
		
		logger = new IonLogger("client daemon");
		
		if (settings.containsKey("model.paths.aux")){
			auxDataDir = new File(settings.getProperty("model.paths.aux"));
			if(!auxDataDir.exists()) auxDataDir.mkdirs();
		} else
			throw new IonException("Не указана директория дополнительных данных [model.paths.aux]!");
		
		classesDir = new File(settings.getProperty("model.paths.classes"));
		if (!classesDir.exists()) classesDir.mkdirs();
		
		/*
		mappingDir = new File("src"+File.separator+settings.getProperty("model.package").replace(".", File.separator));
		if(!mappingDir.exists()) mappingDir.mkdirs();
		*/
		
		dataSource = new ComboPooledDataSource();
		if (!settings.containsKey("connection.driver_class"))
			throw new IonException("Не указан класс драйвера БД [connection.driver_class]!");
		((ComboPooledDataSource)dataSource).setDriverClass(settings.getProperty("connection.driver_class")); 
		if (!settings.containsKey("connection.url"))
			throw new IonException("Не указан URL БД [connection.url]!");
		((ComboPooledDataSource)dataSource).setJdbcUrl(settings.getProperty("connection.url")); 
		if (!settings.containsKey("connection.username"))
			throw new IonException("Не указано имя пользователя БД [connection.username]!");
		((ComboPooledDataSource)dataSource).setUser(settings.getProperty("connection.username")); 
		if (!settings.containsKey("connection.password"))
			throw new IonException("Не указан пароль пользователя БД [connection.password]!");
		((ComboPooledDataSource)dataSource).setPassword(settings.getProperty("connection.password"));
		if (settings.containsKey("c3p0.min_size"))
			((ComboPooledDataSource)dataSource).setMinPoolSize(Integer.parseInt(settings.getProperty("c3p0.min_size")));
		if (settings.containsKey("c3p0.idle_test_period"))
			((ComboPooledDataSource)dataSource).setIdleConnectionTestPeriod(Integer.parseInt(settings.getProperty("c3p0.idle_test_period")));
		if (settings.containsKey("c3p0.max_size"))
			((ComboPooledDataSource)dataSource).setMaxPoolSize(Integer.parseInt(settings.getProperty("c3p0.max_size")));
		
		loadOrm();
		
		if (settings.containsKey("daemon.paths.work"))
			workDir = new File(settings.getProperty("daemon.paths.work"));
		else
			workDir = new File("./sync");
		
		if (settings.containsKey("daemon.paths.tmp"))
			tmpDir = new File(settings.getProperty("daemon.paths.tmp"));
		else
			tmpDir = new File("./tmp");
		
		metaRepo = new IonMetaRepository();
		
		fileStorage = new SimpleFileStorage();
		if (settings.containsKey("dir.storage_root"))
			fileStorage.setStorageRoot(new File(settings.getProperty("dir.storage_root")));
		
		
		aclDao = new DbAclDao();
		aclDao.setSessionFactory(sessionFactory);
		
		acl = new DbAcl();
		acl.setSessionFactory(sessionFactory);
		
		dataRepo = new JdbcOfflineDataRepository(new IJdbcConnectionProvider() {
			@Override
			public Connection getConnection() throws IonException {
					return ((SessionImpl)sessionFactory.getCurrentSession()).connection();
			}
		});
		dataRepo.setMetaRepository(metaRepo);
		/*
		dataRepo.setAuthContext(new IAuthContext() {
			@Override
			public IUserContext CurrentUser() {
				return null;
			}
		});
		*/
		userdao = new UserDaoImpl();
		userdao.setSessionFactory(sessionFactory);
		
		signDao = new DigitalSignatureDAO();
		signDao.setSessionFactory(sessionFactory);
		
		metaRepo.setLogger(logger);
		//metaRepo.setHqlSelectionProvider(dataRepo);
		
		if (settings.containsKey("model.paths.meta"))
			metaRepo.setMetaDirectory(new File(settings.getProperty("model.paths.meta")));
		else
			throw new IonException("Не указана директория классов [model.paths.meta]!");
		
		changeLog = new IonChangelog();
		changeLog.setSessionFactory(sessionFactory);
		
		/*
		if (settings.containsKey("model.paths.navigation")) {
  		navModel = new JSONNavigationModel();
  		navModel.setLogger(logger);
  		navModel.setMetaRepository(metaRepo);
  		navModel.setModelDirectory(new File(settings.getProperty("model.paths.navigation")));
		} else
			throw new IonException("Не указана директория моделей навигации [model.paths.navigation]!");
		*/
		/*
		if (settings.containsKey("model.paths.navigation")) {
			viewModel = new JSONViewModelRepository();
			viewModel.setLogger(logger);
			viewModel.setModelsDirectory(new File(settings.getProperty("model.paths.views")));	
		}
		else
			throw new IonException("Не указана директория моделей представления [model.paths.views]!");
		*/
		if (settings.containsKey("ion.web.urls.reloadProfiles"))
			reloadProfilesUrl = new URL(settings.getProperty("ion.web.urls.reloadProfiles"));
		else
			throw new IonException("Не указан URL обновления профилей [ion.web.urls.reloadProfiles]!");

		if (settings.containsKey("ion.web.urls.reloadMeta"))
			reloadMetaUrl = new URL(settings.getProperty("ion.web.urls.reloadMeta"));
		else
			throw new IonException("Не указан URL обновления репозиториев [ion.web.urls.reloadMeta]!");
		
		
		if (settings.containsKey("ion.web.urls.credentials"))
			credentialsUrl = new URL(settings.getProperty("ion.web.urls.credentials"));
		else
			throw new IonException("Не указан URL учетных записей [ion.web.urls.credentials]!");

		
		if (settings.containsKey("ion.web.urls.offlineSyncInfo"))
			offlineSyncInfoUrl = new URL(settings.getProperty("ion.web.urls.offlineSyncInfo"));
		else
			throw new IonException("Не указан URL оповещения о времени последнего обмена [ion.web.urls.offlineSyncInfo]!");
	}
	
	public ILogger getLogger(){
		return logger;
	}
	
	private SessionFactory createSessionFactory(String packageName) throws IonException, ClassNotFoundException {	
		Configuration configuration = new Configuration();
		configuration.setProperties(settings);
		configuration.addAnnotatedClass(StoredChangelogRecord.class);
		configuration.addAnnotatedClass(User.class);
		configuration.addAnnotatedClass(UserProperty.class);
		configuration.addAnnotatedClass(UserPropertyId.class);
		configuration.addAnnotatedClass(Authority.class);
		configuration.addAnnotatedClass(AccessRecord.class);
		configuration.addAnnotatedClass(AccessRecordId.class);
		configuration.addAnnotatedClass(DigitalSignature.class);
		configuration.addAnnotatedClass(NotReadStatus.class);
		/*
		File packageFolder = new File(classesDir,packageName.replace(".", File.separator));
		packageFolder.mkdirs();
		if (packageFolder.exists())
			for (String fn: packageFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			})){
				Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(packageName+"."+fn.replace(".class",""));
				configuration.addAnnotatedClass(c);
			}
		
		configuration.addDirectory(packageFolder);
		*/
		configuration.buildMappings();
		/*
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		return configuration.buildSessionFactory(serviceRegistry);
		*/
		//configuration.configure();
		
		StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder();
		srb.applySettings(configuration.getProperties());
		srb.applySetting(Environment.DATASOURCE, dataSource);
		return configuration.buildSessionFactory(srb.build());
	}	
	
	@Override
	public File getDomainModelDirectory() {
		return metaRepo.getMetaDirectory();
	}

	@Override
	public File getNavigationModelDirectory() {
		return new File(settings.getProperty("model.paths.navigation"), "SIDEBAR");
	}

	/*
	private List<File> getCompilationClassPath(){
		List<File> result = new LinkedList<File>();
		String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
		for (String path: paths)
			result.add(new File(path));
		return result;
	}
	*/
	
	private void processDeletions(String[] deletions, File dest) {
		processDeletions(deletions, dest, ".json");
	}

	private void processDeletions(String[] deletions, File dest, String fileSuffix){
		for (String code : deletions){
			File del = new File(dest, code + fileSuffix);
			if (del.exists())
				del.delete();
		}
	}	
	
	@Override
	public void adjustNavigationMeta(File src, String[] deletions) throws IonException {
		try {
			File dest = getNavigationModelDirectory();
			processDeletions(deletions, dest);			
			for (File f: src.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".json");
				}
			})){
				Files.move(f.toPath(), new File(dest, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			//navModel.setModelDirectory(dest);
		} catch (Exception e){
			logger.Error("Не удалось обновить модель навигации!", e);
			throw new IonException(e); 
		}
	}
	
	@Override
	public void adjustViewMeta(File src, String[] deletions)
			throws IonException {
		try {
			File dest = new File(settings.getProperty("model.paths.views"));
			processDeletions(deletions, dest);
			for (File f: src.listFiles(new FileFilter() {
				@Override
				public boolean accept(File check) {
					return check.isDirectory();
				}
			})){
				FileUtils.move(dest, f);
			}
			//viewModel.setModelsDirectory(dest);
		} catch (Exception e){
			logger.Error("Не удалось обновить модель представления!", e);
			throw new IonException(e); 
		}
	}	

	@Override
	public void adjustAuxData(File src)
			throws IonException {
		try {
			File dest = getAuxDataDirectory();
			for (File chapter: src.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return dir.isDirectory();
				}
			})){
				File destChapter = new File(dest, chapter.getName());
				if (!destChapter.exists())
					destChapter.mkdirs();

				for (File aux: chapter.listFiles()){
					FileUtils.move(destChapter, aux);
				}				
			}
		} catch (Exception e){
			logger.Error("Не удалось обновить дополнительные данные!", e);
			throw new IonException(e); 
		}
	}		

	public File getAuxDataDirectory() {
		return auxDataDir;
	}
	
	public void setAuxDataDirectory(File value) {
		this.auxDataDir = value;
	}
	
	@Override
	public void adjustStorageMeta(File src, String[] deletions) throws IonException {
		needMetaReload = false;
		/*
		boolean lazyLoading = false;
		boolean discr = false;
		boolean hbm = false;
		if (settings.containsKey("mapping.useLazyLoading"))
			lazyLoading = Boolean.parseBoolean(settings.getProperty("mapping.useLazyLoading"));
		if (settings.containsKey("storage.useDiscriminator"))
			discr = Boolean.parseBoolean(settings.getProperty("storage.useDiscriminator"));
		if (settings.containsKey("mapping.useXml"))
			hbm = Boolean.parseBoolean(settings.getProperty("mapping.useXml"));
		*/
		File metaDir = getDomainModelDirectory();
		processDeletions(deletions, metaDir);
		try {
			for (File c: src.listFiles(new FilenameFilter() {	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class.json");
				}
			})){
				needMetaReload = true;
				Files.move(c.toPath(), new File(metaDir,c.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
			}
		} catch (Exception e){
			throw new IonException(e);
		}
				
		File patchesDir = new File(getSyncWorkingDirectory(),"patches");
		patchesDir.mkdirs();
		final ModelDeployer md = new ModelDeployer("ion", metaDir.getAbsolutePath(), patchesDir.getAbsolutePath(), false);
		md.setUseDiscriminator(true);
		begin();
		try {
			sessionFactory.getCurrentSession().doWork(new Work() {
				@Override
				public void execute(Connection connection) throws SQLException {
					try {
						md.Deploy(connection, "");
					} catch (SQLException e){
						throw e;
					} catch (Exception e) {
						throw new SQLException(e);
					}					
				}
			});
			commit();
		} catch (Exception e) {
			rollback();
			needMetaReload = false;
			logger.Error("Не удалось развернуть доменную модель!", e);
			throw new IonException("Не удалось развернуть доменную модель!", e);
		} finally {
			FileUtils.delete(patchesDir);			
		}
		/*
		if (classesDir != null){
			
			HibernateMappingGenerator sourcesGen = new HibernateMappingGenerator();
			sourcesGen.useDiscriminator = discr;
			sourcesGen.useLazyLoading = lazyLoading;
			
			File sourcesDir = new File(getSyncWorkingDirectory(),"src");
			sourcesDir.mkdirs();
			
			for (File model: metaDir.listFiles(new FilenameFilter() {	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class.json");
				}
			})){
				try {
					sourcesGen.generateSourceFile(model, metaDir, sourcesDir, dataRepo.getDomainPackage());
					if (hbm)
						sourcesGen.generateHbmFile(model, metaDir, classesDir, dataRepo.getDomainPackage());
				} catch (IonException | IOException e) {
					needRestart = false;
					logger.Error("Не удалось создать исходный файл для доменного класса!", e);
					throw new IonException("Не удалось создать исходный файл для доменного класса!",e);
				} catch (ParserConfigurationException e) {
					needRestart = false;
					logger.Error("Не удалось создать hbm файл для доменного класса!", e);
					throw new IonException("Не удалось создать hbm файл для доменного класса!",e);
				}
			}
						
			try {
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				if (compiler == null)
					throw new IonException("Не удалось инициализировать компилятор Java!");
				DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
				StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(classesDir));
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
		                logger.Info("Code: " + diagnostic.getCode());
		                logger.Info("Kind: " + diagnostic.getKind());
		                logger.Info("Position: " + diagnostic.getPosition());
		                logger.Info("Start Position: " + diagnostic.getStartPosition());
		                logger.Info("End Position: " + diagnostic.getEndPosition());
		                logger.Info("Source: " + diagnostic.getSource());
		                logger.Info("Message: " + diagnostic.getMessage(Locale.getDefault()));
		            }
			    }				
			} catch (IOException e) {
				logger.Error("Не удалось скомпилировать JPA-сущности!", e);
				throw new IonException(e);
			} finally {
				FileUtils.delete(sourcesDir);
			}
		}		
		*/
		
		if (needMetaReload){
			try {
				metaRepo.setMetaDirectory(getDomainModelDirectory());
			} catch (IonException e1) {
				logger.Error("Не удалось перегрузить модель данных!", e1);
			}
		}			
	}

	@Override
	public File getSyncWorkingDirectory() {
		return workDir;
	}

	@Override
	public File getDownloadDirectory() {
		return tmpDir;
	}

	private void begin() {
		if (!ManagedSessionContext.hasBind(sessionFactory)){
			Session sess = sessionFactory.openSession();
			sess.setFlushMode(FlushMode.MANUAL);
			ManagedSessionContext.bind(sess);
		}
		sessionFactory.getCurrentSession().beginTransaction();
	}

	private void commit() {
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
		ManagedSessionContext.unbind(sessionFactory);
	}

	private void rollback() {
		sessionFactory.getCurrentSession().getTransaction().rollback();
		sessionFactory.getCurrentSession().close();
		ManagedSessionContext.unbind(sessionFactory);
	}
	
	private String sendRequest(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);
		
		connection.setRequestMethod("GET");			
		connection.setDoInput(true);
		connection.setDoOutput(false);
		connection.setUseCaches(false);  
		connection.setRequestProperty("User-Agent", "Mozilla/5.0"); 
		connection.setRequestProperty("Connection", "Keep-Alive"); 
			
		
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}
		br.close();
		connection.disconnect();
		return response.toString();
	}	

	@Override
	public void onSyncSessionStart() {
		needMetaReload = false;
		needProfileReload = false;
	}

	@Override
	public void onSyncSessionFinish() {
		if (needMetaReload){
			try {
				logger.Info("Отправлен запрос на перезагрузку модели.");
				logger.Info(sendRequest(reloadMetaUrl));
			} catch (IOException e) {
				logger.Error("Не удалось отправить запрос на перезагрузку модели!", e);
			}			
		}	
		
		if (needProfileReload){
			try {
				logger.Info("Отправлен запрос на перезагрузку профилей.");
				logger.Info(sendRequest(reloadProfilesUrl));
			} catch (IOException e) {
				logger.Error("Не удалось отправить запрос на перезагрузку модели!", e);
			}			
		}				
	}
	
	public Map<String,String> getCredentials(){
		try {
			String result = sendRequest(credentialsUrl);
			return new Gson().fromJson(result,new TypeToken<Map<String, String>>(){}.getType());
		} catch (JsonSyntaxException | IOException e) {
			logger.Error("Не удалось получить текущие учетные записи!", e);
		}
		return new HashMap<String, String>();
	}

	@Override
	public boolean needSyncStop() {
		return false;//needRestart;
	}
	/*
	public void reloadOrm() throws MalformedURLException, ClassNotFoundException, IonException{
		try {
			if (ManagedSessionContext.hasBind(sessionFactory)){
				sessionFactory.close();
				ManagedSessionContext.unbind(sessionFactory);
			}
			loadOrm();
			//dataRepo.setSessionFactory(sessionFactory);
			changeLog.setSessionFactory(sessionFactory);
			signDao.setSessionFactory(sessionFactory);
			userdao.setSessionFactory(sessionFactory);
			acl.setSessionFactory(sessionFactory);
		} catch (MalformedURLException | IonException | ClassNotFoundException e1) {
			logger.Error("Не удалось перегрузить контекст ORM!", e1);
		}
	}
	*/
	@SuppressWarnings("serial")
	@Override
	public void updateProfile(UserProfile u) {
		begin();
		User u2 = null;
		
		Gson gs = new GsonBuilder().serializeNulls().create();
		
		try {
			u2 = userdao.getUser(u.login);
			if (u2 != null){
  			for (Map.Entry<String, Object> p : u.properties.entrySet()){
  				userdao.setUserProperty(u2, p.getKey(), gs.toJson(p.getValue()));
  			}
  			userdao.save(u2);
			}
			needProfileReload = true;
			commit();
		} catch (Exception e){
			rollback();
			logger.Error("Ошибка при обновлении профиля пользователя " + u.login, e);
		}
		
		if (u2 != null){
			begin();
			try {
				List<String> grantedClasses = acl.GetGranted(new UserContext(u2.getUsername(), u2.getUsername()), new DACPermission[]{DACPermission.READ}, "c:::");
				
				List<DACPermission> denyAll = new LinkedList<DACPermission>(){{
					add(DACPermission.READ);
					add(DACPermission.WRITE);
					add(DACPermission.USE);
					add(DACPermission.DELETE);
				}};
				
				for (Map.Entry<String, Integer> p: u.access.entrySet()){
					final String cn = p.getKey();
					List<DACPermission> grant = new LinkedList<DACPermission>();
					List<DACPermission> deny = new LinkedList<DACPermission>();
					deny.addAll(denyAll);			
					
					for (ClassPermission cp: ClassPermission.values())
						if ((cp.getValue() & p.getValue().intValue()) == cp.getValue())
							switch (cp){
								case CREATE:{
									grant.add(DACPermission.USE);
									grant.add(DACPermission.READ);
									deny.remove(DACPermission.USE);
									deny.remove(DACPermission.READ);
								}break;
								case READ:{
									grant.add(DACPermission.READ);
									deny.remove(DACPermission.READ);
								}break;
								case UPDATE:{
									grant.add(DACPermission.WRITE);
									grant.add(DACPermission.READ);
									deny.remove(DACPermission.WRITE);
									deny.remove(DACPermission.READ);
								}break;
								case DELETE:{
									grant.add(DACPermission.DELETE);
									grant.add(DACPermission.READ);
									deny.remove(DACPermission.DELETE);
									deny.remove(DACPermission.READ);
								}break;
							}
					if (grant.size() > 0){
						// System.out.println("Granting permissions to class " + cn + " for " + u2.getUsername());
						aclDao.Grant("c:::"+cn, u2.getUsername(), grant.toArray(new DACPermission[grant.size()]));
					}
					
					if (deny.size() > 0){
						// System.out.println("Denying permissions to class " + cn + " for " + u2.getUsername());
						aclDao.Deny("c:::"+cn, u2.getUsername(), deny.toArray(new DACPermission[deny.size()]));
					}
				}
				
				for (String grantedclass: grantedClasses)
					if (!u.access.containsKey(grantedclass)){
						// System.out.println("Denying access to class " + grantedclass + " for " + u2.getUsername());
						aclDao.Deny("c:::"+grantedclass, u2.getUsername(), denyAll.toArray(new DACPermission[denyAll.size()]));
					}
				
				needProfileReload = true;
				commit();
			} catch (Exception e){
				rollback();
				logger.Error("Ошибка при назначении прав пользователя " + u.login, e);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void acceptAttrs(IStructMeta sm, DataUnit unit, Map<String, Object> data, String prefix) throws IonException {
		IStructMeta meta = sm;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		while (meta != null){
  		for (Entry<String,IPropertyMeta> entry : meta.PropertyMetas().entrySet()){
  			Object attr = data.get(entry.getKey());
  			if(attr != null){
  				if(entry.getValue().Type() == MetaPropertyType.FILE) {
  					if (attr instanceof Map){
    					Map<String,Object> am = (Map<String, Object>)attr;
    					if (am.containsKey("fileContents")){
    						String fnm = am.containsKey("fileName")?am.get("fileName").toString():(unit.id+"-"+prefix + entry.getKey());
    						String fct = am.containsKey("fileType")?am.get("fileType").toString():"";
    					
    						String fileId = fileStorage.Accept(new ByteArrayInputStream(
                                    Base64.decodeBase64(am.get("fileContents").toString())), 
    					                   fnm, fct);
    						unit.data.put(prefix + entry.getKey(), fileId);
    					}
  					} else
    						unit.data.remove(prefix + entry.getKey());	
  				} else if (entry.getValue().Type() == MetaPropertyType.STRUCT) {
  					IStructPropertyMeta spm = (IStructPropertyMeta)entry.getValue();
  					if (attr != null && (attr instanceof Map)){
  						acceptAttrs(spm.StructClass(), unit, (Map<String, Object>)attr, prefix + entry.getKey() + "$");
  						data.remove(entry.getKey());
  					}
  				} else if (entry.getValue().Type() == MetaPropertyType.REFERENCE) {
  					if (attr != null){
  						IClassMeta refMeta = ((IReferencePropertyMeta)entry.getValue()).ReferencedClass();
  						if (attr instanceof Map){
  							if (((Map)attr).containsKey(refMeta.KeyProperties()[0]))
  									unit.data.put(prefix + entry.getKey(), ((Map) attr).get(refMeta.KeyProperties()[0]));
  							else if (((Map)attr).containsKey("ouid")) {
  								IItem dummy = dataRepo.GetItem(refMeta.getName());
  								for (Map.Entry<String, Object> pv: ((Map<String, Object>)attr).entrySet()){
  									if (refMeta.PropertyMeta(pv.getKey()) != null)
  										dummy.Set(pv.getKey(), pv.getValue());
  								}
  								Collection<IItem> matched = dataRepo.GetList(dummy);
  								for (IItem mi: matched){
  									unit.data.put(prefix + entry.getKey(), mi.getItemId());
  									break;
  								}
  							}
  						} else {
  							if (StringUtils.isNumeric(attr.toString()) && refMeta.KeyProperties()[0].equals("guid")) {
  								IItem dummy = dataRepo.GetItem(refMeta.getName());
  								dummy.Set("ouid", attr);
  								Collection<IItem> matched = dataRepo.GetList(dummy);
  								for (IItem mi: matched){
  									unit.data.put(prefix + entry.getKey(), mi.getItemId());
  									break;
  								}  								
  							} else
  								unit.data.put(prefix + entry.getKey(), attr);
  						}
  					}
  				} else if (entry.getValue().Type() == MetaPropertyType.DATETIME){
  					Object v = null;
  					if ((attr instanceof Map) && ((Map)attr).containsKey("timestamp")){
  						v = new Date(Long.parseLong(((Map)attr).get("timestamp").toString()));
  					} else if (attr instanceof String) {
  						try {
								v = format.parse(attr.toString());
							} catch (ParseException e) {
								v = null;
							}
  					}
  					if (v != null)
  						unit.data.put(prefix + entry.getKey(), v);
  				} else
  					unit.data.put(prefix + entry.getKey(), attr);
  			}
  		}
  		meta = meta.getAncestor();
		}
	}
	
	private void acceptDataUnitNoT(DataUnit unit) throws IonException {
		logger.Info("Записываем объект "+unit.className+"@"+unit.id);
		
		IStructMeta unitMeta = metaRepo.Get(unit.className);
		
		acceptAttrs(unitMeta, unit, unit.data, "");
		
		IItem item = dataRepo.EditItem(unit.className, unit.id, unit.data);
		if (item == null){
			item = dataRepo.CreateItem(unit.className, unit.data);
			if (item.getMetaClass().checkAncestor("SmevEntity") != null){
				NotReadStatus st = new NotReadStatus();
				st.setGuid(item.getItemId());
				sessionFactory.getCurrentSession().save(st);
				sessionFactory.getCurrentSession().flush();
			}
		}
		
		for (IProperty p: item.getProperties().values()){
			if (p.Meta().Type() == MetaPropertyType.COLLECTION && unit.data.containsKey(p.getName())){
				Object v = unit.data.get(p.getName());
				
				if (v instanceof Iterable<?>){
					Iterator<IItem> curCol = dataRepo.GetAssociationsIterator(item, p.getName());
					
					IClassMeta ccm = (IClassMeta)((ICollectionPropertyMeta)p.Meta()).ItemsClass();
					
					Map<String, IItem> curColItems = new HashMap<String, IItem>();
					while (curCol.hasNext()){
						IItem i = curCol.next();
						curColItems.put(i.getItemId(), i);
					}

					Set<String> ids = new HashSet<String>();
					for (Object obj: (Iterable<?>)v){
						if (obj instanceof Map<?,?>){
							@SuppressWarnings("unchecked")
							Map<String, Object> du = (Map<String, Object>)obj;
							
							String cn = du.containsKey("__systemClass")?du.get("__systemClass").toString():ccm.getName();
							
							IItem el = null;
							
							if (ccm.KeyProperties()[0].equals("offlnSvId")){
								for (IItem test: curColItems.values()){
									if (test.Get("offlnSvVal").toString().equals(du.get("offlnSvVal").toString()))
										el = test;
										break;
								}
							} else if (du.containsKey(ccm.KeyProperties()[0])){
								el = dataRepo.GetItem(cn, du.get(ccm.KeyProperties()[0]).toString());
							} else {
								IItem dummy = dataRepo.GetItem(cn);
								for (Map.Entry<String, Object> pv: du.entrySet()){
									if (ccm.PropertyMeta(pv.getKey()) != null)
										dummy.Set(pv.getKey(), pv.getValue());
								}
								Collection<IItem> matched = dataRepo.GetList(dummy);
								for (IItem mi: matched){
									el = mi;
									break;
								}
							}

							if (el == null){
								acceptDataUnitNoT(new DataUnit(du.containsKey(ccm.KeyProperties()[0])?du.get(ccm.KeyProperties()[0]).toString():null, cn, du));
								el = dataRepo.GetItem(cn, du.get(ccm.KeyProperties()[0]).toString());
							}		
							
							if (el != null){
								ids.add(el.getItemId());
								if (!curColItems.containsKey(el.getItemId()))
									dataRepo.Put(item, p.getName(), el);
							}
						}
					}
					
					for (IItem i: curColItems.values()){
						if (!ids.contains(i.getItemId())){
							if (ccm.KeyProperties()[0].equals("offlnSvId"))
								dataRepo.DeleteItem(i.getClassName(), i.getItemId());
							else
								dataRepo.Eject(item, p.getName(), i);
						}
					}
				}
			}
		}	
		
		logger.Info("Объект " + unit.className + "@" + unit.id + " сохранен!");
	}

	@Override
	public void acceptDataUnit(DataUnit unit) {
		begin();
		try {
			acceptDataUnitNoT(unit);
			commit();
			logger.Info("Изменения записаны в хранилище!");
		} catch (Exception e) {
			rollback();
			logger.Error("Не удалось записать изменения в хранилище!", e);
		}
	}

	@Override
	public void setSyncPrevDate(Date prevDate) {
		String parameters = "prevDate="+prevDate.getTime();
		try {
			sendPost(offlineSyncInfoUrl,parameters);
		} catch (Exception e) {
			logger.Error("Ошибка информирования клиента!", e);
		}
	}

	@Override
	public void setOutgoingPackagesQuantity(Integer quantity) {
		String parameters = "number="+quantity.toString();
		try {
			sendPost(offlineSyncInfoUrl,parameters);
		} catch (Exception e) {
			logger.Error("Ошибка информирования клиента!", e);
		}
	}
	
	private String sendPost(URL url,String urlParameters) throws Exception {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Content-type", "application/x-www-form-urlencoded"); 

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
//		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	@Override
	public void onStop() {
		sessionFactory.close();
		ManagedSessionContext.unbind(sessionFactory);
	}
	
	private Object parseFileAttribute(IStructMeta sm, String prop, Object value) throws IonException {
		if(prop != null && !prop.trim().isEmpty()){
			if(prop.contains(".")){
				IPropertyMeta pm = sm.PropertyMeta(prop.substring(0, prop.indexOf(".")));
				if(pm != null && pm.Type() == MetaPropertyType.STRUCT){
					return parseFileAttribute(((StructPropertyMeta)pm).StructClass(), prop.substring(prop.indexOf(".")+1), value);
				}
			} else {
				IPropertyMeta pm = sm.PropertyMeta(prop);
				if((pm != null) && (pm.Type() == MetaPropertyType.FILE) && (value != null)){
					Map<String, String> fo = new HashMap<String, String>();
					File f = fileStorage.getFile((String)value);
					fo.put("fileName", f.getName());
					fo.put("fileSize", String.valueOf(f.length()));
					try {
						
						String mime = Files.probeContentType(f.toPath());
						fo.put("fileType", mime);
						FileInputStream is = new FileInputStream(f);
						fo.put("fileContents", FileUtils.toBase64String(is));
						is.close();
					} catch(IOException e) {
						throw new IonException(e);
					}
					return fo;
				}
			}
		}
		return value;
	}

	@Override
	public Iterator<DataChange> getChanges(Date since) throws IonException {
		List<DataChange> tmp = new LinkedList<DataChange>();		
		begin();
		try {
			Iterator<IChangelogRecord> ri = changeLog.getChanges(since);
  		while (ri.hasNext()){
  			IChangelogRecord r = ri.next();
  			IStructMeta cm = metaRepo.Get(r.getObjectClass());
  			Map<String, Object> attrUpdates = r.getAttributeUpdates();
  			Map<String, Object> updates = new HashMap<String, Object>();
  			for(Entry<String, Object> au : attrUpdates.entrySet()){
  				updates.put(au.getKey(), parseFileAttribute(cm, au.getKey(), au.getValue()));
  			}
  			tmp.add(new DataChange(r.getActor(), DataChangeType.fromString(r.getType().getValue()).getValue(),r.getObjectId(), r.getObjectClass(), updates));
  		}
  		commit();
		} catch (Exception e) {
			rollback();
			throw new IonException(e);
		}		
		return tmp.iterator();
	}
	
	@SuppressWarnings("serial")
	@Override
	public Iterator<DataUnit> getAuxData(Date since) throws IonException {
		List<DataUnit> tmp = new LinkedList<DataUnit>();
		begin();
		try {
			List<DigitalSignature> ri = signDao.getSigns(since);
			for (final DigitalSignature ds: ri){
				tmp.add(new DataChange(ds.getActor(), "sign", 
				  ds.getClassName()+"-"+ds.getObjId()+"-"+ds.getTs().getTime(),
				  "DigitalSignature", 
				  new HashMap<String, Object>(){{
				  	put("class", ds.getClassName());
						put("id", ds.getObjId());
						put("action", ds.getAction());
						put("attributes",new Gson().fromJson(ds.getAttributes(),new TypeToken<Map<String, String>>(){}.getType()));
						put("signature", new String(ds.getSign()));
						put("data", new String(ds.getData()));
				  }}));
			}
			commit();
		} catch (Exception e) {
			rollback();
			throw new IonException(e);
		}
		return tmp.iterator();
	}
}
