moskito-javaagent
=================


# Basic ussage description.

### 1) Build and enable.

  a). Get javaagent-1.0.0-SNAPSHOT.jar  artifact from {javaagent-home}/target - directory  and   put it to  some location
  b). Get {javaagent-home}/appdata  directory to same location

### 2) Add  moskito javaagent to You're app, as  java-agent
   	as example:
			export JAVA_OPTS=" $JAVA_OPTS -javaagent:/[fulll   path]/javaagent/target/javaagent-1.0.0-SNAPSHOT.jar”
			#export JAVA_OPTS=" $JAVA_OPTS -Dcom.sun.management.jmxremote”     —- OPTIONAL
			#export JAVA_OPTS=" $JAVA_OPTS -Dcom.sun.management.jmxremote.port=10000” —- OPTIONAL
			#export JAVA_OPTS=" $JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"—- OPTIONAL


### 3) Configuring moskito-javaagent-config.json
This configuration allow to pre-select   working mode (PROFILING / LOG_ONLY)  — in log only mode all   class/methods  will be simply dumped into  info - log
in PROFILING mode - core  moskito functionality will  take a part.

a) Add monitoring sections ( Patterns  defines classes which will be weaver ( wrapped into monitoring aspect ))!
>        {
>           "patterns": ["com.test.*"],
>           "subsystem": "default",
>           "category": “foo-bar"
>        }

(in logs & in scope of producers UI view  You will see "com.test.Foo" and ,"com.test.Bar" entries…… )

b)	In case if you want to connect  from  mosquito ui - select   port  - 11111 - default and  enable  it  using  properties.
> 			 "startMoskitoBackend": true,
>  			 "moskitoBackendPort": 11111

### 4) Logging configuration changes.

[moskito-aspect-config.json]  allow to provide  other   logger names.  By default  loggers will beMoskitoDefault ( see logback.xml),
 Moskito1m, Moskito1h - etc….

NOTE :
"attachDefaultStatLoggers": true  - enables logback logging for defaults stats
2 other properties "defaultMoskitoLoggerName": “",  -  defines default logger name  ( “MoskitoDefault” -  by  global default )
"@loggers": [],  -  allow to   create  intervalName - logger name mapping
like :
> 		{
> 			“intervalName” : “1s”,
>			“loggerName” : “MoskitoOneSecondIntervalLogger”
>		}

In app data - defaults for logging specified in logback.xml. In case If you want to rename  some moskito loggers.

### 5) Connection from inspect moskito application with UI.
* Run moskito inspect application.
* Navigate to producer section.
* Provide host (localhost for local connections), port ( those  which was configured in section 2.b ("moskitoBackendPort").
* Connect - and find Your producers.

Enjoy…