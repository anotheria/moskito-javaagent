moskito-javaagent
=================


# Basic usage description.

### 1) Build and enable.

  a). Get javaagent-1.0.0-SNAPSHOT.jar  artifact from {javaagent-home}/target - directory  and   put it to  some location
  b). Get {javaagent-home}/target/appdata  directory to same location ( provides all required configurations and will be used as bootPath for agent)

### 2) Add  moskito javaagent to your app, as  java-agent, and provide applications packages to be monitored.
   as example:   	
   	   
		export JAVA_OPTS=" $JAVA_OPTS -javaagent:/[full   path]/javaagent/target/javaagent-1.0.0-SNAPSHOT.jar"
		export JAVA_OPTS=" $JAVA_OPTS export JAVA_OPTS="$JAVA_OPTS -DapplicationPackages=com.test,com.anothertest"

  By default following classes are monitored: \*DAO\*, \*Repository\*, \*Service\*, \*Manager\*, \*Controller\*.

### 3) Now you can run your application, jump to step 6 and check what is going on.

### 4) Configuring moskito-javaagent-config.json.
This configuration allows to pre-select working mode (PROFILING / LOG_ONLY)  — in log only mode all   class/methods  will be simply dumped into  info - log
in PROFILING mode - core  moskito functionality will  take a part.

a) Add monitoring sections @monitoringClassConfig ( Patterns  defines classes which will be weaver ( wrapped into monitoring aspect ))!
```json
        {
           "patterns": ["com.test.*"],
           "subsystem": "default",
           "category": "foo-bar"
        }
```
b) Extend default monitoring classes @monitoringDefaultClassConfig ( Patterns  defines classes which will be weaver by default ( wrapped into monitoring aspect ))!
```json
        {
           "patterns": [".*Service.*"],
           "subsystem": "default",
           "category": "service"
        }
```
c)	In case you want to connect  from  MoSKito Inspect - select   port  - 9451 - default and  enable  it  using  config properties:
```json
           "startMoskitoBackend": true,
           "moskitoBackendPort": 9451
```
   or system property:   
    
		export JAVA_OPTS=" $JAVA_OPTS -DmoskitoAgentPort=9451"
    
### 5) Logging configuration changes.

[moskito-aspect-config.json]  allow to provide  other   logger names.  By default  loggers will be - MoskitoDefault ( see logback.xml),
 Moskito1m, Moskito1h - etc….

NOTE :
					"attachDefaultStatLoggers": true  - enables logback logging for defaults stats
					"defaultMoskitoLoggerName": "",  -  defines default logger name  ( “MoskitoDefault” -  by  global default )
					"@loggers": [],  -  allow to   create  intervalName - logger name mapping
like :
```json
        {
           "@intervalLogger": {
             "defaultMoskitoLoggerName": "",
             "@loggers": [
                {
                    "intervalName" : "1s",
                    "loggerName" : "MoskitoOneSecondIntervalLogger"
                }],
             "attachDefaultStatLoggers": true
           }
        }
```

In app data - defaults for logging specified in logback.xml. In case If you want to rename  some moskito loggers.

### 6) Connection from inspect moskito application with UI.
* Run MoSKito inspect application.
* Navigate to producer section.
* Provide host (localhost for local connections), port (default 9451 or those which was configured in section 4.c ("moskitoBackendPort").
* Connect - and find your producers.

Enjoy…