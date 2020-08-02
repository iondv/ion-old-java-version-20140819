!macro IONShortCut File_ini
  WriteINIStr ${File_ini} "{000214A0-0000-0000-C000-000000000046}" "Prop3" "19,2"  ;по умолчанию
  WriteINIStr ${File_ini} "InternetShortcut" "URL" "http:\\$TomcatHost:$TomcatPort/$IONAppName"
  WriteINIStr ${File_ini} "InternetShortcut"  "IDList" "" ;по умолчанию
!macroend

!macro ION_Def

###TODO Ќужно заполн€ть только пустые, заполненные считываюс€ с файла настроек

    #«адаем значени€ путей по умолчанию. 
    StrCmp $IONOfServicePATH "" 0 +2
	  StrCpy $IONOfServicePATH "$INSTDIR\${IONOfflineSyncName}"
    StrCmp $JDKPATH "" 0 +2
	  StrCpy $JDKPATH "$INSTDIR\${JDKFolder}"
    StrCmp $TomcatPATH "" 0 +2	  
      StrCpy $TomcatPATH "$INSTDIR\${TomcatFolder}"
    StrCmp $SUBDSQLPATH "" 0 +2	
      StrCpy $SUBDSQLPATH "$INSTDIR\${SUBDSQLFolder}"
    StrCmp $IONAppName "" 0 +2	 ;не нужны , т.к. копируем по пути приложени€
      StrCpy $IONAppName "${IONAppFolder}" ;не нужны , т.к. копируем по пути приложени€
    StrCpy $IONAppPATH "$TomcatPATH\webapps\$IONAppName" ;об€зательно после установки пути к Tomcat, всегда в папке томкат - не считываем с настроек INI
	
	#«адаем значени€ настроек по умолчанию. 
    StrCmp $TomcatServiceName "" 0 +2	
      StrCpy $TomcatServiceName ${TomcatServiceNameDef}
    StrCmp $TomcatPort "" 0 +2	
      StrCpy $TomcatPort ${TomcatPortDef}
    StrCmp $TomcatHost "" 0 +2	
      StrCpy $TomcatHost ${TomcatHostDef}

    StrCmp $IONOflSrvHost "" 0 +2	
	  StrCpy $IONOflSrvHost ${IONOflSrvHostDef}
    StrCmp $IONOflSrvPort "" 0 +2	
      StrCpy $IONOflSrvPort ${IONOflSrvPortDef}
    StrCmp $IONOflSrvName "" 0 +2	
	  StrCpy $IONOflSrvName ${IONOflSrvNameDef}
    StrCmp $IONModelSmevPckg "" 0 +2	
      StrCpy $IONModelSmevPckg ${IONModelSmevPckgDef}
	
    StrCmp $DBName "" 0 +2	
      StrCpy $DBName ${DBNameDef}
    StrCmp $SUBDPort "" 0 +2	
	  StrCpy $SUBDPort ${SUBDPortDef}
    StrCmp $SUBDHost "" 0 +2	
      StrCpy $SUBDHost ${SUBDHostDef}
    StrCmp $SUBDUser "" 0 +2		
	  StrCpy $SUBDUser ${SUBDUserDef}
    StrCmp $SUBDPsw "" 0 +2	
	  StrCpy $SUBDPsw ${SUBDPswDef}
    StrCmp $SUBDServiceName "" 0 +2	
      StrCpy $SUBDServiceName ${SUBDServiceNameDef}

	#файлы настроек	
	StrCpy $IONSyncINI "$EXEDIR\${IONSyncINIDef}" ;по умолчанию настройки клиента к офлайн серверу наход€тс€ в папке инстал€ции
!macroend

!macro IONReadINI File_ini
  ReadINIStr $IONAppName ${File_ini} "IONapps" "IONAppName"

  ReadINIStr $IONOfServicePATH ${File_ini} "path" "IONOfService" 
  ReadINIStr $JDKPATH ${File_ini} "path" "JDK" 
  ReadINIStr $TomcatPATH ${File_ini} "path" "Tomcat" 
  ReadINIStr $SUBDSQLPATH ${File_ini} "path" "SUBDSQL"  

  ReadINIStr $TomcatServiceName ${File_ini} "Tomcat" "ServiceName" 
  ReadINIStr $TomcatPort  ${File_ini} "Tomcat" "Port" 
  ReadINIStr $TomcatHost ${File_ini} "Tomcat" "Host" 
  
  ReadINIStr $IONOflSrvHost ${File_ini} "offlinesrv" "Host" 
  ReadINIStr $IONOflSrvPort  ${File_ini} "offlinesrv" "Port" 
  ReadINIStr $IONOflSrvName ${File_ini} "offlinesrv" "Name" 
  ReadINIStr $IONModelSmevPckg ${File_ini} "offlinesrv" "IONModelSmevPckg" 
 
  ReadINIStr $SUBDHost ${File_ini} "SUBD" "host" 
  ReadINIStr $SUBDPort ${File_ini} "SUBD" "port" 
  ReadINIStr $DBName ${File_ini} "SUBD" "DBName"
  ReadINIStr $SUBDUser ${File_ini} "SUBD" "User"
  ReadINIStr $SUBDPsw ${File_ini} "SUBD" "Psw"
  ReadINIStr $SUBDServiceName ${File_ini} "SUBD" "ServiceName"

 
!macroend 

!macro IONSaveINI File_ini
  WriteINIStr ${File_ini} "IONapps" "IONAppName" "$IONAppName"
 
  WriteINIStr ${File_ini} "path" "IONOfService" "$IONOfServicePATH"
  WriteINIStr ${File_ini} "path" "JDK" "$JDKPATH"
  WriteINIStr ${File_ini} "path" "Tomcat" "$TomcatPATH"
  WriteINIStr ${File_ini} "path" "SUBDSQL" "$SUBDSQLPATH"  

  WriteINIStr ${File_ini} "Tomcat" "ServiceName" "$TomcatServiceName"
  WriteINIStr ${File_ini} "Tomcat" "Port" "$TomcatPort"  
  WriteINIStr ${File_ini} "Tomcat" "Host" "$TomcatHost" 
  
  WriteINIStr ${File_ini} "offlinesrv" "Host" "$IONOflSrvHost"
  WriteINIStr ${File_ini} "offlinesrv" "Port" "$IONOflSrvPort"  
  WriteINIStr ${File_ini} "offlinesrv" "Name" "$IONOflSrvName" 
  WriteINIStr ${File_ini} "offlinesrv" "IONModelSmevPckg" "$IONModelSmevPckg" 
 
  WriteINIStr ${File_ini} "SUBD" "host" "$SUBDHost"
  WriteINIStr ${File_ini} "SUBD" "port" "$SUBDPort"
  WriteINIStr ${File_ini} "SUBD" "DBName" "$DBName"
  WriteINIStr ${File_ini} "SUBD" "User" "$SUBDUser"
  WriteINIStr ${File_ini} "SUBD" "Psw" "$SUBDPsw"
  WriteINIStr ${File_ini} "SUBD" "ServiceName" "$SUBDServiceName"  

!macroend 