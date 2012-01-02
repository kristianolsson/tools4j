/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.tools4j.support.event;

/**
 * Severity is responsible for indicating the severity of certain responses 
 * reported within the system.
 * 
 * The level is used to categorize statuses reported from modules and separate documentation 
 * between different users.
 * 
 */
public enum Severity {
    /**
     * Responses that occur frequently from using the well documented interfaces as expected.
     * Well documented in customer documentation. Causes should be easy to find, but may be subject 
     * to basic level technical support by analyzing and gathering data for symptoms.
     * 
     * Understandable by Regular Users and Administrators of the module, First Line Support.
     */
    L1,
    /**
     * For knowledgeable personnel that can do technical troubleshooting and assert validity of 
     * problems and seeking for known solutions. 
     * 
     * May occur from using internal, less documented interfaces to create customized 
     * behaviour and similar. 
     * 
     * Understandable by Expert Users, Expert Administrators, Second Line Support, 
     * Service Personel and Operations.
     */
    L2,
    /**
     * Statuses reported from difficult or advanced problems that may require expert level troubleshooting.  
     * Maybe caused from implementation details not mentioned in customer documentation and probably 
     * does not make any sense for a person that is unconfortable with analysing log files on debug level.
     * 
     * May cause customer escalations. 
     *  
     * Understandable by Operations, Service Personel and Second Line Support.
     */
    L3,
    /**
     * Failures that occur in low level implementation details only documented in internal developer documentation
     * that may be unexpected or unknown. Lengthly speculations by expert developers around failure causes can
     * be expected. In some cases high priority customer escalations. 
     * 
     * Understandable by Developers and maybe Operations.
     */
    L4;
}
