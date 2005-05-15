<?xml version="1.0" encoding="UTF-8"?>

<!--
$Id: quick-ant.xsl,v 1.1 2005-05-15 16:03:41 grlea Exp $
Copyright (c) 2004-2005 Graham Lea. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" version="1.0" indent="yes" media-type="text/xml"/>
   <xsl:param name="cvsRoot"/>

   <xsl:template match="/project">

      <project default="CreateRelease">
         <xsl:attribute name="name"><xsl:value-of select="id"/></xsl:attribute>


         <!-- Properties -->

         <xsl:comment> Properties </xsl:comment>

         <property name="product.id">
            <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
         </property>

         <property name="product.name">
            <xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute>
         </property>

         <property name="product.website">
            <xsl:attribute name="value"><xsl:value-of select="website"/></xsl:attribute>
         </property>

         <property name="product.copyrightHolder">
            <xsl:attribute name="value"><xsl:value-of select="copyright-holder"/></xsl:attribute>
         </property>

         <property name="product.creationYear">
            <xsl:attribute name="value"><xsl:value-of select="creation-year"/></xsl:attribute>
         </property>

         <property name="product.currentYear">
            <xsl:attribute name="value"><xsl:value-of select="current-year"/></xsl:attribute>
         </property>

         <property name="product.version">
            <xsl:attribute name="value"><xsl:value-of select="version"/></xsl:attribute>
         </property>

         <property name="source-directory">
            <xsl:attribute name="value"><xsl:value-of select="source-dir"/></xsl:attribute>
         </property>

         <property name="manifest-directory">
            <xsl:attribute name="value"><xsl:value-of select="manifest-dir"/></xsl:attribute>
         </property>

         <property name="lib-directory">
            <xsl:attribute name="value"><xsl:value-of select="lib-dir"/></xsl:attribute>
         </property>

         <property name="build-directory" value="build"/>

         <property name="resources-directory">
            <xsl:attribute name="value">${build-directory}/RESOURCES</xsl:attribute>
         </property>

         <property name="release-build-base-directory">
            <xsl:attribute name="value">${build-directory}/RELEASE</xsl:attribute>
         </property>

         <property name="release-build-directory">
            <xsl:attribute name="value">${release-build-base-directory}/${product.id}-${product.version}</xsl:attribute>
         </property>

         <property name="source-zip">
            <xsl:attribute name="value">${release-build-directory}/${product.id}-src.zip</xsl:attribute>
         </property>

         <property name="javadoc-directory">
            <xsl:attribute name="value">${release-build-directory}/doc/api</xsl:attribute>
         </property>

         <property name="release-lib-directory">
            <xsl:attribute name="value">${release-build-directory}/lib</xsl:attribute>
         </property>

         <property name="release-store-directory">
            <xsl:attribute name="value">release/${product.version}</xsl:attribute>
         </property>

         <property name="release-zip">
            <xsl:attribute name="value">${release-store-directory}/${product.id}-${product.version}.zip</xsl:attribute>
         </property>

         <property name="release-base-directory">
            <xsl:attribute name="value">www/release</xsl:attribute>
         </property>

         <property name="release-directory">
            <xsl:attribute name="value">${release-base-directory}/${product.version}</xsl:attribute>
         </property>


         <xsl:comment> Source directories </xsl:comment>

         <xsl:for-each select="component">
            <property>
               <xsl:attribute name="name">source.<xsl:value-of select="id"/></xsl:attribute>
               <xsl:attribute name="value">${source-directory}/<xsl:value-of select="source-dir"/></xsl:attribute>
            </property>
         </xsl:for-each>


         <xsl:comment> Build directories </xsl:comment>

         <xsl:for-each select="component">
            <property>
               <xsl:attribute name="name">build.<xsl:value-of select="id"/></xsl:attribute>
               <xsl:attribute name="value">${build-directory}/<xsl:value-of select="id"/></xsl:attribute>
            </property>
         </xsl:for-each>


         <xsl:comment> Manifest files </xsl:comment>

         <xsl:for-each select="component">
            <xsl:if test="manifest">
               <property>
                  <xsl:attribute name="name">manifest.<xsl:value-of select="id"/></xsl:attribute>
                  <xsl:attribute name="value">${manifest-directory}/<xsl:value-of select="manifest"/></xsl:attribute>
               </property>
            </xsl:if>
         </xsl:for-each>


         <xsl:comment> Jar files </xsl:comment>

         <xsl:choose>
            <xsl:when test="count(component) = 1">
               <property>
                  <xsl:attribute name="name">jar.<xsl:value-of select="component/id"/></xsl:attribute>
                  <xsl:attribute name="value">${release-store-directory}/${product.id}.jar</xsl:attribute>
               </property>
            </xsl:when>
            <xsl:otherwise>
               <xsl:for-each select="component">
                  <property>
                     <xsl:attribute name="name">jar.<xsl:value-of select="id"/></xsl:attribute>
                     <xsl:attribute name="value">${release-build-directory}/${product.id}-<xsl:value-of select="id"/>.jar</xsl:attribute>
                  </property>
               </xsl:for-each>
            </xsl:otherwise>
         </xsl:choose>


         <!-- Classpaths -->

         <xsl:comment> Classpaths </xsl:comment>

         <xsl:for-each select="component">

            <path>
               <xsl:attribute name="id">classpath.<xsl:value-of select="id"/></xsl:attribute>

               <xsl:if test="count(dependencies/component-ref) &gt; 0">
                  <xsl:for-each select="dependencies/component-ref">
                     <path>
                        <xsl:attribute name="refid">classpath.<xsl:value-of select="text()"/></xsl:attribute>
                     </path>
                  </xsl:for-each>
                  <dirset dir=".">
                     <xsl:for-each select="dependencies/component-ref">
                        <include>
                           <xsl:attribute name="name">${build.<xsl:value-of select="text()"/>}</xsl:attribute>
                        </include>
                     </xsl:for-each>
                  </dirset>
               </xsl:if>

               <xsl:if test="dependencies/library">
                  <fileset>
                     <xsl:attribute name="dir">${lib-directory}</xsl:attribute>
                     <xsl:for-each select="dependencies/library">
                        <include>
                           <xsl:attribute name="name"><xsl:value-of select="text()"/></xsl:attribute>
                        </include>
                     </xsl:for-each>
                  </fileset>
               </xsl:if>

            </path>

         </xsl:for-each>

         <path id="classpath.ALL">
            <xsl:for-each select="component">
               <path>
                  <xsl:attribute name="refid">classpath.<xsl:value-of select="id"/></xsl:attribute>
               </path>
            </xsl:for-each>
         </path>


         <!-- Targets -->

         <xsl:comment> TARGETS </xsl:comment>

         <target name="Clean">
            <delete>
               <xsl:attribute name="dir">${build-directory}</xsl:attribute>
            </delete>
            <delete>
               <xsl:attribute name="dir">${release-build-directory}</xsl:attribute>
            </delete>
            <delete>
               <xsl:attribute name="dir">${release-store-directory}</xsl:attribute>
            </delete>
         </target>


         <target name="Init">
            <mkdir>
               <xsl:attribute name="dir">${build-directory}</xsl:attribute>
            </mkdir>
            <mkdir>
               <xsl:attribute name="dir">${release-build-directory}</xsl:attribute>
            </mkdir>
            <mkdir>
               <xsl:attribute name="dir">${release-store-directory}</xsl:attribute>
            </mkdir>
         </target>


         <target name="Release" depends="ConfirmRelease,Init,CreateRelease">

            <!-- Copy release files to the deployment directory (www/release/<version>) -->
            <copy>
               <xsl:attribute name="todir">${release-directory}</xsl:attribute>
               <fileset includes="**">
                  <xsl:attribute name="dir">${release-store-directory}</xsl:attribute>
               </fileset>
            </copy>

            <!-- Add the deployment directory (www/release/<version>) -->
            <cvs command="add" failonerror="true">
               <xsl:attribute name="command">add ${release-directory}</xsl:attribute>
            </cvs>

            <!-- Add the release files -->
            <cvs failonerror="true">
               <xsl:attribute name="command">add ${release-directory}/*</xsl:attribute>
            </cvs>

            <!-- Commit the release files -->
<!--            <cvs failonerror="true">-->
<!--               <xsl:attribute name="command">commit ${release-directory}/*</xsl:attribute>-->
<!--            </cvs>-->

            <!-- Copy published resources to the deployment base directory (www/release) -->
            <copy>
               <xsl:attribute name="todir">${release-base-directory}</xsl:attribute>
               <fileset>
                  <xsl:for-each select="resources/resource[@publish='true']">
                     <include><xsl:value-of select="text()"/></include>
                  </xsl:for-each>
               </fileset>
            </copy>

            <!-- Add & Commit the published resources -->
            <cvs>
               <xsl:attribute name="command">add ${release-directory}/*</xsl:attribute>
            </cvs>

            <cvs failonerror="true">
               <xsl:attribute name="command">commit ${release-directory}/*</xsl:attribute>
            </cvs>

<!--            <xsl:for-each select="resources/resource[@publish='true']">-->
<!--               <cvs failonerror="true">-->
<!--                   TODO (grlea) Will this commit subdirectories?-->
<!--                  <xsl:attribute name="command">commit ${release-base-directory}/<xsl:value-of select="text()"/></xsl:attribute>-->
<!--               </cvs>-->
<!--            </xsl:for-each>-->

            <!-- TODO (grlea) Create a tag on the repository -->

         </target>


         <target name="ConfirmRelease">
            <property file="confirm-release.properties"/>
            <condition property="do-release">
               <and>
                  <isset property="confirm-release"/>
                  <equals arg1="${confirm-release}" arg2="true" casesensitive="false" trim="true" />
               </and>
            </condition>
            <fail unless="do-release" message="Release aborted. To complete a release, modify 'confirm-release.properties'."/>

            <!-- TODO (grlea) Check the release files don't already exist -->

         </target>

         <target name="CreateRelease" description="Creates the release containing all components" depends="Clean,CreateResources,Build,CreateSourceZip,CreateJavadoc">

            <xsl:comment> Copy Jar files into release build directory </xsl:comment>
            <copy>
               <xsl:attribute name="todir">${release-build-directory}</xsl:attribute>
               <fileset includes="**">
                  <xsl:attribute name="dir">${release-store-directory}</xsl:attribute>
               </fileset>
            </copy>

            <xsl:comment> Copy resources into release build directory </xsl:comment>
            <copy>
               <xsl:attribute name="todir">${release-build-directory}</xsl:attribute>
               <fileset includes="**">
                  <xsl:attribute name="dir">${resources-directory}</xsl:attribute>
               </fileset>
            </copy>

            <xsl:comment> Copy extra release files </xsl:comment>
            <copy flatten="true">
               <xsl:attribute name="todir">${release-build-directory}</xsl:attribute>
               <fileset dir=".">
                  <xsl:for-each select="extra-release-files/include">
                     <xsl:copy-of select="current()" />
                  </xsl:for-each>
               </fileset>
            </copy>

            <xsl:if test="component/dependencies/library">
               <xsl:comment> Copy the dependencies </xsl:comment>
               <copy>
                  <xsl:attribute name="todir">${release-lib-directory}</xsl:attribute>
                  <fileset>
                     <xsl:attribute name="dir">${lib-directory}</xsl:attribute>
                     <xsl:for-each select="component/dependencies/library">
                        <include>
                           <xsl:attribute name="name"><xsl:value-of select="text()"/></xsl:attribute>
                        </include>
                     </xsl:for-each>
                  </fileset>
               </copy>
            </xsl:if>

            <xsl:comment> Create the release zip </xsl:comment>
            <zip>
               <xsl:attribute name="zipfile">${release-zip}</xsl:attribute>
               <xsl:attribute name="basedir">${release-build-base-directory}</xsl:attribute>
               <include name="**"/>
            </zip>

         </target>

         <target name="CreateSourceZip">
            <zip>
               <xsl:attribute name="zipfile">${source-zip}</xsl:attribute>
               <xsl:attribute name="basedir">.</xsl:attribute>

               <xsl:for-each select="extra-sources/include">
                  <xsl:copy-of select="current()" />
               </xsl:for-each>

               <xsl:for-each select="component">
                  <include>
                     <xsl:attribute name="name">${source.<xsl:value-of select="id"/>}/<xsl:value-of select="source-files"/></xsl:attribute>
                  </include>
               </xsl:for-each>
            </zip>
         </target>

         <target name="CreateJavadoc" description="Create javadoc Documentation" depends="Init">
            <javadoc destdir="${javadoc-directory}"
                     classpathref="classpath.ALL"
                     access="protected" Author="true" Use="true" Version="true"
                     nodeprecatedlist="true" notree="false" nohelp="true" failonerror="true">

               <xsl:attribute name="destdir">${javadoc-directory}</xsl:attribute>
               <xsl:attribute name="packagenames"><xsl:value-of select="javadoc/package-names"/></xsl:attribute>
               <xsl:attribute name="WindowTitle">${product.name} ${product.version}</xsl:attribute>

               <xsl:for-each select="javadoc/source-link">
                  <link offline="true">
                     <xsl:attribute name="href"><xsl:value-of select="remote"/></xsl:attribute>
                     <xsl:if test="local">
                        <xsl:attribute name="packagelistloc"><xsl:value-of select="local"/></xsl:attribute>
                     </xsl:if>
                  </link>
               </xsl:for-each>

               <sourcepath>
                  <dirset dir=".">
                     <xsl:for-each select="component">
                        <include>
                           <xsl:attribute name="name">${source.<xsl:value-of select="id"/>}</xsl:attribute>
                        </include>
                     </xsl:for-each>
                  </dirset>
               </sourcepath>

               <xsl:for-each select="javadoc/group">
                  <xsl:copy-of select="current()"/>
               </xsl:for-each>

               <header>
                  ${product.name} ${product.version} &lt;br /&gt;
                  &lt;a href="${product.website}" target="_blank"&gt;${product.website} &lt;/a&gt;&lt;br /&gt;&lt;br /&gt;
               </header>

               <bottom>
<!--                  Copyright (c) ${product.creationYear}-<xsl:value-of select="year-from-date(current-date())"/>  ${product.copyrightHolder}. &lt;br /&gt;-->
                  Copyright (c) ${product.creationYear}-2005  ${product.copyrightHolder}. &lt;br /&gt;
                  All rights reserved.
               </bottom>
            </javadoc>
         </target>

         <target name="Build" description="Build and package all components">
            <xsl:attribute name="depends">Init<xsl:for-each select="component">,Build-<xsl:value-of select="id"/></xsl:for-each></xsl:attribute>
         </target>

         <target name="Compile" description="Compile all components">
            <xsl:attribute name="depends">Init<xsl:for-each select="component">,Compile-<xsl:value-of select="id"/></xsl:for-each></xsl:attribute>
         </target>


         <xsl:comment> Build Targets </xsl:comment>

         <xsl:for-each select="component">
            <target>
               <xsl:attribute name="name">Build-<xsl:value-of select="id"/></xsl:attribute>
               <xsl:attribute name="description">Build and package the <xsl:value-of select="name"/> component</xsl:attribute>
               <xsl:attribute name="depends">Compile-<xsl:value-of select="id"/>,CopyResources-<xsl:value-of select="id"/></xsl:attribute>

               <jar includes="**">
                  <xsl:attribute name="jarfile">${jar.<xsl:value-of select="id"/>}</xsl:attribute>
                  <xsl:attribute name="basedir">${build.<xsl:value-of select="id"/>}</xsl:attribute>
                  <xsl:if test="manifest">
                     <xsl:attribute name="manifest">${manifest.<xsl:value-of select="id"/>}</xsl:attribute>
                  </xsl:if>
               </jar>
            </target>
         </xsl:for-each>


         <xsl:comment> Compile Targets </xsl:comment>

         <xsl:for-each select="component">
            <target>
               <xsl:attribute name="name">Compile-<xsl:value-of select="id"/></xsl:attribute>
               <xsl:attribute name="description">Compile the <xsl:value-of select="name"/> component</xsl:attribute>
               <xsl:attribute name="depends">Init<xsl:for-each select="dependencies/component-ref">,Compile-<xsl:value-of select="text()"/></xsl:for-each></xsl:attribute>

               <mkdir>
                  <xsl:attribute name="dir">${build.<xsl:value-of select="id"/>}</xsl:attribute>
               </mkdir>

               <javac>
                  <xsl:attribute name="srcdir">${source-directory}/<xsl:value-of select="source-dir"/></xsl:attribute>
                  <xsl:attribute name="classpathref">classpath.<xsl:value-of select="id"/></xsl:attribute>
                  <xsl:attribute name="destdir">${build.<xsl:value-of select="id"/>}</xsl:attribute>
                  <xsl:attribute name="includes">
                     <xsl:choose>
                        <xsl:when test="source-files"><xsl:value-of select="source-files"/></xsl:when>
                        <xsl:otherwise>**/*.java</xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>
               </javac>

            </target>

         </xsl:for-each>


         <xsl:comment> CopyResources Targets </xsl:comment>

         <target name="CreateResources">

            <mkdir>
               <xsl:attribute name="dir">${resources-directory}</xsl:attribute>
            </mkdir>

            <xsl:if test="resources">
               <copy flatten="true">
                  <xsl:attribute name="todir">${resources-directory}</xsl:attribute>

                  <fileset dir=".">
                     <xsl:for-each select="resources/resource">
                        <include>
                           <xsl:attribute name="name"><xsl:value-of select="text()"/></xsl:attribute>
                        </include>
                     </xsl:for-each>
                  </fileset>
                  <filterset>
                     <filter token="VERSION">
                        <xsl:attribute name="value">${product.version}</xsl:attribute>
                     </filter>
                   </filterset>
               </copy>
            </xsl:if>
         </target>

         <xsl:for-each select="component">
            <target depends="CreateResources">
               <xsl:attribute name="name">CopyResources-<xsl:value-of select="id"/></xsl:attribute>
               <xsl:attribute name="description">Copy the resources for the <xsl:value-of select="name"/> component</xsl:attribute>

               <property>
                  <xsl:attribute name="name">resources.<xsl:value-of select="id"/></xsl:attribute>
                  <xsl:attribute name="value">${build.<xsl:value-of select="id"/>}/meta-inf/${product.id}</xsl:attribute>
               </property>

               <mkdir>
                  <xsl:attribute name="dir">${resources.<xsl:value-of select="id"/>}</xsl:attribute>
               </mkdir>

               <copy>
                  <xsl:attribute name="todir">${resources.<xsl:value-of select="id"/>}</xsl:attribute>
                  <fileset includes="**">
                     <xsl:attribute name="dir">${resources-directory}</xsl:attribute>
                  </fileset>
               </copy>

            </target>

         </xsl:for-each>

      </project>

   </xsl:template>

</xsl:stylesheet >
