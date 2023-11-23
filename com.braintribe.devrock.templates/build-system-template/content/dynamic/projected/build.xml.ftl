<#if !request.buildSystem?? || request.buildSystem != 'bt-ant'>
	${template.ignore()}
</#if>
<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<project xmlns:artifact="antlib:org.apache.maven.artifact.ant" xmlns:bt="antlib:com.braintribe.build.ant.tasks" basedir="." default="install">
	<bt:import artifact="com.braintribe.devrock.ant:${request.artifactType}-ant-script#1.0" useCase="DEVROCK"/>
<#if request.artifactType == 'common' && request.resources?size != 0>

	<target name="install">
        <bt:install file="pom.xml">
            <pom file="pom.xml" id="project"/>
	<#list request.resources as resource>
            <attach file="${resource}" type="${support.getFileNameWithoutExtension(resource)}:${support.getFileNameExtension(resource)}"/>
	</#list>
        </bt:install>
    </target>
</#if>
</project>
