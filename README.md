XtreemFS OpenDMK 1.0-b03-SNAPSHOT Build
=======================================

This project aims at providing `jdmkrt`, `jdmktk` and `jmxremote_optional` in a Maven structure. It is built from the 1.0-b03 Sources of [OpenDMK](https://opendmk.java.net/) using Maven 3.6.3 and Open JDK 14.0.1 with 1.8 bytecode.

The SNAPSHOT build is available at this repository's [gh-pages](https://github.com/xtreemfs/opendmk/tree/gh-pages) and can be used as follows:

In your `$HOME/.m2/settings.xml` add:
```XML
<settings>
  <profiles>

    <!-- more profiles -->

    <profile>
      <id>opendmk-dev</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>http://repo.maven.apache.org/maven2</url>
        </repository>
  
        <repository>
          <id>xtreemfs-opendmk</id>
          <url>https://xtreemfs.github.io/opendmk</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  
  <!-- more profiles -->

  </profiles>
</settings>
````

In your `pom.xml` add:
```XML
<project>

  <!-- more project configuration -->
  
  <dependencies>
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>jdmkrt</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <!-- jdmkrt bundles the following artifacts -->
    <!--
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>core</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>core-rmic</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>snmp_manager</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>snmp_agent</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>binary-plug</artifactId>
      <version>1.0-b03</version>
    </dependency>
    -->
    
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>jdmktk</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <!-- jdmktk bundles the following artifact -->
    <!--
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>toolkit</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    -->
    
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>jmxremote_optional</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    <!-- jmxremote_optional bundles the following artifact -->
    <!--
    <dependency>
      <groupId>org.xtreemfs.opendmk</groupId>
      <artifactId>jmx_optional</artifactId>
      <version>1.0-b03-SNAPSHOT</version>
    </dependency>
    -->
  </dependencies>
  
  <!-- more project configuration -->

</project>
```

And build your project like so:
```Bash
  mvn install -Popendmk-dev
```

OpenDMK comes with a dual license ([GPL](https://opendmk.java.net/legal_notices/LICENSE_GPL.txt) and [CDDL](https://opendmk.java.net/legal_notices/LICENSE_CDDL.txt)) and this project uses the same dual licensing scheme. This project uses OpenDMK's binary plug which comes with a seperate [binary license](https://opendmk.java.net/legal_notices/LICENSE_BINARY.txt).

[![Build Status](https://travis-ci.org/xtreemfs/opendmk.svg?branch=master)](https://travis-ci.org/xtreemfs/opendmk)
