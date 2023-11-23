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

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;

/**
 * a context for the bidirectional link declarations
 * 
 * @author pit
 *
 * @param <T>
 */
public class BidirectionalLinkContext<T extends BidirectionalLinkReceiver> implements SchemaAddressConsumer {

	private T consumer;
	private BidirectionalLink link = BidirectionalLink.T.create();

	public BidirectionalLinkContext(T consumer) {
		this.consumer = consumer;
	}
	
	/**
	 * specify the address within the schema
	 * @return - a {@link SchemaAddressContext}
	 */
	public SchemaAddressContext<BidirectionalLinkContext<T>> schemaAddress() {
		return new SchemaAddressContext<BidirectionalLinkContext<T>>(this);
	}
	
	/**
	 * specify the target property (to point to the parent)
	 * @param property
	 * @return
	 */
	public BidirectionalLinkContext<T> property(String property) {
		link.setBacklinkProperty(property);
		return this;
	}

	@Override
	public void accept(SchemaAddress address) {
		link.setSchemaAddress(address);		
	}
	
	/**
	 * close the context and return to parent context
	 * @return - the parent context
	 */
	public T close() {
		consumer.accept(link);
		return consumer;
	}
}
