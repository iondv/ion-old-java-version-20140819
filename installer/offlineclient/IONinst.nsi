;NSIS Modern User Interface
;Basic Example Script
;Written by Joost Verburg

#���� ������������ ������ ����������� ����� ��������� - �� ���������� ��������� � ��� ����� ��� ��-��������� � ������������ ��������������.

/*�������� ��������� � ����:
+ ������ uninstall
+ �������� ��������� ion.ini - ��.����
+ jdk �����������
+ ������ ��������� ����������, ����� serve.xml, tomcat-user.xml, service.bat �������������, ������ �����������������, �������
+ ������ ������ ��������������� ��� ������ "����"
+MySQL ����������� ���������, my.ini ������������, ��������� ���������, ������ ������� � ����������� my.ini, ������������� root ������ ���������
+	���� offlinedb ������������
+ ��������� ������������� � ������
+ ������ ������������� � ������, ��������� � db.properties � model.properties - ��������
+ ������ ������������� ������������, �������� daemon.properties �������, ������ ��������������. 
+ ������ ������������� ����������, ��� ������� "����" ���� ���������� - ��� ������������
+������ ���� ���������
+ ���������� � ������ - ��������


*/


#1. ������� ������� ���������:
#   * �������� ������ �� ��������� ������ � �������� �������� ��� ������� ����.
#   * �������������� ������ ������ �������������
#   * ���� ������������� MySQL ����������
#   * ���������, ��� �������� ������������� �������, ��� �������� JAVA_PATH � ������� � PATH � ����� JDK\bin. ���� �� �������� �������� ��� ���������� ��� �������.
#2. ����� �������
#   * �������� ������������ ������ ���� � ���� ������� - �������� ������� ������ JDK.
#   * ��������� ��������� jdk, ��������� ���������� JAVA_PATH, ���� � PATH. ���� �����������, �� �� ������������� JDK. ���� ��� - ����������� Java_HOME, ������� java bin � ����
#   * ��������� ��������� tomcat. ���������, ��� ���������� ��� ������. ���� ����������, ������ ��������������� ����� � �����. ���� �� ���������� ����������� ��������� ���������, � �.�. CATALINA_HOME=%PATH%\Tomcat
#	* ��������� ��������� postgress ��� mysqll. ���� ���������� ���������� ������, �����. ���������� ����.

#3. ����� �������.
#   * ����� ���� ���� ������ �� �������������
#   * ��������� �������� ����.
#   * ����� � ����� ������� ���.������. ������������ �� � ������ � � ������.
#   * ����������� �������� ������ ������ �������

#3. Relice Candidat
#   * ����������� �������� ������ ������� (������������� ��������� ������ � ������ �������)
#   * ����������� ����������� ������������ �����
#   * ����������� ��������� ���������� ���������� ������ �������.
#   * ����������� ��������� ��������� ���������� � ����� ��������� ��� ������� (���� ������ ����� ������������� � �������� ���������� ������ ���. ������ - �����������, ������� ������, �������������)
;--------------------------------
;��� ������ ����� ������������� ����������. ��� ���������� � NSIS http://nsis.sourceforge.net/How_can_I_install_a_plugin . 
;--------------------------------
/*
UserMgr   http://nsis.sourceforge.net/UserMgr_plug-in
*/

;--------------------------------
;���������� ������� NSIS
;--------------------------------
  !include "StrFunc.nsh"  ;������ �� ��������, � ��������� ������ ����� �� ������� ����
  ${StrRep} ;����������� ������������ ������� �������� ������ �� �������


#��������� �����������
  !define PACK ; ����� ����������������, ���� ���������� ����������� ���  ������� � ������ ������� - ���� ���������. ��� �������� �� ����� ����������� � ���������� ������������ ����� � ��� ��������� �� �� ����� ������������� (��������������, ��� ��� ��� ���� � ����� ����������). ����� ��� �������� ����� ���������, ��� �������� �������
  !define IONSYNC  ;�������� � ����� ������ �������������

