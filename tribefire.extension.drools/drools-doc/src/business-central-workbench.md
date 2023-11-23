
# Installation

## Download
|Component|Meaning|URL|
|---|---|---|
|Wildfly|J2EE Application Server|https://download.jboss.org/wildfly/19.0.0.Final/wildfly-19.0.0.Final.zip|
|Business Central Workbench|Visual Rule Designer|https://download.jboss.org/drools/release/7.44.0.Final/business-central-7.44.0.Final-wildfly19.war|
|KieServer|Drools Runtime|https://repo1.maven.org/maven2/org/kie/server/kie-server/7.44.0.Final/kie-server-7.44.0.Final-ee8.war|

## Install Wildfly

Unpack contents of `wildfly-19.0.0.Final.zip` to a folder named `wildfly-19.0.0.Final`.

## Integrate Drools Components in Wildfly

* Copy `kie-server-7.44.0.Final-ee8.war` to `wildfly-19.0.0.Final/standalone/deployments/kie-server.war`  
* Copy `business-central-7.44.0.Final-wildfly19.war` to `wildfly-19.0.0.Final/standalone/deployments/kie-wb.war`  
* Install auth information  
  * Change directory to `wildfly-19.0.0.Final/bin`  
  * Windows
    * `add-user -a -u kieserver -p kieserver1! -g kie-server`
    * `add-user -a -u workbench -p workbench! -g admin,kie-server`
  * Unix  
    * `./add-user.sh -a -u kieserver -p kieserver1! -g kie-server`  
    * `./add-user.sh -a -u workbench -p workbench! -g admin,kie-server`  

# Starting The Server

Change directory to `wildfly-19.0.0.Final/bin`  

## Windows
`standalone --server-config=standalone-full.xml -Dorg.kie.server.id=wildfly-kieserver -Dorg.kie.server.location=http://localhost:8080/kie-server/services/rest/server -Dorg.kie.server.controller=http://localhost:8080/kie-wb/rest/controller`

## Unix
`./standalone.sh --server-config=standalone-full.xml -Dorg.kie.server.id=wildfly-kieserver -Dorg.kie.server.location=http://localhost:8080/kie-server/services/rest/server -Dorg.kie.server.controller=http://localhost:8080/kie-wb/rest/controller`