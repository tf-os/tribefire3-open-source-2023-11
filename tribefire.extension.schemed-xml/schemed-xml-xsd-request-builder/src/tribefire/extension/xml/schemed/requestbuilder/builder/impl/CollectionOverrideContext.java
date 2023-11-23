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

import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmSetType;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.CollectionOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;

/**
 * a context to specify what collection type should be used for a certain property. 
 * Default is to use {@link GmListType} type, but you can override it to used {@link GmSetType}
 *  
 * @author pit
 *
 * @param <T>
 */
public class CollectionOverrideContext<T extends CollectionOverrideConsumer> implements SchemaAddressConsumer{

	private CollectionOverride override = CollectionOverride.T.create();
	private T consumer;
	
	public CollectionOverrideContext(T consumer) {
		this.consumer = consumer;		
	}
		
	/**
	 * access the {@link SchemaAddressContext} to specify what type/property combination you want to 
	 * influence
	 * @return - a {@link SchemaAddressContext}, specialized of internal addresses in XSD
	 */
	public SchemaAddressContext<CollectionOverrideContext<T>> schemaAddress() {
		return new SchemaAddressContext<CollectionOverrideContext<T>>(this);
	}

	@Override
	public void accept(SchemaAddress address) {
		override.setSchemaAddress(address);		
	}
	
	/**
	 * specify that a {@link GmSetType} should be used
	 * @return - the {@link CollectionOverrideContext}
	 */
	public CollectionOverrideContext<T> asSet() {
		override.setCollectionAsSet(true);
		return this;
	}
	
	/**
	 * specify that a {@link GmListType} should be used (this is default)
	 * @return - the {@link CollectionOverrideContext}
	 */
	public CollectionOverrideContext<T> asList() {
		override.setCollectionAsSet(true);
		return this;
	}
	
	/**
	 * finish and return to parent context
	 * @return - the parent context
	 */
	public T close() {
		consumer.accept(override);
		return consumer;
	}
	
}