#��� ����
;  !define PostgreSQL ###TODO ����� ������� ��� ���� � ����� ������.
  !define MySQL
;  !define MSSQL


#��������� �������� �� ��������� � ��������
  !include 				".\include\IONInstInit.nsh" ;������� �������� �� ���������, ���������� �������� � INI, ������ �������������� � INI
  !include 				".\include\db_properties.nsh" ;������ �������� ���� ������ ��� ���������, ������ ������ ������� MySQL - � ������������� ���������� ����������� �� ��������� ��� ��������� ������������� 
  !include 				".\include\model_properties.nsh" ;������ �������� ���� ������ ��� ���������, ������ ������ ������� MySQL - � ������������� ���������� ����������� �� ��������� ��� ��������� ������������� 
!ifdef IONSYNC 
 !include 				".\include\daemon_properties.nsh" ;������ �������� ������� �������������,� ������������� ���������� ����������� � �������� �� INI
!endif
  !include				".\include\ionservice.nsh" ;���� ������ �������� ��� ������ � ���������
  
  !define InclFolder 		"include"  #����� � ����������������� ������� ��� ��������� � ������ ������ ������������.
  !define IONDbProperties	"db.properties"			;��������� ����������� � ���� ��� ���������: tomcat\webapps\ion-web-platform\WEB-INF\db.properties
  !define IONModelProperties "model.properties"   ;��������� ������ ION



#����� � ����������. ���� ����� ������������� ���������, ����� ��������
  !define PlatfFolderSrc 		"ion"
    !define IONINI 	"ion.ini" ;���� � ����������� ����������
	!define IONURL 	"ion.url" ;���� � URL ��� �������� � ���������
	!define IONPlatformFolder "ion-web-platform"		;����� � ���������� ���������
	Var IONAppName
	Var IONAppPATH
      !define IONAppFolder "ion-offline"		;����� ��� webapps tomcat
	Var IONModelSmevPckg
	  !define IONModelSmevPckgDef "ion.domain.smev"


	
!ifdef IONSYNC 
   Var IONOfServicePATH 
      !define IONOfflineSyncName "ion-offline-sync"	;������ ������ ������������� ������ �������
	Var IONSyncINI
      !define IONSyncINIDef 	"IONSync.ini" 	  ;���� �������� ������� �������������, ������ ������ � ����� ��������� ������ ������������
	  
	!define IONDaemonProperties	"daemon.properties"  ;��������� �� ��������� ��� ������� ������������� ������ �������
	!define IONSyncInit	"ionservice.bat" 		     ;��������� ���� ������������� �������
	
    Var IONOflSrvHost 		;Host ������ �������
	  !define IONOflSrvHostDef "172.18.225.55"	
	Var IONOflSrvPort 		;���� ������ �������
	  !define 	IONOflSrvPortDef "80"
	Var IONOflSrvName 		;���� � ���������� tomcat ������ �������
	  !define	IONOflSrvNameDef "offline"
	
!endif
  
#����� � ������������. ����� ������������� �������������, ����� ��������
  !define DistrFolder 		"distr"
	  
#��������� JDK
	Var JDKPATH
      !define JDKFolder 		"jdk" 					;����� � JDK
	  
#��������� ������
	Var TomcatPATH 
      !define TomcatFolder 	"tomcat" 				;����� � ��������
      !define TomcatServerXml "server.xml"			;��������� ������� Tomcat: tomcat\conf\server.xml - ��������� � ����� InclFolder 
	  !define TomcatServiceBat "service.bat"			;���� ����������� ������� Tomcat - ��������� � ����� InclFolder. � ��� �������� ��������� tomcat ��� �������, � ��������� ������ --JvmMx 512.
	  !define TomcatUserXml "tomcat-users.xml"			;������������ Tomcata - ����� ��������� ������������ ������
	Var TomcatServiceName ;�������� ������� �������
	  !define TomcatServiceNameDef "Tomcat"
	Var TomcatPort
	  !define TomcatPortDef "8080"
	Var TomcatHost  ;�� �����, �.�. ������ localhost
	  !define TomcatHostDef "localhost"

