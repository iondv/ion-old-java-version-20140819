Var syncPrivateKey
Var syncClientId
Var adapterUrl

!macro ReadDaemonIni File_ini
  ReadINIStr $syncPrivateKey ${File_ini} "daemon" "sync.privateKey"
  ReadINIStr $syncClientId ${File_ini} "daemon" "sync.clientId"
  ReadINIStr $adapterUrl ${File_ini} "daemon" "sync.adapterUrl"
!macroend 

!macro Daemon_properties File_prop
  ClearErrors  
  Push $0
  FileOpen $0 ${File_prop} w 
  IfErrors done 
  

FileWrite $0 "$\r$\niteration.interval=60"
!ifdef MySQL
  FileWrite $0 "$\r$\nhibernate.connection.driver_class=com.mysql.jdbc.Driver"
  FileWrite $0 "$\r$\nhibernate.connection.url=jdbc:mysql://$SUBDHost:$SUBDPort/$DBName?useUnicode=true&characterEncoding=UTF-8"
  FileWrite $0 "$\r$\nhibernate.dialect=org.hibernate.dialect.MySQLDialect"
!endif
!ifdef PostgreSQL
  FileWrite $0 "$\r$\nhibernate.connection.driver_class=org.postgresql.Driver"
  FileWrite $0 "$\r$\nhibernate.connection.url=jdbc:postgresql://$SUBDHost:$SUBDPort/$DBName?useUnicode=true&characterEncoding=UTF-8"
  FileWrite $0 "$\r$\nhibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
!endif
!ifdef MSSQL
  FileWrite $0 "$\r$\nhibernate.connection.driver_class=com.microsoft.sqlserver.jdbc.SQLServerDriver"
  FileWrite $0 "$\r$\nhibernate.connection.url=jdbc:sqlserver://$SUBDHost:$SUBDPort/$DBName?useUnicode=true&characterEncoding=UTF-8"
  FileWrite $0 "$\r$\nhibernate.dialect=org.hibernate.dialect.SQLServer2008Dialect"
!endif

FileWrite $0 "$\r$\nhibernate.connection.username=$SUBDUser"
FileWrite $0 "$\r$\nhibernate.connection.password=$SUBDPsw"

FileWrite $0 "$\r$\nhibernate.show_sql=false"
FileWrite $0 "$\r$\nhibernate.charSet=UTF-8"
FileWrite $0 "$\r$\nhibernate.zeroDateTimeBehavior=convertToNull"
FileWrite $0 "$\r$\nhibernate.current_session_context_class=managed"

  ${StrRep} $R0 $IONAppPATH '\' '\\'
FileWrite $0 "$\r$\nmodel.package=$IONModelSmevPckg"
FileWrite $0 "$\r$\nmodel.paths.meta=$R0\\meta"
FileWrite $0 "$\r$\nmodel.paths.navigation=$R0\\navigation"
FileWrite $0 "$\r$\nmodel.paths.views=$R0\\views"
FileWrite $0 "$\r$\nmodel.paths.classes=$R0\\WEB-INF\\classes"

FileWrite $0 "$\r$\nsync.adapterUrl=$adapterUrl"
FileWrite $0 "$\r$\nsync.privateKey=$syncPrivateKey"
FileWrite $0 "$\r$\nsync.clientId=$syncClientId"

FileWrite $0 "$\r$\nsync.timeouts.volumeUpload=180"
FileWrite $0 "$\r$\nsync.timeouts.volumeDownload=180"
FileWrite $0 "$\r$\nsync.timeouts.download=360"
FileWrite $0 "$\r$\nsync.timeouts.upload=360"
FileWrite $0 "$\r$\nsync.timeouts.downloadComplete=30"

FileWrite $0 "$\r$\nstorage.useDiscriminator=true"
FileWrite $0 "$\r$\nmapping.useLazyLoading=true"

##2CHECK относительные пути для рестарта, информации - могут меняться?
/*FileWrite $0 "$\r$\nion.web.urls.restart=http://$IONOflSrvHost:$IONOflSrvPort/$IONOflSrvName/ion/restart"
FileWrite $0 "$\r$\nion.web.urls.credentials=http://$IONOflSrvHost:$IONOflSrvPort/$IONOflSrvName/ion/credentials"
FileWrite $0 "$\r$\nion.web.urls.offlineSyncInfo=http://$IONOflSrvHost:$IONOflSrvPort/$IONOflSrvName/ion/syncinfo/set"*/

FileWrite $0 "$\r$\nion.web.urls.restart=http://$TomcatHost:$TomcatPort/$IONAppName/ion/restart"
FileWrite $0 "$\r$\nion.web.urls.credentials=http://$TomcatHost:$TomcatPort/$IONAppName/ion/credentials"
FileWrite $0 "$\r$\nion.web.urls.offlineSyncInfo=http://$TomcatHost:$TomcatPort/$IONAppName/ion/syncinfo/set"

done:   
   FileClose $0 
   Pop $0
!macroend 