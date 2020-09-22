#! /bin/sh
#  /etc/init.d/mydaemon

### BEGIN INIT INFO
# Provides:          mydaemon
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the Ion Target System requestor service
# Description:       This file is used to start the daemon
#                    and should be placed in /etc/init.d
### END INIT INFO

JAVA_HOME=/usr/java/jdk1.7.0_25;
export JAVA_HOME


NAME="ion-offline-server"
DESC="Ion target system requestor service"

# The path to Jsvc
EXEC="/usr/java/jsvc"

# The path to the folder containing MyDaemon.jar
FILE_PATH="/srv/offline"

# The path to the folder containing the java runtime
#JAVA_HOME="/usr/java/latest/bin"

# Our classpath including our jar file and the Apache Commons Daemon library
CLASS_PATH="$FILE_PATH/daemon.jar:$FILE_PATH"

# The fully qualified name of the class to execute
CLASS="ion.framework.offline.server.UnixDaemon"

#The user to run the daemon as
USER="root"

# The file that will contain our process identification number (pid) for other scripts/programs that need to access it.
PID="/var/run/$NAME.pid"

# System.out writes to this file...
LOG_OUT="$FILE_PATH/log/out.log"

# System.err writes to this file...
LOG_ERR="$FILE_PATH/log/err.log"

jsvc_exec()
{  
    cd $FILE_PATH
    $EXEC -debug -Xmx256m -XX:+UseG1GC -home $JAVA_HOME -cp $CLASS_PATH -user $USER -cwd $FILE_PATH -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS
}

case "$1" in
    start) 
        echo "Starting the $DESC..."       
       
        # Start the service
        jsvc_exec
       
        echo "The $DESC has started."
    ;;
    stop)
        echo "Stopping the $DESC..."
       
        # Stop the service
        jsvc_exec "-stop"      
       
        echo "The $DESC has stopped."
    ;;
    restart)
        if [ -f "$PID" ]; then
           
            echo "Restarting the $DESC..."
           
            # Stop the service
            jsvc_exec "-stop"
           
            # Start the service
            jsvc_exec
           
            echo "The $DESC has restarted."
        else
            echo "Daemon not running, no action taken"
            exit 1
        fi
            ;;
    *)
    echo "Usage: /etc/init.d/$NAME {start|stop|restart}" >&2
    exit 3
    ;;
esac