#��������� ����
	Var SUBDSQLPATH
	Var SUBDHost
	  !define SUBDHostDef "localhost"	  
	Var DBName
	  !define DBNameDef "offlinedb"
	Var SUBDPort
	Var SUBDUser
	Var SUBDServiceName 		#TODO �������� ����� �������� ������ �������� �� ���������?
!ifdef PostgreSQL
      !define SUBDSQLFolder 	"pgsql" 				;����� � PostrgeSQL
	  !define SUBDPortDef "5432" 
	  !define SUBDUserDef  "postgres" 
	  !define SUBDServiceNameDef  "PostrgeSQL" 
	  !define SQLInitDB "psinit.sql" 				;������ ������������� �� PostrgeSQL
!endif
!ifdef MSSQL
	   !define SUBDSQLFolder 	"mssql" 				;����� � MSSQL
       !define SUBDPortDef "1433" 
       !define SUBDUserDef  "sa" 
       !define SSUBDServiceNameDef  "MSSQL" 
	   !define SQLInitDB "msinit.sql" 				;������ ������������� �� MSSQL
   
!endif
!ifdef MySQL
  	   !define SUBDSQLFolder 	"mysql" 				;����� � MySQL
       !define SUBDPortDef "3306"
       !define SUBDUserDef  "root"
       !define SUBDServiceNameDef  "MySQL"
	   !define SQLInitDB "myinit.sql"				;������ ������������� �� MySQL
	   
       !define MySQLINIDef 	"my.ini"
!endif
	Var SUBDPsw
	  !define SUBDPswDef "ION-sql1"
 

#�������� ���������� � �������� ����
  Name "ION ������ ������"
  OutFile "IONinst.exe"
 
# ��������� ������������ 
  
  InstallDirRegKey HKCU "Software\ION" ""  ;Get installation folder from registry if available
  RequestExecutionLevel admin ; ������� ������� �������������
 
  ;���� �������� ����� ��� ��������� ���� ���������, ��� ���������� ������� � Java, ���� ��� ��������
  InstallDir "C:\ION"   ;"��� ������, �������� ������������������ � ������ � ��������� - - ��������� ����� ����������

  InstType "������" ; - ��� ���������
  InstType "���������� ���������" ; - ��� ��������� - ��� ������������� ������, ���� � JDK
#  SetCompressor /solid lzma ; - ������������ ����������� ������ Lzma
#  SetDatablockOptimize on ; - �������� ����������� ������ �����
#  CRCCheck off ; - �� ��������� ����������� ����� ������������
#  WindowIcon off ; - ��������� ������ � ���� ������������
#  XPStyle on ; - �������� ������������� ����� XP
#  SetOverwrite on ; - ����������� ���������� ������ ��������
#  AllowRootDirInstall false ; - �������� ����������� ��������� ��������� � ������
#  AutoCloseWindow false ; - ������ ������������ ������������ ����� ���������� ���� ��������
  
  
  ; ��������� ����������
#  BrandingText "ION ���������" ; - ����� ����� �����������
#  BGGradient 0x0000FF 0x000080 0x0080FF ; - ������������� ��� ��������� (�������/������/���� ������)
#  !define MUI_HEADERIMAGE ; - ����������� ��������� � ��������� �������
#  !define MUI_HEADERIMAGE_BITMAP "modern-header.bmp" ; - ��� ������� � ���������
#  !define MUI_HEADERIMAGE_RIGHT ; - ������� � ��������� ����� ���������� ������
#  !define MUI_ICON "Install.ico" ; - ������ ������������
#  !define MUI_UNICON "UnInstall.ico" ; - ������ ��������������
#  !define MUI_COMPONENTSPAGE_SMALLDESC ; - ����������� ����� ��� ����������
#  !define MUI_ABORTWARNING ; - ������������� �� ������ ���������
#  !define MUI_LICENSEPAGE_RADIOBUTTONS ; - ������������ ����������� �� �������� ��������
  

	Var MUI_TEMP ; - ��� ���������� ��� �������� ���� ��� ������� � ���� ����
    Var STARTMENU_FOLDER 	

