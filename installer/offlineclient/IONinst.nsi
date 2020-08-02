;NSIS Modern User Interface
;Basic Example Script
;Written by Joost Verburg

#Если пользователь выбрал расширенный решим установки - то показываем настройки с ИНИ файла или по-умолчанию с возможностью редактирования.

/*Контроль установки с нуля:
+ создан uninstall
+ записаны настройки ion.ini - см.выше
+ jdk развернулся
+ томкат полностью поставился, файлы serve.xml, tomcat-user.xml, service.bat подкопированы, сервис зарегистрировался, запущен
+ томкат сервис зарегистрирован как запуск "авто"
+MySQL развернулся полностью, my.ini скопировался, настройки добавлены, сервис запущен с настройками my.ini, пользовательь root пароль добавлены
+	база offlinedb развернулась
+ платформа скопировалась в томкат
+ модель скопировалась в томкта, настройки в db.properties и model.properties - записаны
+ сервис синхронизации скопировался, настроки daemon.properties записан, сервис зарегисрирован. 
+ сервис синхронизации запустился, тип запуска "авто" логи нормальные - нет пользователя
+группы меню создались
+ приложение в томкат - запущено


*/


#1. Простой вариант установки:
#   * Добавить ссылку на локальный томкат в закладки броузера или создать линк.
#   * регистрировать офлайн сервис синхронизации
#   * базу инициализации MySQL подправить
#   * проверить, что работает подкомпиляция классов, без указания JAVA_PATH и прописи в PATH к папке JDK\bin. Если не работает задавать эти переменные для системы.
#2. Альфа вариант
#   * Поверить стабильность работы если в пути пробелы - передачу сервису томкта JDK.
#   * проверять установку jdk, проверять переменные JAVA_PATH, пути в PATH. Если установлено, то не устанавливать JDK. Если нет - прописывать Java_HOME, Указать java bin в путь
#   * проверять установку tomcat. Проверять, что установлен как сервис. Если установлен, только подкопировывать файлы в папку. Если не установлен прописывать установку правильно, в т.ч. CATALINA_HOME=%PATH%\Tomcat
#	* проверять установку postgress или mysqll. Если установлен спрашивать пароль, логин. Подгружать базу.

#3. бетта вариант.
#   * выбор типа базы данных из установленных
#   * настройка названия базы.
#   * порты и адрес сервера баз.данных. Прокидывание их в клиент и в сервис.
#   * возможность указания адреса офлайн сервера

#3. Relice Candidat
#   * возможность настроек офлайн клиента (ретроспектива подгрузки данных с офлайн сервера)
#   * возможность регистрации пользователя сразу
#   * возможность настройки интервалов обновления офлайн сервиса.
#   * возможность подгрузки первичных обновлений с папки установки или вручную (если сервер будет гененрировать с клиентом актуальный состав доп. данных - справочники, базовые модели, представления)
;--------------------------------
;Для работы нужны нижеследующие библиотеки. Как установить к NSIS http://nsis.sourceforge.net/How_can_I_install_a_plugin . 
;--------------------------------
/*
UserMgr   http://nsis.sourceforge.net/UserMgr_plug-in
*/

;--------------------------------
;Включаемые функции NSIS
;--------------------------------
  !include "StrFunc.nsh"  ;работа со строками, в частности замена слеша на двойной слеш
  ${StrRep} ;декларируем используемые функции макросов работы со строкой


#Настройки установщика
  !define PACK ; нужно закомментировать, если компиляция инсталятора для  отладки и убрать коммент - если продуктив. При комменте не будет упаковывать в инсталятор установочные файлы и при установке их не будет распаковывать (предполагается, что они уже есть в папке инсталяции). Иначе эти операции будут пропущены, для удобства отладки
  !define IONSYNC  ;Включаем в пакет модуль синхронизации

#Тип СУБД
;  !define PostgreSQL ###TODO можно выбрать две СУБД и будет ерунда.
  !define MySQL
