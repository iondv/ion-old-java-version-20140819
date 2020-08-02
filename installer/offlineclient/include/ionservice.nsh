!define IONServStartBAT		"start.bat"
!define IONServStopBAT		"stop.bat"	  
!define IONServInstBAT		"installservice.bat"	
!define IONServRemoveBAT	"removeservice.bat"	


!macro IONSeviceStartSave 
  ClearErrors  
  Push $0
  FileOpen $0 '$INSTDIR\${IONServStartBAT}' w
  IfErrors donesss

  FileWrite $0 "@echo off$\r$\n"
  FileWrite $0 "echo ����᪠�� �ࢨ�� ION: ����, ������, �ࢨ� ᨭ�஭���樨$\r$\n"
  FileWrite $0 'NET START $TomcatServiceName$\r$\n'
  FileWrite $0 'NET start $SUBDServiceName$\r$\n'
  FileWrite $0 'NET START ${IONOfflineSyncName}$\r$\n'
  FileWrite $0 'pause'
  donesss:   
   FileClose $0 
   Pop $0  
!macroend

!macro IONSeviceStopSave 
  ClearErrors  
  Push $0
  FileOpen $0 '$INSTDIR\${IONServStopBAT}' w
  IfErrors donessts

  FileWrite $0 "@echo off$\r$\n"
  FileWrite $0 "echo ��⠭�������� �ࢨ�� ION: ����, ������, �ࢨ� ᨭ�஭���樨$\r$\n"
   FileWrite $0 'NET STOP ${IONOfflineSyncName}$\r$\n'
   FileWrite $0 'NET STOP "$TomcatServiceName"$\r$\n'
   FileWrite $0 'NET STOP "$SUBDServiceName"$\r$\n'
   FileWrite $0 'pause'
  donessts:   
   FileClose $0 
   Pop $0  
!macroend


!macro IONSeviceInstSave 
  ClearErrors  
  Push $0
  FileOpen $0 '$INSTDIR\${IONServInstBAT}' w
  IfErrors donesis
   FileWrite $0 "@echo off$\r$\n"
  FileWrite $0 "echo ��������㥬 � ��⥬� �ࢨ�� ION: ����, ������, �ࢨ� ᨭ�஭���樨$\r$\n" 
   FileWrite $0 'SET "JAVA_HOME=$JDKPATH"$\r$\n SET "CATALINA_HOME=$TomcatPATH"$\r$\n "$TomcatPATH\bin\service.bat" install $TomcatServiceName$\r$\n'
!ifdef PostgreSQL
    FileWrite $0 'sc create $SUBDServiceName binpath="$SUBDSQLPATH\bin\pg_ctl.exe runservice -N $SUBDServiceName -D $SUBDSQLPATH/data -w" DisplayName="ION PostgreSQL" start="auto" type= own obj=".\$SUBDUser" password="$SUBDPsw"$\r$\n'
!endif
!ifdef MSSQL
###TODO ᤥ���� ��⠭���� MS SQL
!endif
!ifdef MySQL
    FileWrite $0 '$SUBDSQLPATH\bin\mysqld.exe --install $SUBDServiceName --defaults-file="$SUBDSQLPATH\${MySQLINIDef}$\r$\n'
!endif
  FileWrite $0 '$IONOfServicePATH\${IONSyncInit} install ${IONOfflineSyncName} $IONOfServicePATH $JDKPATH$\r$\n'
  FileWrite $0 'pause'
  donesis:   
   FileClose $0 
   Pop $0  
!macroend

!macro IONSeviceRemoveSave 
  ClearErrors  
  Push $0
  FileOpen $0 '$INSTDIR\${IONServRemoveBAT}' w
  IfErrors donesrs
  
   FileWrite $0 "@echo off$\r$\n"
  FileWrite $0 "echo ����塞 �� ��⥬� �ࢨ�� ION: ����, ������, �ࢨ� ᨭ�஭���樨$\r$\n" 
FileWrite $0  '$IONOfServicePATH\${IONSyncInit} remove ${IONOfflineSyncName} $IONOfServicePATH$\r$\n'
FileWrite $0 'SET "JAVA_HOME=$JDKPATH"$\r$\n SET "CATALINA_HOME=$TomcatPATH"$\r$\n "$TomcatPATH\bin\service.bat" remove "$TomcatServiceName"$\r$\n'
!ifdef PostgreSQL	
	FileWrite $0 'sc delete "$SUBDServiceName"$\r$\n'
!endif
!ifdef MSSQL
##TODO  㤠����� MS SQL
!endif
!ifdef MySQL
   FileWrite $0  '$SUBDSQLPATH\bin\mysqld.exe --remove $SUBDServiceName$\r$\n'
!endif
  FileWrite $0 'pause'
  donesrs: 
   FileClose $0 
   Pop $0  
!macroend