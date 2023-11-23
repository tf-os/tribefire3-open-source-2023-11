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

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ShallowSubstitutingModel;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.Substitution;

/**
 * a context to specify the substitution of types, grouped by the model of origin 
 * @author pit
 *
 * @param <T>
 */
public class ShallowSubstitutionModelContext<T extends ShallowSubstitutionModelConsumer> implements SubstitutionConsumer {
	
	private ShallowSubstitutingModel substitionModel = ShallowSubstitutingModel.T.create();
	private T consumer;
	
	public ShallowSubstitutionModelContext(T consumer) {
		this.consumer = consumer;
	}
	
	public ShallowSubstitutionModelContext<T> modelName( String name) {			
		substitionModel.setDeclaringModel( name);
		return this;
	}
	
	/**
	 * declared the types and their substitutions
	 * @return - a {@link SubstitutionContext}
	 */
	public SubstitutionContext<ShallowSubstitutionModelContext<T>> substitution() {
		return new SubstitutionContext<ShallowSubstitutionModelContext<T>>( this);
	}		
	
	@Override
	public void accept(Substitution substitution) {	
		substitionModel.getSubstitutions().add(substitution);
	}
	

	/**
	 * finish and return to parent context
	 * @return - the parent context
	 */
	public T close() {
		consumer.accept( substitionModel);
		return consumer;
	}
}