;  !define MSSQL


#Включение настроек по умолчанию и шаблонов
  !include 				".\include\IONInstInit.nsh" ;задание настроек по умолчанию, считывание настроек с INI, запись результирующих в INI
  !include 				".\include\db_properties.nsh" ;Шаблон настроек базы данных для платформы, макрос записи конфига MySQL - с подстановокой переменных инсталляции по умолчанию или введенных пользователем 
  !include 				".\include\model_properties.nsh" ;Шаблон настроек базы данных для платформы, макрос записи конфига MySQL - с подстановокой переменных инсталляции по умолчанию или введенных пользователем 
!ifdef IONSYNC 
 !include 				".\include\daemon_properties.nsh" ;Шаблон настроек сервиса синхронизации,с подстановокой переменных инсталляции и настроек из INI
!endif
  !include				".\include\ionservice.nsh" ;файл записи батников для работы с сервисами
  
  !define InclFolder 		"include"  #Папка с конфигруационными файлами для включения в состав архива дистрибутива.
  !define IONDbProperties	"db.properties"			;настройка подключения к базе для платформы: tomcat\webapps\ion-web-platform\WEB-INF\db.properties
  !define IONModelProperties "model.properties"   ;настройка модели ION



#Папка с платформой. Ниже папки инсталируемых продуктов, могут меняться
  !define PlatfFolderSrc 		"ion"
    !define IONINI 	"ion.ini" ;файл с настройками инсталяции
	!define IONURL 	"ion.url" ;файл с URL для перехода к платформе
	!define IONPlatformFolder "ion-web-platform"		;папка с источником платформы
	Var IONAppName
	Var IONAppPATH
      !define IONAppFolder "ion-offline"		;папка для webapps tomcat
	Var IONModelSmevPckg
	  !define IONModelSmevPckgDef "ion.domain.smev"


	
!ifdef IONSYNC 
   Var IONOfServicePATH 
      !define IONOfflineSyncName "ion-offline-sync"	;офлайн сервис синхронизации офлайн клиента
	Var IONSyncINI
      !define IONSyncINIDef 	"IONSync.ini" 	  ;файл настроек сервиса синхронизации, должны лежать в папке установки вместе инсталятором
	  
	!define IONDaemonProperties	"daemon.properties"  ;настройки по умолчанию для сервиса синхронизации офлайн клиента
	!define IONSyncInit	"ionservice.bat" 		     ;командный файл инициализации сервиса
	
    Var IONOflSrvHost 		;Host офлайн сервера
	  !define IONOflSrvHostDef "172.18.225.55"	
	Var IONOflSrvPort 		;Порт офлайн сервера
	  !define 	IONOflSrvPortDef "80"
	Var IONOflSrvName 		;Путь к приложению tomcat офлайн сервера
	  !define	IONOflSrvNameDef "offline"
	
!endif
  
#Папка с приложениями. Папки инсталируемых дистрибутивов, могут меняться
  !define DistrFolder 		"distr"
	  
#Настройки JDK
	Var JDKPATH
      !define JDKFolder 		"jdk" 					;папка с JDK
	  
#Настройки томкат
	Var TomcatPATH 
      !define TomcatFolder 	"tomcat" 				;папка с томкатом
      !define TomcatServerXml "server.xml"			;настройки сервера Tomcat: tomcat\conf\server.xml - находятся в папке InclFolder 
	  !define TomcatServiceBat "service.bat"			;файл регистрации сервиса Tomcat - находится в папке InclFolder. в нем задаются параметры tomcat как сервиса, в частности память --JvmMx 512.
	  !define TomcatUserXml "tomcat-users.xml"			;пользователи Tomcata - чтобы управлять приложениями томкта
	Var TomcatServiceName ;Название сервиса томката
	  !define TomcatServiceNameDef "Tomcat"
	Var TomcatPort
	  !define TomcatPortDef "8080"
	Var TomcatHost  ;не нужно, т.к. только localhost
	  !define TomcatHostDef "localhost"