;--------------------------------
;��������� ���������� Modern UI
;--------------------------------
  !include "MUI2.nsh"
  !define MUI_ABORTWARNING

;--------------------------------
;�������� ���������
;--------------------------------
  !insertmacro MUI_PAGE_WELCOME ; - �������� �����������
  !insertmacro MUI_PAGE_LICENSE ".\${InclFolder}\License.txt" ; - �������� � ���������
  !insertmacro MUI_PAGE_COMPONENTS ; - �������� �����������
  !insertmacro MUI_PAGE_DIRECTORY ; - �������� ������ ����� ���������
  !insertmacro MUI_PAGE_INSTFILES ; - �������� ���� ���������� ���������
  
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER ; - �������� � ������� ������ �������  

  !insertmacro MUI_UNPAGE_CONFIRM ; - �������� �������� ���� ���������������
  !insertmacro MUI_UNPAGE_INSTFILES ; - �������� ���� ���������� ��������
  
;--------------------------------
;�����
;-------------------------------- 
  !insertmacro MUI_LANGUAGE "Russian"

;--------------------------------
;���� "����" ��������� �������
;--------------------------------  
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\ION"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Label"


;--------------------------------
;���������� ���������
;--------------------------------

Section "���������� ��������� ION" IONSec #����� ������ � ����������� ��� ION
    SectionIn RO ; - ������ ������ ��� ������, �.�. �� ������ ���������

	CreateDirectory "$INSTDIR"
    !insertmacro IONReadINI "$EXEDIR\${IONINI}"
	!insertmacro ION_Def ;������ �������� ���������� �� ���������, ���� ��� �� ���� ������ � INI
####TODO ���� �������� ��� ��������������� tomcat, postgress
##    !insertmacro IONReadUser �������� ���������� �������� �� ������������ (����������� �����) - ������� ���� ���.

###TODO ������ ��� ��������  �� ���������� � ������??
###TODO ����� ���������, ��� ����� ������
###TODO ����� ���������, ��� ����� ��������
###TODO ����� ���������, ��� ������� ����� �� ����������������
	
  !insertmacro IONShortCut "$INSTDIR\${IONURL}" ;�������� url ����� � ������� �� ���������� � �������
###TODO  ;�������� ������� ��� ������� ������� � ������� ���������
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\$IONAppName.lnk" "$INSTDIR\${IONURL}"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\�������� $IONAppName.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END 

    WriteRegStr HKCU "Software\ION" "PATH" $INSTDIR ;��������� � ������ ���� ���������
    WriteUninstaller "$INSTDIR\Uninstall.exe"		;������� uninstaller
	

	!insertmacro IONSaveINI "$INSTDIR\${IONINI}" ;���������� ��������� ���������� � ini ����

	!insertmacro IONSeviceStartSave 
	!insertmacro IONSeviceStopSave
	!insertmacro IONSeviceInstSave
	!insertmacro IONSeviceRemoveSave 
	
SectionEnd

SectionGroup "����� ����������" BDGroup
Section "Java" JDKSec
  SectionIn 1 ; - �������������� ������ � ������ ���������
  DetailPrint "��������� JDK" 
  SetOutPath "$JDKPATH" ;����� ��� ���������� ���������
!ifdef PACK
  File /r ${DistrFolder}\${JDKFolder}\*.*
!endif

  # ��������� JDK - ���� �� ���������, �.�. ������ ����� ��������� ��� tomcat    
  ###TODO: ������� ��������� ������������ ����� JDK �� �� ������������ ������.
SectionEnd 

Section "Tomcat" TomcatSec
  SectionIn 1 ; - �������������� ������ � ������ ���������
  DetailPrint "��������� Tomcat"
  SetOutPath "$TomcatPATH" ;����� ��� ���������� ���������
