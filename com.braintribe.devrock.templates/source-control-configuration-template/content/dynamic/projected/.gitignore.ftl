<#if !request.sourceControl?? || request.sourceControl != 'git'>
	${template.ignore()}
</#if>
<#list request.ignoredFiles as if>
${if}
</#list>
<#if request.buildSystem??>
	<#if request.buildSystem == 'bt-ant'>
/dist
/build
	<#elseif request.buildSystem == 'maven'>
/target
	</#if>
</#if>