#Настройки СУБД
	Var SUBDSQLPATH
	Var SUBDHost
	  !define SUBDHostDef "localhost"	  
	Var DBName
	  !define DBNameDef "offlinedb"
	Var SUBDPort
	Var SUBDUser
	Var SUBDServiceName 		#TODO возможно имеет оставить только значения по умолчанию?
!ifdef PostgreSQL
      !define SUBDSQLFolder 	"pgsql" 				;папка с PostrgeSQL
	  !define SUBDPortDef "5432" 
	  !define SUBDUserDef  "postgres" 
	  !define SUBDServiceNameDef  "PostrgeSQL" 
	  !define SQLInitDB "psinit.sql" 				;Скрипт инициализации БД PostrgeSQL
!endif
!ifdef MSSQL
	   !define SUBDSQLFolder 	"mssql" 				;папка с MSSQL
       !define SUBDPortDef "1433" 
       !define SUBDUserDef  "sa" 
       !define SSUBDServiceNameDef  "MSSQL" 
	   !define SQLInitDB "msinit.sql" 				;Скрипт инициализации БД MSSQL
   
!endif
!ifdef MySQL
  	   !define SUBDSQLFolder 	"mysql" 				;папка с MySQL
       !define SUBDPortDef "3306"
       !define SUBDUserDef  "root"
       !define SUBDServiceNameDef  "MySQL"
	   !define SQLInitDB "myinit.sql"				;Скрипт инициализации БД MySQL
	   
       !define MySQLINIDef 	"my.ini"
!endif
	Var SUBDPsw
	  !define SUBDPswDef "ION-sql1"
 

#Название инсталяции и выходной файл
  Name "ION офлайн клиент"
  OutFile "IONinst.exe"
 
# Параметры инсталлятора 
  
  InstallDirRegKey HKCU "Software\ION" ""  ;Get installation folder from registry if available
  RequestExecutionLevel admin ; Уровень доступа администратор
 
  ;Путь корневой папки для установки всех компонент, для уменьшения проблем с Java, пути без пробелов
  InstallDir "C:\ION"   ;"Для случая, проверки работосопособности в папках с проебалми - - установка папку приложений

  InstType "Полная" ; - тип установки
  InstType "Обновление платформы" ; - тип установки - без дистрибутивов томкат, СБУД и JDK
#  SetCompressor /solid lzma ; - использовать непрерывное сжатие Lzma
#  SetDatablockOptimize on ; - включить оптимизацию данных блока
#  CRCCheck off ; - не проверять контрольную сумму инсталлятора
#  WindowIcon off ; - выключаем иконку у окна инсталлятора
#  XPStyle on ; - включаем использование стиля XP
#  SetOverwrite on ; - возможность перезаписи файлов включена
#  AllowRootDirInstall false ; - отменяем возможность установки программы в корень
#  AutoCloseWindow false ; - отмена автозакрытия инсталлятора после выполнения всех действий
  
  
  ; Настройка интерфейса
#  BrandingText "ION платформа" ; - текст внизу инсталляции
#  BGGradient 0x0000FF 0x000080 0x0080FF ; - устанавливаем фон установки (верхний/нижний/цвет текста)
#  !define MUI_HEADERIMAGE ; - возможность запихнуть в заголовок рисунок
#  !define MUI_HEADERIMAGE_BITMAP "modern-header.bmp" ; - сам рисунок в заголовке
#  !define MUI_HEADERIMAGE_RIGHT ; - рисунок в заголовке будет находиться справа
#  !define MUI_ICON "Install.ico" ; - иконка инсталлятора
#  !define MUI_UNICON "UnInstall.ico" ; - иконка деинсталлятора
#  !define MUI_COMPONENTSPAGE_SMALLDESC ; - увеличивает место под компоненты
#  !define MUI_ABORTWARNING ; - предупреждать об отмене установки
#  !define MUI_LICENSEPAGE_RADIOBUTTONS ; - Использовать радиокнопки на странице лицензии
  

	Var MUI_TEMP ; - две переменные для хранения пути для ярлыков в меню Пуск
    Var STARTMENU_FOLDER 	

