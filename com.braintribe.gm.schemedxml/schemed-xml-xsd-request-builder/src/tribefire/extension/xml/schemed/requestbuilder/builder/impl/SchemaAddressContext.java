// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.xml.schemed.requestbuilder.builder.impl;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;

/**
 * a specialized context for names within XSD.
 * It's not a XmlPath or so, just a simple type (ComplexType) and property (element) pairing 
 * 
 * @author pit
 *
 * @param <T>
 */
public class SchemaAddressContext<T extends SchemaAddressConsumer> {
	private T consumer;
	private SchemaAddress schemaAddress = SchemaAddress.T.create();
	
	public SchemaAddressContext(T consumer) {
		this.consumer = consumer;
	}
	
	/**
	 * specify the name of the ComplexType to match
	 * @param complexType - the name 
	 * @return - this context
	 */
	public SchemaAddressContext<T> type(String complexType) {
		schemaAddress.setParent(complexType);
		return this;
	}
	/**
	 * specify the name of the Element to match. If null, just the ComplexType matches,
	 * e.g. to rename the type rather than the property. 
	 * @param element - the name
	 * @return - this context
	 */
	public SchemaAddressContext<T> property(String element) {
		schemaAddress.setElement(element);
		return this;
	}
	
	/**
	 * finish and return to parent context 
	 * @return - the parent context
	 */
	public T close() {
		consumer.accept(schemaAddress);
		return consumer; 
	}
}
