# Tools4j Config: Configuration Management for the Enterprise

## Mission and purpose

The mission of the project is to support long-running enterprise Java applications with a framework for handling configuration changes without restarting themselves.

The framework also aid developing applications that are decoupled from knowing how and where to store, retrieve and validate configuration.

The aim is liberate applications to use configuration seamlessly on the terms of their particular environment without constraining  them    to Java SE,  EE, OSGi, Spring, CDI or  any other programming model or framework.

## Goals  
To fill a relevant need in the Java community  and support building highly-available applications we believe that the following goals should be pursued. 

* Productivity and Simplicity  
Using configuration must be intuitive and non-intrusive; managed in a unified way to support developer productivity. Configuration is published and discovered automatically when it become available and is also reusable in different contexts without burdening applications with portability issues.

* Predictability and Clarity  
Configuration is strongly-typed and give the capability to declaratively express the intents and rules under which circumstances the application can promise correct behaviour. Violations are handled reliably and does not disturb application behaviour. Valid changes are applied and exposed to applications in a consistent way.

* Extendability and Portability  
As applications are developed in different shapes and sizes; configuration should enable, not limit, a diversity of platforms and technologies.  Applications are supported with the flexibility to  extend and customize a variety aspects locally and still be able to manage configuration in a central and unified way.

* Performance and Scalability  
Configuration should not be a limiting factor  to application performance.  It is quickly accessible to be able to meet service-level agreements in environments of scale.

## Tools4j Config URLs

Website         : http://tools4j-config.deephacks.org  
Users           : tools4j-config-user@googlegroups.com  
Developers      : tools4j-config-dev@googlegroups.com  
Source Code     : git@github.com:deephacks/tools4j.git  
Issue Tracker   : https://github.com/deephacks/tools4j/issues  

## Licensing

This distribution, as a whole, is licensed under the terms of the Apache License, Version 2.0 (see license.txt).

## Documentation

Javadoc for the public API can be generated from this directory using the following command:

    mvn javadoc:aggregate

DocBook documentation can be generated into html and pdf format from the config-docbkx directoring using the following commands:

    mvn docbkx:generate-pdf
    mvn docbkx:generate-html
`