<?xml version="1.0" encoding="UTF-8"?>
<!-- This XSLT removes whitespace and otherwise just copies the XML.
	(The rest of the formatting is done via Serializer configuration.) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*"/>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
