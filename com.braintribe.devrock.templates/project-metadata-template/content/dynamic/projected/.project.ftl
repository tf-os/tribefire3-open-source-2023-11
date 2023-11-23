<#if !request.ide??  || request.ide != 'eclipse'>
	${template.ignore()}
</#if>
<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<projectDescription>
	<name>${request.projectName}</name>
<#if request.sourceDirectory??>
	<buildSpec>
	<#list request.builders as builder>
		<buildCommand>
			<name>${builder}</name>
			<arguments/>
		</buildCommand>
	</#list>
	<#if request.buildSystem?? && request.buildSystem == 'maven'>
        <buildCommand>
			<name>org.eclipse.m2e.core.maven2Builder</name>
			<arguments/>
		</buildCommand>
	</#if>
    </buildSpec>
    <natures>
	<#list request.natures as nature>
		<nature>${nature}</nature>
	</#list>
	<#if request.buildSystem?? && request.buildSystem == 'maven'>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
	</#if>
    </natures>
</#if>
</projectDescription>