!ifdef PACK
  File /r "${DistrFolder}\${TomcatFolder}\*.*"
!endif

#��������� Tomcat - ####TODO ���������� ���� �� � ��� ����� ���������� �������� ���������, �� ���� XML � bat ������������ � NSIS ������ � ��� ������ ��������� � ������� ��������� ��������� �����������
    SetOutPath "$TomcatPATH\bin"
	File "${InclFolder}\${TomcatServiceBat}" ;���� ����������� Tomcat ��� ������� - ���������������� - ������ ������ JVM, �������� ��������� ������� --Startup=auto
	
    SetOutPath "$TomcatPATH\conf"
	File "${InclFolder}\${TomcatServerXml}" ; � �������� ����� �� ���������, ���������� ����� ���������
	File "${InclFolder}\${TomcatUserXml}"   ; ������������, ��� ����������� �����������������.
	IfFileExists "$TomcatPATH\conf\${TomcatServerXml}" 0 +2
	  DetailPrint "����������� ��������� ������: $TomcatPATH\conf\${TomcatServerXml}"

  #��������� ������� cmd /c set "JAVA_HOME=c:\ion\jdk"& SET "CATALINA_HOME=c:\ion\tomcat"& "c:\ion\tomcat\bin\service.bat" install Tomcat
   nsExec::ExecToStack 'cmd /C SET "JAVA_HOME=$JDKPATH"& SET "CATALINA_HOME=$TomcatPATH"& "$TomcatPATH\bin\service.bat" install $TomcatServiceName'
   pop $0
   DetailPrint "���������� ������ Tomcat: $TomcatServiceName $0"
  #��������� ������ ������
   nsExec::ExecToStack "NET START $TomcatServiceName"
   pop $0
   DetailPrint "������� ������ Tomcat: $TomcatServiceName $0"
SectionEnd

Section "��������� ����" SUBDSQLSec
  SectionIn 1 ; - �������������� ������ � ������ ���������
  DetailPrint "��������� ���� ${SUBDSQLFolder}"
  SetOutPath "$SUBDSQLPATH" ;����� ��� ���������� ���������
!ifdef PACK
  File /r ${DistrFolder}\${SUBDSQLFolder}\*.*
!endif

!ifdef PostgreSQL
##TODO ������������ postgreSQL � 9� ������ ��� ������ �� ����������? ��� ������������ ��� ������ �� �� �����
	UserMgr::CreateAccountEx "$SUBDUser" "$SUBDPsw" "$SUBDServiceName" "ION PostgreSQL Database User" "������������ ������ ������������� ��� ���������� ION" "UF_PASSWD_NOTREQD|UF_DONT_EXPIRE_PASSWD"
    pop $R0
    DetailPrint "��������� �������� ������������: result=$R0"
    UserMgr::AddPrivilege "$SUBDUser" "SeBatchLogonRight"
    pop $R0
    DetailPrint "����� SeBatchLogonRight: result=$R0"
    UserMgr::AddPrivilege "SUBDUser" "SeServiceLogonRight"
    pop $R0
    DetailPrint "����� SeServiceLogonRight: result=$R0"

!ifdef PACK	
    nsExec::ExecToStack '"$SUBDSQLPATH\bin\initdb.exe" --username=$SUBDUser --locale=Russian_Russia --encoding=UTF8 -D "$SUBDSQLPATH\data"'
    pop $R0
!endif
    DetailPrint "��������� ������������� ��: result=$R0"

    nsExec::ExecToStack 'sc create $SUBDServiceName binpath="$SUBDSQLPATH\bin\pg_ctl.exe runservice -N $SUBDServiceName -D $SUBDSQLPATH/data -w" DisplayName="ION PostgreSQL" start="auto" type= own obj=".\$SUBDUser" password="$SUBDPsw" '
!endif


!ifdef MSSQL
###TODO ������� ��������� MS SQL
!endif

