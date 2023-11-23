<#if !request.ide?? || request.ide != 'eclipse' || !request.sourceDirectory??>
	${template.ignore()}
</#if>
<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<classpath>
	<classpathentry kind="src" path="${request.sourceDirectory}"/>
<#list request.classPathEntries as cpe>
	<classpathentry kind="con" path="${cpe}"/>
</#list>
<#if request.buildSystem??>
	<#if request.buildSystem == 'bt-ant'>
	<classpathentry kind="con" path="Braintribe.ArtifactClasspathContainer"/>
	<#elseif request.buildSystem == 'maven'>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
	</#if>
</#if>
	<classpathentry kind="output" path="${request.outputDirectory}"/>
<#if request.builderOutputLibs??>
<#list request.builderOutputLibs as bol>
	<classpathentry exported="true" kind="lib" path="${bol}"/>
</#list>
</#if>
</classpath>