;--------------------------------
;Настройки интерфейса Modern UI
;--------------------------------
  !include "MUI2.nsh"
  !define MUI_ABORTWARNING

;--------------------------------
;Страницы установки
;--------------------------------
  !insertmacro MUI_PAGE_WELCOME ; - Страница приветствия
  !insertmacro MUI_PAGE_LICENSE ".\${InclFolder}\License.txt" ; - Страница с лицензией
  !insertmacro MUI_PAGE_COMPONENTS ; - страница компонентов
  !insertmacro MUI_PAGE_DIRECTORY ; - страница выбора папки установки
  !insertmacro MUI_PAGE_INSTFILES ; - страница хода выполнения установки
  
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER ; - страница с выбором группы ярлыков  

  !insertmacro MUI_UNPAGE_CONFIRM ; - страница проверки пути деинсталлятором
  !insertmacro MUI_UNPAGE_INSTFILES ; - страница хода выполнения удаления
  
;--------------------------------
;Языки
;-------------------------------- 
  !insertmacro MUI_LANGUAGE "Russian"

;--------------------------------
;Меню "Пуск" настройки реестра
;--------------------------------  
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\ION"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Label"


;--------------------------------
;Компоненты установки
;--------------------------------

Section "Компоненты платформы ION" IONSec #Общая секция с настройками для ION
    SectionIn RO ; - Секция только для чтения, т.е. ее нельзя отключить

	CreateDirectory "$INSTDIR"
    !insertmacro IONReadINI "$EXEDIR\${IONINI}"
	!insertmacro ION_Def ;Задаем значения переменных по умолчанию, если они не были заданы в INI
####TODO Ввод настроек для устанавливаемых tomcat, postgress
##    !insertmacro IONReadUser Страница считывания настроек от пользователя (расширенный режим) - макроса пока нет.

###TODO Вместо ини возможно  их копировать в реестр??
###TODO нужно проверять, что папки пустые
###TODO нужно проверять, что порты свободны
###TODO нужно проверять, что сервисы такие не зарегистрированы
	
  !insertmacro IONShortCut "$INSTDIR\${IONURL}" ;создания url файлс с ссылкой на приложение в томкате
###TODO  ;Создание ярлыков для запуска бразера с адресом платформы
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\$IONAppName.lnk" "$INSTDIR\${IONURL}"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Удаление $IONAppName.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END 

    WriteRegStr HKCU "Software\ION" "PATH" $INSTDIR ;Сохраняем в реестр путь установки
    WriteUninstaller "$INSTDIR\Uninstall.exe"		;Создаем uninstaller
	

	!insertmacro IONSaveINI "$INSTDIR\${IONINI}" ;записываем настройки инсталяции в ini файл

	!insertmacro IONSeviceStartSave 
	!insertmacro IONSeviceStopSave
	!insertmacro IONSeviceInstSave
	!insertmacro IONSeviceRemoveSave 
	
SectionEnd

SectionGroup "Среда исполнения" BDGroup
Section "Java" JDKSec
  SectionIn 1 ; - Принадлежность секции к полной установки
  DetailPrint "Установка JDK" 
  SetOutPath "$JDKPATH" ;папка для распаковки платформы
!ifdef PACK
  File /r ${DistrFolder}\${JDKFolder}\*.*
!endif

  # настройка JDK - пока не требуется, т.к. задаем через параметры для tomcat    
  ###TODO: полезно почистить установочные папки JDK от не используемых файлов.
SectionEnd 

Section "Tomcat" TomcatSec
  SectionIn 1 ; - Принадлежность секции к полной установки
  DetailPrint "Установка Tomcat"
  SetOutPath "$TomcatPATH" ;папка для распаковки платформы
