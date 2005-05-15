<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" media-type="text/html"
            doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"
            doctype-system="http://www.w3.org/TR/REC-html40/loose.dtd" />

<xsl:template match="/project">
<xsl:variable name="id"><xsl:value-of select="id"/></xsl:variable>
<xsl:variable name="name"><xsl:value-of select="name"/></xsl:variable>

<html>

<head>
   <title><xsl:value-of select="name"/> Downloads</title>
   <meta http-equiv="description">
      <xsl:attribute name="content"><xsl:value-of select="name"/> Downloads</xsl:attribute>
   </meta>
   <meta http-equiv="keywords">
      <xsl:attribute name="content">download <xsl:value-of select="keywords"/></xsl:attribute>
   </meta>
   <style type="text/css">
      p.copyright {
         font-size: smaller;
         color: #666666;
         margin-top: 50;
      }
   </style>
</head>

<body>
<div class="app">

<!--<h3>&nbsp;</h3>-->
<h2><xsl:value-of select="name"/> Downloads</h2>

<p>This page contains all the currently available downloads of <xsl:value-of select="name"/>.</p>

<p>
Note that <xsl:value-of select="name"/> requires <xsl:value-of select="platform/name"/> version
<xsl:value-of select="platform/version"/> or above to run.
</p>

<h3><xsl:value-of select="name"/><xsl:text> </xsl:text><xsl:value-of select="version"/></h3>

<h4>Binaries</h4>

<p>
The full release of <xsl:value-of select="name"/> contains the deployable JAR, source code, API
documentation and a template configuration file.
</p>
<p><b>Full Release:</b>
<a>
   <xsl:variable name="release-zip"><xsl:value-of select="id"/>-<xsl:value-of select="version"/>.zip</xsl:variable>
   <xsl:attribute name="href">release/<xsl:value-of select="version"/>/<xsl:value-of select="$release-zip"/></xsl:attribute>

   <!-- TODO (grlea) Generate the size somehow ? -->
   <xsl:value-of select="$release-zip"/></a> (79 K)
</p>
<br />

<p>
The Distributable JAR contains only the <xsl:value-of select="name"/> classes, the Licence and the README.
</p>
<p><b>Distributable JAR Only:</b>
<a>
   <xsl:variable name="distributable-jar"><xsl:value-of select="id"/>.jar</xsl:variable>
   <xsl:attribute name="href">release/<xsl:value-of select="version"/>/<xsl:value-of select="$distributable-jar"/></xsl:attribute>

   <!-- TODO (grlea) Generate the size somehow ? -->
   <xsl:value-of select="$distributable-jar"/></a> (20 K)
</p>
<br />


<h4>Readme</h4>
<p>The Readme contains important information about the latest release.</p>

<p>
<a href="release/README.txt" target="_blank">README.txt</a> (4 K)
</p>
<br />

<h4>Change Log</h4>
<p>
The Change Log contains details about what has changed in each version.<br />
You can check this file to see if it is worth your while downloading the latest version.
</p>

<p>
<a href="release/ChangeLog.txt" target="_blank">ChangeLog.txt</a> (1 K)
</p>
<br />

<h4>License</h4>
<p>
By downloading and using this software you are agreeing to be bound by its licence,
the Apache License 2.0.<br />
Please take the time to read, understand and comply with the licence.
</p>

<p>
<a href="release/LICENSE.txt" target="_blank">LICENSE.txt</a> (12 K)
</p>
<br />

<h3>Old Releases</h3>

<xsl:choose>
   <xsl:when test="previous-versions">
      <xsl:for-each select="previous-versions/version">

<h4><xsl:value-of select="$name"/><xsl:text> </xsl:text><xsl:value-of select="text()"/></h4>

<p><b>Full Release:</b>
<a>
   <xsl:attribute name="href">release/<xsl:value-of select="text()"/>/<xsl:value-of select="$id"/>-<xsl:value-of select="text()"/>.zip</xsl:attribute>
   <!-- TODO (grlea) Generate these sizes automatically as well? -->
   <xsl:value-of select="$id"/>-<xsl:value-of select="text()"/>.zip</a> (71 K)
</p>
<p><b>Distributable JAR Only:</b>
<a>
   <xsl:attribute name="href">release/<xsl:value-of select="text()"/>/<xsl:value-of select="$id"/>.jar</xsl:attribute>
   <!-- TODO (grlea) Generate these sizes automatically as well? -->
   <xsl:value-of select="$id"/>.jar</a> (18 K)
</p>

      </xsl:for-each>
   </xsl:when>
</xsl:choose>

<br />
<br />
<p class="copyright">Copyright (c) 2004-2005, Graham Lea. All rights reserved.</p>

</div>
</body>
</html>

</xsl:template>
</xsl:stylesheet>