!ifdef MySQL
    SetOutPath "$SUBDSQLPATH"
	File "${InclFolder}\${MySQLINIDef}" ; ��������� �� ���������

	!insertmacro SaveMySQLINI "$SUBDSQLPATH\${MySQLINIDef}"
    DetailPrint "�������� ��������� � ${MySQLINIDef}" ##TODO ��������� ������� �������� �����

    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqld.exe --install $SUBDServiceName --defaults-file="$SUBDSQLPATH\${MySQLINIDef}'
	pop $R0
	DetailPrint "����������� ������� $SUBDServiceName � ����������� ${MySQLINIDef}: result=$R0" ;1 - ������, 0 ������??

    nsExec::ExecToStack 'NET start $SUBDServiceName'
	pop $R0
	DetailPrint "������ $SUBDServiceName �������: result=$R0" ;1 - ������, 0 ������??

    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --host=$SUBDHost --port=$SUBDPort --user=$SUBDUser password $SUBDPsw '
	pop $R0
	DetailPrint "��������� ������ $SUBDUser: result=$R0" ;1 - ������, 0 ������??

!endif

    pop $R0
    DetailPrint "��������� ����������� ������� ����: result=$R0"
##TODO ���������, ��� ������ ���������� �����.	
    nsExec::ExecToStack 'NET START $SUBDServiceName'

SectionEnd
SectionGroupEnd


SectionGroup !"��������� ION" IONGroup
Section "������ ������" OfflicentSec 
    SectionIn RO ; - ������ ������ ��� ������, �.�. �� ������ ���������
	DetailPrint "��������� ������ �������" 
    SetOutPath "$IONAppPATH" ;����� ��� ���������� ���������
	
   nsExec::ExecToStack "NET STOP $TomcatServiceName"
   pop $0
   DetailPrint "���������� ������ Tomcat: $TomcatServiceName $0"
;!ifdef PACK
    File /r ${PlatfFolderSrc}\${IONPlatformFolder}\*.*  ; ������������� ���������� ��� �������
    File /r ${PlatfFolderSrc}\${IONAppFolder}\*.*  ; ������������� ������
;!endif
##TODO ���� ����� � ����� �������� ��� ����, �� ���� ��������� ���� ������� � ���, �� � ������ ���� - ����� ��������, � �� ���������� ��� ����������. � ����� ����� � ion.ini � ����� ���������, ���� ��� ���� ����� ��������, �� ���� ���� �� ��������� - ������ �� �������
	#��������� ��������� �� ��������� � ������������
    !insertmacro DB_properties "$IONAppPATH\WEB-INF\${IONDbProperties}" ##TODO ���� ����� ��� - �� ������� ���������
	DetailPrint "����������� ��������� �� �� ���������: $IONAppPATH\WEB-INF\${IONDbProperties}"
	!insertmacro Model_properties "$IONAppPATH\WEB-INF\${IONModelProperties}" ##TODO ���� ����� ��� - �� ������� ���������
	DetailPrint "����������� ��������� ������: $IONAppPATH\WEB-INF\${IONModelProperties}"	
	
   nsExec::ExecToStack "NET START $TomcatServiceName"
   pop $0
   DetailPrint "������� ������ Tomcat: $TomcatServiceName $0"
SectionEnd 

!ifdef IONSYNC 
Section "������ �������������" SyncSec 
  SectionIn RO ; ������ ������ ��� ������, �.�. �� ������ ���������.
  DetailPrint "��������� ������� ������������� ������ �������" 
  SetOutPath "$IONOfServicePATH" ;����� ��� ���������� ������� ��������������
;!ifdef PACK
  File /r ${PlatfFolderSrc}\${IONOfflineSyncName}\*.*  ; ������������� ������ �������������
