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
package com.braintribe.model.processing.assembly.sync.impl;

import java.util.function.Predicate;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.assembly.sync.api.AssemblyImportContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferExpert;

/**
 * @author peter.gazdik
 */
public class GenericImporterContext<T extends GenericEntity> implements AssemblyImportContext<T> {

	private PersistenceGmSession session;
	private T assembly;
	private boolean includeEnvelope;
	private boolean requireAllGlobalIds;
	private Predicate<GenericEntity> envelopePredicate;
	private Predicate<GenericEntity> externalPredicate;
	private String defaultPartition;
	private PropertyTransferExpert propertyTransferExpert;
	
	@Override
	public T getAssembly() {
		return assembly;
	}

	public void setAssembly(T assembly) {
		this.assembly = assembly;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	@Override
	public boolean isExternalReference(GenericEntity entity) {
		return externalPredicate.test(entity);
	}

	public void setExternalPredicate(Predicate<GenericEntity> externalPredicate) {
		this.externalPredicate = externalPredicate;
	}

	@Override
	public boolean isEnvelope(GenericEntity entity) {
		return envelopePredicate.test(entity);
	}

	public void setEnvelopePredicate(Predicate<GenericEntity> envelopePredicate) {
		this.envelopePredicate = envelopePredicate;
	}

	@Override
	public boolean includeEnvelope() {
		return includeEnvelope;
	}

	public void setIncludeEnvelope(boolean includeEnvelope) {
		this.includeEnvelope = includeEnvelope;
	}

	@Override
	public boolean requireAllGlobalIds() {
		return requireAllGlobalIds;
	}

	public void setRequireAllGlobalIds(boolean requireAllGlobalIds) {
		this.requireAllGlobalIds = requireAllGlobalIds;
	}

	@Override
	@Deprecated
	public String getDefaultPartition() {
		return defaultPartition;
	}

	public void setDefaultPartition(String defaultPartition) {
		this.defaultPartition = defaultPartition;
	}
	
	public void setPropertyTransferExpert(PropertyTransferExpert propertyTransferExpert) {
		this.propertyTransferExpert = propertyTransferExpert;
	}
	
	@Override
	public com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferExpert propertyTransferExpert() {
		return this.propertyTransferExpert;
	}

}
