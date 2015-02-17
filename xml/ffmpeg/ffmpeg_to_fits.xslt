<?xml version="1.0" ?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/">

	<xsl:variable name="version"><xsl:value-of select="/ffprobe/program_version/@version"/></xsl:variable>
	<xsl:variable name="name">FFmpeg</xsl:variable>

	<fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output">

		<!-- Identification information. -->
		<identification>
			<identity>

				<xsl:variable name="format-name" select="ffprobe/format/@format_name"/>

				<!-- Format name is that of MOV and MP4 files -->
				<xsl:choose> 
					<xsl:when test="$format-name='mov,mp4,m4a,3gp,3g2,mj2'">
						<xsl:variable name="brand" select="ffprobe/format/tag[@key='major_brand']"/> 
						<xsl:choose>
							<xsl:when test="$brand/@value='qt  '">
								<xsl:attribute name="format">ISO Media, Apple QuickTime movie</xsl:attribute>
								<xsl:attribute name="mimetype">video/quicktime</xsl:attribute>
							</xsl:when>
							<xsl:when test="$brand/@value='mp42'">
								<xsl:attribute name="format">ISO Media, MPEG v4 system, version 2</xsl:attribute>
								<xsl:attribute name="mimetype">video/mp4</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="format"><xsl:value-of select="$format-name"/></xsl:attribute>
								<xsl:attribute name="mimetype">application/octet-stream</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					
					<!-- Format name is set but is not of any of the types above -->
					<xsl:when test="$format-name">
						<xsl:attribute name="format"><xsl:value-of select="$format-name"/></xsl:attribute>
						<xsl:attribute name="mimetype">application/octet-stream</xsl:attribute>
					</xsl:when>
					
					<!-- There is no format name, return attributes with error values. -->
					<xsl:otherwise>
						<xsl:attribute name="format">error</xsl:attribute>
						<xsl:attribute name="mimetype">error</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>

			</identity>
		</identification>


		<!-- File validitity. -->
		<filestatus>
			<xsl:choose>
				<xsl:when test="ffprobe/error">
					<well-formed toolname="$name" toolversion="$version">false</well-formed>
					<valid toolname="$name" toolversion="$version">false</valid>
					<message><xsl:value-of select="ffprobe/error/@string"/></message>
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