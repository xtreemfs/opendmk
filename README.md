XtreemFS OpenDMK 1.0-b02-SNAPSHOT Build
=======================================

This project aims at providing `jdmkrt`, `jdmktk` and `jmxremote_optional` in a Maven structure. It is built from the 1.0-b02 Sources of [OpenDMK](https://opendmk.java.net/) using Maven 3.1.1 and Oracle JDK 1.5.0_13-b05, because the [binary plug](https://opendmk.java.net/download/index.html#BinaryComponents) (which this project uses) is built using this version as well.

The SNAPSHOT build is available at this repository's [gh-pages](https://github.com/xtreemfs/opendmk/tree/gh-pages) and can be used as follows:

```XML
<repositories>
  <repository>
    <id>xtreemfs-opendmk</id>
    <url>https://raw.githubusercontent.com/xtreemfs/opendmk/gh-pages/</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.xtreemfs.opendmk</groupId>
    <artifactId>jdmkrt</artifactId>
    <version>1.0-b02-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.xtreemfs.opendmk</groupId>
    <artifactId>jdmktk</artifactId>
    <version>1.0-b02-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.xtreemfs.opendmk</groupId>
    <artifactId>jmxremote_optional</artifactId>
    <version>1.0-b02-SNAPSHOT</version>
  </dependency>
</dependencies>
```

OpenDMK comes with a dual license ([GPL](https://opendmk.java.net/legal_notices/LICENSE_GPL.txt) and [CDDL](https://opendmk.java.net/legal_notices/LICENSE_CDDL.txt)) and this project uses the same dual licensing scheme. This project uses OpenDMK's binary plug which comes with a seperate [binary license](https://opendmk.java.net/legal_notices/LICENSE_BINARY.txt).

[![Build Status](https://travis-ci.org/xtreemfs/opendmk.svg?branch=master)](https://travis-ci.org/xtreemfs/opendmk)