;!endif
##TODO ���� ����� � ����� �������� ��� ����, �� ���� ��������� ���� ������� � ���, �� � ������ ���� - ����� ��������, � �� ���������� ��� ����������
##TODO ��� ���� IONSyncINI - ����� ���������� � ��������� ����������� ���� � ����������� ������������� - ����� ���� ����� ������.
 
  IfFileExists "$IONSyncINI" 0 copydefprop   
    #������ ����������� �������� ������� � ����������������� ����� � ����� �����������
    DetailPrint "������ ��������� ������������ ������� �������������: $IONSyncINI"  
    !insertmacro ReadDaemonIni "$IONSyncINI"  ##TODO ��� ��������� ����� ����� ������� ������������� � �����������. 
	goto saveprof
  copydefprop:
    StrCmp $adapterUrl "" 0 +2
      StrCpy $adapterUrl "http://$IONOflSrvHost:$IONOflSrvPort/$IONOflSrvName/"
  # ������ �������� �������
  saveprof:
  !insertmacro Daemon_properties "$IONOfServicePATH\${IONDaemonProperties}"
  DetailPrint "�������� ��������� �������: $IONOfServicePATH\${IONDaemonProperties}"
  
#������������ ������ � �������
    nsExec::ExecToStack '$IONOfServicePATH\${IONSyncInit} install ${IONOfflineSyncName} $IONOfServicePATH $JDKPATH'
	pop $R0
	DetailPrint '������ ��������������� $IONOfServicePATH\${IONSyncInit} install ${IONOfflineSyncName} $IONOfServicePATH $JDKPATH: result=$R0'
	nsExec::ExecToStack 'NET START ${IONOfflineSyncName}'
	pop $R0
	DetailPrint '������ ������� �������: result=$R0' 	
SectionEnd 
!endif

SectionGroupEnd

Section "��������� ������������� ���� ������" BDSec
  SectionIn 1 ; �������������� ������ � ������ ���������
  DetailPrint "��������� ���������������� ����" 
  SetOutPath "$SUBDSQLPATH"
  File ${InclFolder}\${SQLInitDB} ;��������� ������ �������������

  IfFileExists "$SUBDSQLPATH\${SQLInitDB}" 0 err
    DetailPrint "������ ���������������� SQL ������"
!ifdef PostgreSQL	
    nsExec::ExecToStack 'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\dropdb.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser -e $DBName'
	pop $R0
	DetailPrint "��������� �������� ������ ����: result=$R0" ;0 - �������, 1 ������
	nsExec::ExecToStack  'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\createdb.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser  -E UTF8 -O $SUBDUser $DBName'
	pop $R0
	DetailPrint "��������� �������� ����� ����: result=$R0" ;0 - �������, 1 ������
	nsExec::ExecToStack  'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\psql.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser  -d $DBName -f "$SUBDSQLPATH\${SQLInitDB}"'
	pop $R0		
!endif

!ifdef MSSQL
###TODO ������� ��������� MS SQL
!endif

!ifdef MySQL
    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort -f drop $DBName'
	pop $R0
	DetailPrint "��������� �������� ������ ����: result=$R0" ;1 - ������, 0 ������

	nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort create $DBName'
	pop $R0
	DetailPrint "��������� �������� ����� ����: result=$R0" ;1 - ������, 0 ������

	${StrRep} $0 $SUBDSQLPATH '\' '\\'
	nsExec::ExecToStack '$SUBDSQLPATH\bin\mysql.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort --database=$DBName -e "source $0\\${SQLInitDB}'
	pop $R0
	DetailPrint "��������� �������� ����� ���� $0\\${SQLInitDB}: result=$R0" ;1 - ������, 0 ������	
!endif
  Delete $SUBDSQLPATH\${SQLInitDB} ;������� ���������������� ������
  goto endprop
err:
   DetailPrint "�� ������ ���������������� SQL ������"
   goto +3
endprop:
  DetailPrint "�������������� ����: result=$R0"

SectionEnd 
;--------------------------------
; �������� ��������������� ����������� � �������
;--------------------------------

  ;Language strings
  LangString DESC_Total ${LANG_RUSSIAN} "������ ������������." ;TODO �������� ����� �����

