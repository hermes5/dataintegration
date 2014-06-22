<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:hermes5="http://www.hermes.admin.ch/print/hermes5">
<xsl:template match="/hermes5:book">
  <html>
  <head>
  <meta charset='utf-8'/>
  </head>
  <body>
      <xsl:for-each select="hermes5:chapter">
         <h1><xsl:value-of select="hermes5:name"/></h1>
          
         <xsl:value-of select="hermes5:content" disable-output-escaping="yes"/>
          <xsl:for-each select="hermes5:section">
            <h2><xsl:value-of select="hermes5:name"/></h2>
            <xsl:value-of select="hermes5:content" disable-output-escaping="yes"/>
             <xsl:for-each select="hermes5:subsection">
            <h3><xsl:value-of select="hermes5:name"/></h3>
            <xsl:value-of select="hermes5:content" disable-output-escaping="yes"/>
             <xsl:for-each select="hermes5:subsubsection">
            <h4><xsl:value-of select="hermes5:name"/></h4>
            <xsl:value-of select="hermes5:content" disable-output-escaping="yes"/>
            </xsl:for-each>
            </xsl:for-each>
             
            </xsl:for-each>
          
      </xsl:for-each>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>