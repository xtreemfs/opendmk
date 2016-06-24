<center>

# Project OpenDMK - Open Source Project for the Java<sup><font size="-2">TM</font></sup> Dynamic Management Kit

</center>

<center>**Build opendmk-1.0-b02**</center>

* * *

## Description

[Project OpenDMK](http://opendmk.dev.java.net/) is an open source release of version 5.1 of the **[Java Dynamic Management Kit](http://java.sun.com/products/jdmk/index.jsp "Java Dynamic Management Kit Home Page") (Java DMK)**.

The majority of Project OpenDMK code is being distributed under the terms of the **[license](LEGAL_NOTICES/license.txt)** present in the LEGAL_NOTICES folder. Some binary components listed on the [Project OpenDMK download page](http://opendmk.dev.java.net/download/index.html#BinaryComponents) are covered under the [Binary License for Project OpenDMK](http://opendmk.dev.java.net/legal_notices/LICENSE_BINARY.txt), and must be downloaded separetely from the [Project OpenDMK download page](http://opendmk.dev.java.net//download).  
Note that the Full Binary Bundle also incorporates classes from the Project OpenDMK binary plug and is therefore submitted to the terms of the [Project OpenDMK binary license](http://opendmk.dev.java.net/legal_notices/LICENSE_BINARY.txt).

The <a name="http://opendmk.dev.java.net/download/index.html#BinaryComponents">binary components</a> are contained in self extracting jar files. To expand a self extracting jar file simply type

<pre>    java -jar self-extracting.jar</pre>

in a terminal window.

This release of Project OpenDMK has the same features and code base as **Java DMK version 5.1**, with the exception of some legacy or deprecated APIs which have been removed.

This release of Project OpenDMK does not have its own documentation set.

Moreover, this release is intended to run versions 5.0 and above of the Java SE platform.

## Dependencies

*   Java<sup><font size="-2">TM</font></sup> Management Extensions (JMX<sup><font size="-2">TM</font></sup>) Technology:  
    An implementation of the [JMX API 1.2](http://www.jcp.org/en/jsr/detail?id=3) and the [JMX Remote API 1.0](http://www.jcp.org/en/jsr/detail?id=160), such as those included in version 5.0 or higher of the Java SE platform, is required.
*   This release is targetted at versions 5.0 or higher of the Java SE platform. However, it can also run on the JDK 1.4.x releases, with the addition of the necessary JMX API packages listed above.
*   If you wish to build Project OpenDMK sources, you will need either [Apache Ant](http://ant.apache.org/) version 1.6.5 or higher, or alternatively, [NetBeans IDE](http://www.netbeans.org/) version 5.5 or higher.

## Building Project OpenDMK Sources and API Documentation

The binary bundle of OpenDMK already contains all the OpenDMK Java archive (JAR) files and the API documentation. You can, however, <a name="build">build them yourself</a> from the Project OpenDMK source bundle.

<u>How to build the sources</u>:

1.  Download the Project OpenDMK source bundle zip file from [http://opendmk.dev.java.net/](http://opendmk.dev.java.net/), and expand it into a working directory.
2.  Then download the Project OpenDMK self extractible binary plug jar file from [http://opendmk.dev.java.net/](http://opendmk.dev.java.net/), and expand it into the _OpenDMK-src_ directory by typing

    <pre>java -jar downloaded-binary-plug.jar</pre>

    in a terminal window. You should now have a directory named _opendmk-binary-plug_ in the _OpenDMK-src_ directory.
3.  Navigate to the expanded `OpenDMK-src` source directory (where the `build.xml` file is located) and run the following commands:

    `ant jar` to build the jar files in the `dist/lib` directory

    `ant javadoc` to build the Project OpenDMK API documentation in the `dist/docs/api` directory

The `OpenDMK-src` source directory can also be loaded directly in the [NetBeans<sup><font size="-2">TM</font></sup> IDE](http://www.netbeans.org/) as a NetBeans project.

## Linking Your Application With Project OpenDMK

You will need to add the following Project OpenDMK JAR files to your classpath, depending on what you want to do:

*   To run an agent, add <tt>jdmkrt.jar</tt>.
*   To open a JMX Connector or JMX Connector Server using the JMXMP protocol: add <tt>jmxremote_optional.jar</tt>.

## Runtime: Choosing an <tt>MBeanServerBuilder</tt>

The factory used to create MBean server objects is <tt>javax.management.MBeanServerFactory</tt>. By default, this factory uses <tt>javax.management.MBeanServerBuilder</tt> which creates `MBeanServer` instances from the Java platform's JMX 1.2 implementation.

The Project OpenDMK provides a custom builder <tt>com.sun.jdmk.JdmkMBeanServerBuilder</tt> which creates OpenDMK `MBeanServer` instances. You only need to use these if you want to plug interceptors into your MBean server.

To set the Project OpenDMK `MBeanServerBuilder` as the default initial builder, add the following option to the <tt>java</tt> command when you run it:  
**<tt>-Djavax.management.builder.initial=com.sun.jdmk.JdmkMBeanServerBuilder</tt>**

For more details, see the API specification of the [com.sun.jdmk.JdmkMBeanServerBuilder](dist/docs/api/com/sun/jdmk/JdmkMBeanServerBuilder.html "Generated API documentation"), [javax.management.MBeanServerBuilder](http://java.sun.com/javase/6/docs/api/javax/management/MBeanServerBuilder.html) and [javax.management.MBeanServerFactory](http://java.sun.com/javase/6/docs/api/javax/management/MBeanServerFactory.html) classes.

## Additional Documentation

You can refer to the following documents:

*   [Project OpenDMK API documentation](dist/docs/api/index.html "Generated API documentation") (if you have downloaded the source bundle, you may first need to generate the API documentation)
*   [Java DMK 5.1 documentation set](http://java.sun.com/products/jdmk/jdmk_docs.html)

* * *

[Copyright © 1998-2007 Sun Microsystems, Inc.](LEGAL_NOTICES/COPYRIGHT) All rights reserved. Use is subject to [license](LEGAL_NOTICES/license.txt) terms.

build opendmk-1.0-b02