!ifdef PACK
  File /r "${DistrFolder}\${TomcatFolder}\*.*"
!endif

#настройка Tomcat - ####TODO правильнее было бы в эти файлы записывать заданные настройки, но файл XML и bat обрабатывать в NSIS сложно и как шаблон большеват и типовые настройки прийдется отлавливать
    SetOutPath "$TomcatPATH\bin"
	File "${InclFolder}\${TomcatServiceBat}" ;файл регистрации Tomcat как сервиса - модифицированный - больше памяти JVM, добавлен автостарт сервиса --Startup=auto
	
    SetOutPath "$TomcatPATH\conf"
	File "${InclFolder}\${TomcatServerXml}" ; в основном порты по умолчанию, автодеплой можно настроить
	File "${InclFolder}\${TomcatUserXml}"   ; пользователи, для возможности администрирования.
	IfFileExists "$TomcatPATH\conf\${TomcatServerXml}" 0 +2
	  DetailPrint "Установлены настройки Томкат: $TomcatPATH\conf\${TomcatServerXml}"

  #Установка сервиса cmd /c set "JAVA_HOME=c:\ion\jdk"& SET "CATALINA_HOME=c:\ion\tomcat"& "c:\ion\tomcat\bin\service.bat" install Tomcat
   nsExec::ExecToStack 'cmd /C SET "JAVA_HOME=$JDKPATH"& SET "CATALINA_HOME=$TomcatPATH"& "$TomcatPATH\bin\service.bat" install $TomcatServiceName'
   pop $0
   DetailPrint "Установлен сервис Tomcat: $TomcatServiceName $0"
  #Запускаем сервис томкат
   nsExec::ExecToStack "NET START $TomcatServiceName"
   pop $0
   DetailPrint "Запущен сервис Tomcat: $TomcatServiceName $0"
SectionEnd

Section "Установка СУБД" SUBDSQLSec
  SectionIn 1 ; - Принадлежность секции к полной установки
  DetailPrint "Установка СУБД ${SUBDSQLFolder}"
  SetOutPath "$SUBDSQLPATH" ;папка для распаковки платформы
!ifdef PACK
  File /r ${DistrFolder}\${SUBDSQLFolder}\*.*
!endif

!ifdef PostgreSQL
##TODO Пользователь postgreSQL с 9й версии под виндой не обязателен? как пользователи для логина он не виден
	UserMgr::CreateAccountEx "$SUBDUser" "$SUBDPsw" "$SUBDServiceName" "ION PostgreSQL Database User" "Пользователь создан автоматически при инсталяции ION" "UF_PASSWD_NOTREQD|UF_DONT_EXPIRE_PASSWD"
    pop $R0
    DetailPrint "Результат создания пользователя: result=$R0"
    UserMgr::AddPrivilege "$SUBDUser" "SeBatchLogonRight"
    pop $R0
    DetailPrint "Права SeBatchLogonRight: result=$R0"
    UserMgr::AddPrivilege "SUBDUser" "SeServiceLogonRight"
    pop $R0
    DetailPrint "Права SeServiceLogonRight: result=$R0"

!ifdef PACK	
    nsExec::ExecToStack '"$SUBDSQLPATH\bin\initdb.exe" --username=$SUBDUser --locale=Russian_Russia --encoding=UTF8 -D "$SUBDSQLPATH\data"'
    pop $R0
!endif
    DetailPrint "Результат инициализации БД: result=$R0"

    nsExec::ExecToStack 'sc create $SUBDServiceName binpath="$SUBDSQLPATH\bin\pg_ctl.exe runservice -N $SUBDServiceName -D $SUBDSQLPATH/data -w" DisplayName="ION PostgreSQL" start="auto" type= own obj=".\$SUBDUser" password="$SUBDPsw" '
!endif


!ifdef MSSQL
###TODO сделать установку MS SQL
!endif