#�������� ��������������� ����������� � �������
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${BDGroup} "C����� ����������, ����������� ������ � ����"

!insertmacro MUI_DESCRIPTION_TEXT ${JDKSec} "����� ���������� Java"
SectionSetSize JDKSec 306916
!insertmacro MUI_DESCRIPTION_TEXT ${TomcatSec} "������ ���������� Tomcat"
SectionSetSize TomcatSec 14219
!insertmacro MUI_DESCRIPTION_TEXT ${SUBDSQLSec} "��������� ���� ${SUBDSQLFolder}"
!ifdef PostgreSQL	
  SectionSetSize SUBDSQLSec 168583
!endif
!ifdef MSSQL
  SectionSetSize SUBDSQLSec 300000 ##TODO �������� ������
!endif
!ifdef MySQL
  SectionSetSize SUBDSQLSec 378925
!endif

!insertmacro MUI_DESCRIPTION_TEXT ${IONSec} "�������� ���������� ��������� ION"
!insertmacro MUI_DESCRIPTION_TEXT ${OfflicentSec} "������ ������"
SectionSetSize OfflicentSec 52611
!ifdef IONSYNC 
!insertmacro MUI_DESCRIPTION_TEXT ${SyncSec} "������ �������������"
SectionSetSize SyncSec 26066
!endif
!insertmacro MUI_DESCRIPTION_TEXT ${BDSec} "���������������� ���� ������. ����������� ��� ��������� ��������� �������"
SectionSetSize BDSec 9

!insertmacro MUI_FUNCTION_DESCRIPTION_END 

  
  
;--------------------------------
;������ ������������ 
;--------------------------------

Section "Uninstall"
   !insertmacro IONReadINI "$INSTDIR\${IONINI}"
   !insertmacro ION_Def ;������ �������� ���������� �� ��������� 
   ##TODO �������� ��� ������ ����� ������ un (!!!)

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP ; �������� �� ������� ���� � ������� � ������� ��
  Delete "$SMPROGRAMS\$MUI_TEMP\$IONAppName.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\�������� $IONAppName.lnk"

;������� �������� ������ ���� ����
   StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"
  startMenuDeleteLoop:
  RMDir $MUI_TEMP
  GetFullPathName $MUI_TEMP "$MUI_TEMP\.."

  IfErrors startMenuDeleteLoopDone
   StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
startMenuDeleteLoopDone:

!ifdef IONSYNC 
  	nsExec::ExecToStack 'NET STOP ${IONOfflineSyncName}'
    nsExec::ExecToStack '$IONOfServicePATH\${IONSyncInit} remove ${IONOfflineSyncName} $IONOfServicePATH'
!endif

   nsExec::ExecToStack 'NET STOP "$TomcatServiceName"'
   pop $R0
   nsExec::ExecToStack 'cmd /C SET "JAVA_HOME=$JDKPATH"& SET "CATALINA_HOME=$TomcatPATH"& "$TomcatPATH\bin\service.bat" remove "$TomcatServiceName"' ;������� ������ ������� �� �������
   pop $R0
   DetailPrint "��������� �������� ������� Tomcat: result=$R0"

    nsExec::ExecToStack 'NET STOP "$SUBDServiceName"'
	pop $R0
!ifdef PostgreSQL	
	nsExec::ExecToStack 'sc delete "$SUBDServiceName"'
!endif
!ifdef MSSQL
##TODO  �������� MS SQL
!endif
!ifdef MySQL
    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqld.exe --remove $SUBDServiceName'
!endif
    pop $R0
    DetailPrint "��������� �������� ������� ����: result=$R0"

  RMDir /r "$INSTDIR" ;������� ���. � �� ���� ��������� ���� ���������� ���������. � ��� ��������� ��������� - ���� �� ������ �����������...
  Delete "$INSTDIR\Uninstall.exe"  ; �������� ������
	
  DeleteRegKey /ifempty HKCU "Software\ION" ; ������� ���� �� �������...

SectionEnd