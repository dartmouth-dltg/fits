<?xml version="1.0" ?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/">

	<xsl:variable name="version"><xsl:value-of select="/ffprobe/program_version/@version"/></xsl:variable>
	<xsl:variable name="name">FFmpeg</xsl:variable>

	<fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output">
		<xsl:attribute name="version">$version</xsl:attribute>

		<!-- Identification information. -->
		<identification>
			<identity>
				<xsl:attribute name="format">
					<xsl:choose>
						<xsl:when test="ffprobe/format/@format_long_name">
							<xsl:value-of select="ffprobe/format/@format_long_name" />
						</xsl:when>
						<xsl:otherwise>error</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>

				<xsl:attribute name="mimetype">
					<xsl:choose> 
						<xsl:when test="ffprobe/format/@format_name='mov,mp4,m4a,3gp,3g2,mj2'">
							<xsl:variable name="brand" select="ffprobe/format/tag[@key='major_brand']"/> 
							<xsl:choose>
								<xsl:when test="normalize-space($brand/@value)='qt'">video/quicktime</xsl:when>
								<xsl:when test="$brand/@value='mp42'">video/mp4</xsl:when>
								<xsl:otherwise>application/octet-stream</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>error</xsl:otherwise>
					</xsl:choose>
					<!-- else? -->
				</xsl:attribute>				
			</identity>
		</identification>


		<!-- File validitity. -->
		<filestatus>
			<xsl:choose>
				<xsl:when test="ffprobe/error">
					<well-formed toolname="$name" toolversion="$version">false</well-formed>
					<valid toolname="$name" toolversion="$version">false</valid>
					<error><xsl:value-of select="ffprobe/error"/></error>
				</xsl:when>
				<xsl:otherwise>
					<well-formed toolname="$name" toolversion="$version">true</well-formed>
					<valid toolname="$name" toolversion="$version">true</valid>
				</xsl:otherwise>
			</xsl:choose>
		</filestatus>

	</fits>
	</xsl:template>

</xsl:stylesheet>