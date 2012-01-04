/**
* A set of service provider interfaces (SPI) each dealing with a separate concern within the 
* architecture. Each SPI is orthogonal and should not need direct dependency or interaction with 
* other SPI. It is the responsibility of the core to glue/mediate such communication in a 
* decoupled fashion.
* <p>
* Providers are free to implement any SPI. Such third-party services can be registered 
* and automatically bound to core functionality when the application is loaded.
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
* The following SPIs are available:
* <ul>
* <li> {@link org.deephacks.tools4j.config.spi.BeanManager}</li>
* <li> {@link org.deephacks.tools4j.config.spi.SchemaManager}</li>
* <li> {@link org.deephacks.tools4j.config.spi.ValidationManager}</li>
* <li> {@link org.deephacks.tools4j.support.conversion.Converter}</li>
* This interface takes care of converting configuration property types to-and-from String 
* (which is the format for storing property values).
* </ul> 
* </p>
*
* <h2>Usage</h2>
* 
* TODO:
*/
package org.deephacks.tools4j.config.spi;

;