!ifdef MySQL
    SetOutPath "$SUBDSQLPATH"
	File "${InclFolder}\${MySQLINIDef}" ; настройки по умолчанию

	!insertmacro SaveMySQLINI "$SUBDSQLPATH\${MySQLINIDef}"
    DetailPrint "Записаны настройки в ${MySQLINIDef}" ##TODO проверить двойные обратные слеши

    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqld.exe --install $SUBDServiceName --defaults-file="$SUBDSQLPATH\${MySQLINIDef}'
	pop $R0
	DetailPrint "Регистрация сервиса $SUBDServiceName с настройками ${MySQLINIDef}: result=$R0" ;1 - хорошо, 0 ошибка??

    nsExec::ExecToStack 'NET start $SUBDServiceName'
	pop $R0
	DetailPrint "Сервис $SUBDServiceName запущен: result=$R0" ;1 - хорошо, 0 ошибка??

    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --host=$SUBDHost --port=$SUBDPort --user=$SUBDUser password $SUBDPsw '
	pop $R0
	DetailPrint "Установка пароля $SUBDUser: result=$R0" ;1 - хорошо, 0 ошибка??

!endif

    pop $R0
    DetailPrint "Результат регистрации сервиса СУБД: result=$R0"
##TODO проверить, что сервис запустился сразу.	
    nsExec::ExecToStack 'NET START $SUBDServiceName'

SectionEnd
SectionGroupEnd


SectionGroup !"Платформа ION" IONGroup
Section "Офлайн клиент" OfflicentSec 
    SectionIn RO ; - Секция только для чтения, т.е. ее нельзя отключить
	DetailPrint "Установка офлайн клиента" 
    SetOutPath "$IONAppPATH" ;папка для распаковки платформы
	
   nsExec::ExecToStack "NET STOP $TomcatServiceName"
   pop $0
   DetailPrint "Остановлен сервис Tomcat: $TomcatServiceName $0"
;!ifdef PACK
    File /r ${PlatfFolderSrc}\${IONPlatformFolder}\*.*  ; Распаковываем приложение для томката
    File /r ${PlatfFolderSrc}\${IONAppFolder}\*.*  ; Распаковываем модель
;!endif
##TODO если папка и файлы конфигов уже есть, по идее настройки надо считать с них, но в секции выше - ввода настроек, а то перетрутся при распаковке. А можно брать с ion.ini в папке установки, если они были ранше записаны, но туда надо их сохранить - сейчас не пишется
	#настройки платформы по умолчанию в дистрибутиве
    !insertmacro DB_properties "$IONAppPATH\WEB-INF\${IONDbProperties}" ##TODO если папки нет - не запишет настройки
	DetailPrint "Установлены настройки БД по умолчанию: $IONAppPATH\WEB-INF\${IONDbProperties}"
	!insertmacro Model_properties "$IONAppPATH\WEB-INF\${IONModelProperties}" ##TODO если папки нет - не запишет настройки
	DetailPrint "Устанавлены настройки модели: $IONAppPATH\WEB-INF\${IONModelProperties}"	
	
   nsExec::ExecToStack "NET START $TomcatServiceName"
   pop $0
   DetailPrint "Запущен сервис Tomcat: $TomcatServiceName $0"
SectionEnd 

!ifdef IONSYNC 
Section "Сервис синхронизации" SyncSec 
  SectionIn RO ; Секция только для чтения, т.е. ее нельзя отключить.
  DetailPrint "Установка сервиса синхронизации офлайн клиента" 
  SetOutPath "$IONOfServicePATH" ;папка для распаковки сервиса синхроиназации
;!ifdef PACK
  File /r ${PlatfFolderSrc}\${IONOfflineSyncName}\*.*  ; Распаковываем сервис синхронизации
