<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no"/>

	<xsl:param name="someString" required="yes" />
	<xsl:param name="someInteger" required="yes" />
	<xsl:param name="someLong" required="yes" />
	<xsl:param name="someFloat" required="yes" />
	<xsl:param name="someDouble" required="yes" />
	<xsl:param name="someBigDecimal" required="yes" />
	<xsl:param name="someBoolean" required="yes" />
	<xsl:param name="someURI" required="yes" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/ParamTest/someString/text()">
		<xsl:value-of select="$someString"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someInteger/text()">
		<xsl:value-of select="$someInteger"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someLong/text()">
		<xsl:value-of select="$someLong"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someFloat/text()">
		<xsl:value-of select="$someFloat"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someDouble/text()">
		<xsl:value-of select="$someDouble"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someBigDecimal/text()">
		<xsl:value-of select="$someBigDecimal"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someBoolean/text()">
		<xsl:value-of select="$someBoolean"/>
	</xsl:template>
	
	<xsl:template match="/ParamTest/someURI/text()">
		<xsl:value-of select="$someURI"/>
	</xsl:template>

</xsl:stylesheet>
