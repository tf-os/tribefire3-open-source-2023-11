<#if request.buildSystem != 'bt-ant'>	
	${template.ignore()}	
</#if>	
<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<project xmlns:bt="antlib:com.braintribe.build.ant.tasks" name="group-build-script" default="transitive-build">
    <bt:import artifact="com.braintribe.devrock.ant:group-ant-script#1.0"/>
</project>
