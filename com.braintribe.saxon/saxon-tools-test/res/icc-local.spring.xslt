<?xml version="1.0" encoding="UTF-8"?>
<!-- Creates icc-fides.spring.xml based on icc-local.spring.xml and expected differences. -->
<xsl:stylesheet version="2.0" xmlns:b="http://www.springframework.org/schema/beans" xmlns:util="http://www.springframework.org/schema/util"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Output with indentation. -->
	<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" indent="yes"/>

	<!-- If there is no better match, just copy. -->
	<xsl:template match="node()|@*" priority="-1000">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!-- Set property/entry value to '- -changeme- -'. -->
	<xsl:template name="addTobeChangedValueAttribute">
		<xsl:attribute name="value"><xsl:value-of select="'--changeme--'"/></xsl:attribute>
	</xsl:template>

	<!-- Add a property with specified name and value. -->
	<xsl:template name="addPropertyWithValue">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:element name="property" namespace="http://www.springframework.org/schema/beans">
			<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- Add a property with the specified name and value 'changeme'. -->
	<xsl:template name="addTobeChangedProperty">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:call-template name="addPropertyWithValue">
			<xsl:with-param name="name" select="$name"/>
			<xsl:with-param name="value" select="'--changeme--'"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ****************************************************************** -->
	<!-- ****************************************************************** -->

	<!-- Transforms all JmsServerActiveMq class beans to JmsServerMq. -->
	<xsl:template match="b:beans/b:bean[@class='com.braintribe.transport.jms.server.JmsServerActiveMq']">
		<xsl:element name="bean" namespace="http://www.springframework.org/schema/beans">
			<xsl:apply-templates select="@id"/>
			<xsl:attribute name="class">com.braintribe.transport.jms.server.JmsServerMq</xsl:attribute>
			<xsl:call-template name="addTobeChangedProperty">
				<xsl:with-param name="name" select="'host'"/>
			</xsl:call-template>
			<xsl:call-template name="addTobeChangedProperty">
				<xsl:with-param name="name" select="'port'"/>
			</xsl:call-template>
			<xsl:call-template name="addTobeChangedProperty">
				<xsl:with-param name="name" select="'channel'"/>
			</xsl:call-template>
			<xsl:call-template name="addTobeChangedProperty">
				<xsl:with-param name="name" select="'queueManager'"/>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>

	<!-- Bean 'fileGateQueuePE1': copy entry msgMinSizeInKb, other values to be changed -->
	<xsl:template match="b:beans/b:bean[@id='fileGateQueuePE1']//b:entry[@key='msgMinSizeInKb']/@value">
		<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="b:beans/b:bean[@id='fileGateQueuePE1']//b:entry/@value" priority="-1">
		<xsl:call-template name="addTobeChangedValueAttribute"/>
	</xsl:template>

	<!-- Bean 'fileGateQueueInput': all entry values to be changed -->
	<xsl:template match="b:beans/b:bean[@id='fileGateQueueInput']//b:entry/@value">
		<xsl:call-template name="addTobeChangedValueAttribute"/>
	</xsl:template>

	<!-- Bean 'queueNames': all entry values to be changed -->
	<xsl:template match="b:beans/b:bean[@id='queueNames']//b:entry/@value">
		<xsl:call-template name="addTobeChangedValueAttribute"/>
	</xsl:template>

	<!-- Bean 'stageContextMap': some values are set, some are copied and some have to be changed. -->
	<!-- Entry values to set -->
	<xsl:template match="b:beans/b:bean[@id='stageContextMap']//b:entry[@key='pollerCronExpression']/@value">
		<xsl:attribute name="value">0 0/5 * * * ?</xsl:attribute>
	</xsl:template>
	<xsl:template match="b:beans/b:bean[@id='stageContextMap']//b:entry[@key='workaroundEnabled']/@value">
		<xsl:attribute name="value">false</xsl:attribute>
	</xsl:template>
	<!-- Entry values to copy -->
	<xsl:template
		match="b:beans/b:bean[@id='stageContextMap']//b:entry[@key='activateInputProcessDuplicateCheck' or @key='inputProcessDuplicateCheckDayRange' or @key='dayRange' or @key='intervalInMinutes' or @key='intervalInMinutes' or @key='smtpAuthentication' or @key='smtpStartTls' or @key='serverMustUnderstandSecurityHeaders' or @key='pain002SwiftService' or @key='pain002SwiftServiceStoreAndForward' or @key='customization']/@value">
		<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<!-- Entries values to be changed -->
	<xsl:template match="b:beans/b:bean[@id='stageContextMap']//b:entry/@value" priority="-1">
		<xsl:call-template name="addTobeChangedValueAttribute"/>
	</xsl:template>

	<!-- Bean: 'stageContext.dispatchWebServiceUrls' -->
	<xsl:template match="b:beans/util:list[@id='stageContext.dispatchWebServiceUrls']/b:value">
		<xsl:copy>
			<xsl:value-of select="'--changeme--'"/>
		</xsl:copy>
	</xsl:template>

	<!-- Bean 'payment.process.handler.dispatch.webserviceclient.sslfactory': all property values (except the ones explicitly 
		specified below) to be changed -->
	<xsl:template match="b:beans/b:bean[@id='payment.process.handler.dispatch.webserviceclient.sslfactory']/b:property/@value"
		priority="-1">
		<xsl:call-template name="addTobeChangedValueAttribute"/>
	</xsl:template>

	<!-- Bean: 'payment.process.handler.dispatch.webserviceclient.sslfactory' -->
	<xsl:template
		match="b:beans/b:bean[@id='payment.process.handler.dispatch.webserviceclient.sslfactory']/b:property[@name='securityProtocol']/@value">
		<xsl:copy>
			<xsl:value-of select="'SSL'"/>
		</xsl:copy>
	</xsl:template>

	<!-- -->
	<xsl:template
		match="b:beans/b:bean[@id='iccNamesapceIndex']//b:entry/@value|//util:map[@id='fileGateHeader.fileFormat_to_FormatVersion.urn_map']//b:entry/@value|//b:bean[@id='filegate.header.format.version.urn.file.formats']//b:entry/@value">
		<xsl:copy/>
	</xsl:template>

</xsl:stylesheet>
