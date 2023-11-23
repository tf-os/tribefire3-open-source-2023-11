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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemaAddress;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.Substitution;

/**
 * a context to specify substitution.. 
 * @author pit
 *
 * @param <T>
 */
public class SubstitutionContext<T extends SubstitutionConsumer> implements SchemaAddressConsumer {
	private Substitution substitution = Substitution.T.create();
	private T consumer;
	
	public SubstitutionContext(T consumer) {
		this.consumer = consumer;
	}
	
	/**
	 * declare the matching address
	 * @return
	 */
	public SchemaAddressContext<SubstitutionContext<T>> schemaAddress() {
		return new SchemaAddressContext<SubstitutionContext<T>>(this);
	}

	@Override
	public void accept(SchemaAddress address) {
		substitution.setSchemaAddress(address);
	}
	
	/**
	 * declares the signature of the {@link GmEntityType} you want to use
	 * @param signature - the fully qualified type signature 
	 * @return - 
	 */
	public SubstitutionContext<T> replacementSignature(String signature) {
		substitution.setReplacementSignature(signature);
		return this;
	}
	
	/**
	 * if not set, the global id is derived from the signature (as JTA does) 
	 * @param globalId - the global id 
	 * @return
	 */
	public SubstitutionContext<T> replacementGlobalId(String globalId) {
		substitution.setReplacementGlobalId(globalId);
		return this;
	}
	
	/**
	 * finish and return to parent context
	 * @return - the parent context
	 */
	public T close() {
		// if not global id is set, derive it from the signature
		if (substitution.getReplacementGlobalId() == null) {
			substitution.setReplacementGlobalId( JavaTypeAnalysis.typeGlobalId( substitution.getReplacementSignature()));
		}
		consumer.accept( substitution);
		return consumer;
	}
	

}
