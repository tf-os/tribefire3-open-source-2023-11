<#if !request.buildSystem?? || (request.buildSystem != 'bt-ant' && request.buildSystem != 'maven')>
	${template.ignore()}
</#if>
<#assign version = support.createVersionFromString(request.version)>
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
<#if request.hasParent>
	<parent>
		<groupId>${request.groupId}</groupId>
		<artifactId>${request.parentArtifactId}</artifactId>
		<version>[${version.getMajor()}.${version.getMinor()},${version.getMajor()}.${version.getMinor() + 1})</version>
	</parent>
<#else>
	<groupId>${request.groupId}</groupId>
</#if>
	<artifactId>${request.artifactId}</artifactId>
	<version>${version.getMajor()}.${version.getMinor()}.<#if version.getRevision()??>${version.getRevision()}<#if version.getQualifier()??>-${version.getQualifier()}</#if><#else>1-pc</#if></version>
<#if request.packaging??>
	<packaging>${request.packaging}</packaging>
</#if>
	<properties>
<#list request.properties as property>
		<${property.name}>${property.value}</${property.name}>
</#list>
	</properties>
<#if request.buildSystem == 'maven' && request.resources?size != 0>
	<build>
   		<resources>
     		<resource>
       			<directory>${r'${basedir}'}</directory>
       			<includes>
   	<#list request.resources as resource>
   					<include>${resource}</include>
   	</#list>
       			</includes>
    		</resource>
   		</resources>
 	</build>
</#if>
<#if request.dependencies?size != 0>
	<dependencies>
	<#list request.dependencies as dependency>
		<dependency> 
			<groupId>${dependency.groupId}</groupId>
			<artifactId>${dependency.artifactId}</artifactId>
		<#if dependency.version??>
			<version>${dependency.version}</version>
		<#elseif request.hasParent>
			<version>${r'${V.'}${dependency.groupId}${r'}'}</version>
		</#if>
		<#if dependency.scope??>
			<scope>${dependency.scope}</scope>
		</#if>
		<#if dependency.classifier??>
			<classifier>${dependency.classifier}</classifier>
		</#if>
		<#if dependency.getType()??>
			<type>${dependency.getType()}</type>
		</#if>
		<#list dependency.tags as tag>
			<?tag ${tag}?>
		</#list>
		<#if dependency.exclusions?size != 0>
			<exclusions>
			<#list dependency.exclusions as e>
				<exclusion>
					<groupId>${e.groupId}</groupId>
					<artifactId>${e.artifactId}</artifactId>
				</exclusion>
			</#list>
			</exclusions>
		</#if>
		</dependency>
	</#list>
	</dependencies>
<#elseif request.artifactType != 'parent'>
	<dependencies>
		<!-- no dependencies -->
	</dependencies>
</#if>
<#if request.managedDependencies?size != 0>
	<dependencyManagement>
		<dependencies>
	<#list request.managedDependencies as dependency>
			<dependency> 
				<groupId>${dependency.groupId}</groupId>
				<artifactId>${dependency.artifactId}</artifactId>
		<#if dependency.version??>
				<version>${dependency.version}</version>
		<#elseif request.hasParent>
				<version>${r'${V.'}${dependency.groupId}${r'}'}</version>
		</#if>
		<#if dependency.scope??>
				<scope>${dependency.scope}</scope>
		</#if>
		<#if dependency.classifier??>
				<classifier>${dependency.classifier}</classifier>
		</#if>
		<#if dependency.getType()??>
				<type>${dependency.getType()}</type>
		</#if>
		<#list dependency.tags as tag>
				<?tag ${tag}?>
		</#list>
		<#if dependency.exclusions?size != 0>
				<exclusions>
			<#list dependency.exclusions as e>
					<exclusion>
						<groupId>${e.groupId}</groupId>
						<artifactId>${e.artifactId}</artifactId>
					</exclusion>
			</#list>
				</exclusions>
		</#if>
			</dependency>
	</#list>
		</dependencies>
	</dependencyManagement>
</#if>
</project>