;!endif
##TODO если папка и файлы конфигов уже есть, по идее настройки надо считать с них, но в секции выше - ввода настроек, а то перетрутся при распаковке
##TODO Сам файл IONSyncINI - можно подгружать с заданными настройками пути в инсталяторе пользователем - тогда путь будет другой.
 
  IfFileExists "$IONSyncINI" 0 copydefprop   
    #Чтение специфичных настроек клиента с конфигурационного файла в папке установщика
    DetailPrint "Читаем настройки конфигруации сервиса синхронизации: $IONSyncINI"  
    !insertmacro ReadDaemonIni "$IONSyncINI"  ##TODO Все настройки можно также вводить пользователем в инсталяторе. 
	goto saveprof
  copydefprop:
    StrCmp $adapterUrl "" 0 +2
      StrCpy $adapterUrl "http://$IONOflSrvHost:$IONOflSrvPort/$IONOflSrvName/"
  # Запись настроек сервиса
  saveprof:
  !insertmacro Daemon_properties "$IONOfServicePATH\${IONDaemonProperties}"
  DetailPrint "Записаны настройки сервиса: $IONOfServicePATH\${IONDaemonProperties}"
  
#Регистрируем сервис в виндовс
    nsExec::ExecToStack '$IONOfServicePATH\${IONSyncInit} install ${IONOfflineSyncName} $IONOfServicePATH $JDKPATH'
	pop $R0
	DetailPrint 'Сервис зарегистрирован $IONOfServicePATH\${IONSyncInit} install ${IONOfflineSyncName} $IONOfServicePATH $JDKPATH: result=$R0'
	nsExec::ExecToStack 'NET START ${IONOfflineSyncName}'
	pop $R0
	DetailPrint 'Статус запуска сервиса: result=$R0' 	
SectionEnd 
!endif

SectionGroupEnd

Section "Первичная инициализация базы данных" BDSec
  SectionIn 1 ; Принадлежность секции к полной установки
  DetailPrint "Установка инициализирующей СУБД" 
  SetOutPath "$SUBDSQLPATH"
  File ${InclFolder}\${SQLInitDB} ;Извлекаем скрипт инициализации

  IfFileExists "$SUBDSQLPATH\${SQLInitDB}" 0 err
    DetailPrint "Найден инициализирующий SQL скрипт"
!ifdef PostgreSQL	
    nsExec::ExecToStack 'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\dropdb.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser -e $DBName'
	pop $R0
	DetailPrint "Результат удаления старой базы: result=$R0" ;0 - успешно, 1 ошибка
	nsExec::ExecToStack  'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\createdb.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser  -E UTF8 -O $SUBDUser $DBName'
	pop $R0
	DetailPrint "Результат создания новой базы: result=$R0" ;0 - успешно, 1 ошибка
	nsExec::ExecToStack  'cmd.exe /C SET PGPASSWORD=$SUBDPsw& "$SUBDSQLPATH\bin\psql.exe" -h $SUBDHost -p $SUBDPort -U $SUBDUser  -d $DBName -f "$SUBDSQLPATH\${SQLInitDB}"'
	pop $R0		
!endif

!ifdef MSSQL
###TODO сделать установку MS SQL
!endif

!ifdef MySQL
    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort -f drop $DBName'
	pop $R0
	DetailPrint "Результат удаления старой базы: result=$R0" ;1 - хорошо, 0 ошибка

	nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqladmin.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort create $DBName'
	pop $R0
	DetailPrint "Результат создания новой базы: result=$R0" ;1 - хорошо, 0 ошибка

	${StrRep} $0 $SUBDSQLPATH '\' '\\'
	nsExec::ExecToStack '$SUBDSQLPATH\bin\mysql.exe --user=$SUBDUser --password=$SUBDPsw --host=$SUBDHost --port=$SUBDPort --database=$DBName -e "source $0\\${SQLInitDB}'
	pop $R0
	DetailPrint "Результат создания новой базы $0\\${SQLInitDB}: result=$R0" ;1 - хорошо, 0 ошибка	
!endif
  Delete $SUBDSQLPATH\${SQLInitDB} ;Удаляем инициализирующий скрипт
  goto endprop
