!macro DB_properties File_prop
  ClearErrors  
  Push $0
  FileOpen $0 ${File_prop} w 
  IfErrors done

!ifdef MySQL
  FileWrite $0 "$\r$\ndb.driver=com.mysql.jdbc.Driver"
  FileWrite $0 "$\r$\ndb.url=jdbc:mysql://$SUBDHost:$SUBDPort/$DBName"
  FileWrite $0 "$\r$\ndb.dialect=org.hibernate.dialect.MySQLDialect"
!endif
!ifdef PostgreSQL
  FileWrite $0 "$\r$\ndb.driver=org.postgresql.Driver"
  FileWrite $0 "$\r$\ndb.url=jdbc:postgresql://$SUBDHost:$SUBDPort/$DBName"
  FileWrite $0 "$\r$\ndb.dialect=org.hibernate.dialect.PostgreSQLDialect"
!endif
!ifdef MSSQL
  FileWrite $0 "$\r$\ndb.driver=ccom.microsoft.sqlserver.jdbc.SQLServerDriver"
  FileWrite $0 "$\r$\ndb.url=jdbc:sqlserver://$SUBDHost:$SUBDPort/$DBName"
  FileWrite $0 "$\r$\ndb.dialect=org.hibernate.dialect.SQLServer2008Dialect"
!endif

FileWrite $0 "$\r$\ndb.user=$SUBDUser"
FileWrite $0 "$\r$\ndb.password=$SUBDPsw"

done:   
   FileClose $0 
   Pop $0
!macroend 

!macro SaveMySQLINI File_ini
  WriteINIStr ${File_ini} "mysqld" "bind-address" "$SUBDHost"
  WriteINIStr ${File_ini} "mysqld" "port" "$SUBDPort"
  ${StrRep} $R0 $SUBDSQLPATH '\' '\\'
  WriteINIStr ${File_ini} "mysqld" "basedir" $R0 ##TODO проверить, что \ срабатывает, а не надо менять на \\ или /
  WriteINIStr ${File_ini} "mysqld" "datadir" "$R0\\data" ##TODO проверить, что \ срабатывает, а не надо менять на \\ или /
  WriteINIStr ${File_ini} "mysqld" "sql_mode" "NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES"
!macroend 