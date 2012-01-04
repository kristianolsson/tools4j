/**
* Provides a set of modular service interfaces each dealing with a separate concern within the 
* architecture. These interfaces are also called SPI. Each SPI are orthogonal in relation to 
* each other and should not have need direct dependency between each other directly. It is 
* the task of the core to glue/mediate interactionbetween services.
* <p>
* Providers are free to implement any SPI. Such third-party services can be registered 
* and automatically bind to the core functionality at when the application is loaded.
* The mechanism used for this purpose is the standard {@link java.util.ServiceLoader}.
* <p>
* A Test Compability Kit (TCK) is available for providers to verify that their 
* implementation behaves correctly.
* </p>
* <p> 
* Each service have a default implementation that will be used if no other is provided. 
* </p>
* 
* <p>
* The following SPI is available:
* <ul>
* <li> {@link org.deephacks.tools4j.config.spi.BeanManager}</li>
* <li> {@link org.deephacks.tools4j.config.spi.SchemaManager}</li>
* <li> {@link org.deephacks.tools4j.config.spi.ValidationManager}</li>
* <li> Converter</li>
* This interface takes care of converting class variables to-and-from the BeanManager.
* It is not part of the tools4j config packages. 
* </ul> 
* </p>
*
* <h2>Usage</h2>
* 
* TODO:
*/
package org.deephacks.tools4j.config.spi;

;