err:
   DetailPrint "Не найден инициализирующий SQL скрипт"
   goto +3
endprop:
  DetailPrint "Инициализирова СУБД: result=$R0"

SectionEnd 
;--------------------------------
; Описание устанавливаемых компонентов и размеры
;--------------------------------

  ;Language strings
  LangString DESC_Total ${LANG_RUSSIAN} "Секция тестирования." ;TODO уточнить зачем нужно

#Описание устанавливаемых компонентов и размеры
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${BDGroup} "Cервер приложений, виртуальная машина и СУБД"

!insertmacro MUI_DESCRIPTION_TEXT ${JDKSec} "Среда исполнения Java"
SectionSetSize JDKSec 306916
!insertmacro MUI_DESCRIPTION_TEXT ${TomcatSec} "Сервер приложений Tomcat"
SectionSetSize TomcatSec 14219
!insertmacro MUI_DESCRIPTION_TEXT ${SUBDSQLSec} "Установка СУБД ${SUBDSQLFolder}"
!ifdef PostgreSQL	
  SectionSetSize SUBDSQLSec 168583
!endif
!ifdef MSSQL
  SectionSetSize SUBDSQLSec 300000 ##TODO уточнить размер
!endif
!ifdef MySQL
  SectionSetSize SUBDSQLSec 378925
!endif

!insertmacro MUI_DESCRIPTION_TEXT ${IONSec} "Основные компоненты платформы ION"
!insertmacro MUI_DESCRIPTION_TEXT ${OfflicentSec} "Офлайн клиент"
SectionSetSize OfflicentSec 52611
!ifdef IONSYNC 
!insertmacro MUI_DESCRIPTION_TEXT ${SyncSec} "Сервис синхронизации"
SectionSetSize SyncSec 26066
!endif
!insertmacro MUI_DESCRIPTION_TEXT ${BDSec} "Инициализирующая база данных. Обязательна при первичной установке оффлайн"
SectionSetSize BDSec 9

!insertmacro MUI_FUNCTION_DESCRIPTION_END 

  
  
;--------------------------------
;Секция деинсталяции 
;--------------------------------

Section "Uninstall"
   !insertmacro IONReadINI "$INSTDIR\${IONINI}"
   !insertmacro ION_Def ;Задаем значения переменных по умолчанию 
   ##TODO возможно для вызова нужно писать un (!!!)

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP ; выдираем из реестра путь к ярлыкам и удаляем их
  Delete "$SMPROGRAMS\$MUI_TEMP\$IONAppName.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Удаление $IONAppName.lnk"

;Удаляем ненужные пункты меню Пуск
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
   nsExec::ExecToStack 'cmd /C SET "JAVA_HOME=$JDKPATH"& SET "CATALINA_HOME=$TomcatPATH"& "$TomcatPATH\bin\service.bat" remove "$TomcatServiceName"' ;Удаляем сервис томката из системы
   pop $R0
   DetailPrint "Результат удаления сервиса Tomcat: result=$R0"

    nsExec::ExecToStack 'NET STOP "$SUBDServiceName"'
	pop $R0
!ifdef PostgreSQL	
	nsExec::ExecToStack 'sc delete "$SUBDServiceName"'
!endif
!ifdef MSSQL
##TODO  удаление MS SQL
!endif
!ifdef MySQL
    nsExec::ExecToStack '$SUBDSQLPATH\bin\mysqld.exe --remove $SUBDServiceName'
!endif
    pop $R0
    DetailPrint "Результат удаления сервиса СУБД: result=$R0"

  RMDir /r "$INSTDIR" ;удаляет все. А по идее платформу надо предложить сохранить. И при установке проверять - была ли раньше установлена...
  Delete "$INSTDIR\Uninstall.exe"  ; удаление файлов
	
  DeleteRegKey /ifempty HKCU "Software\ION" ; удаляем ключ из реестра...

SectionEnd