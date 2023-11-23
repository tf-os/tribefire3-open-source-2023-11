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
package com.braintribe.model.processing.deployment.api;

/**
 * This is an entry for a component of the {@link DeployedUnit} which is found in a {@link DeployRegistry}
 * @author dirk.scheffler
 */
public interface DeployedComponent {
	/**
	 * The maybe enriched value which could add additional value to an expert that was given by a customer's deployable binding
	 * In some cases it is not enriched and then equal to {@link #suppliedImplementation()}
	 * 
	 * One example:
	 * 
	 * Given: a supplier for a SmoodAccess bound to a denotation type with an IncrementalAccessBinder 
	 * Result: InternalizingPersistenceProcessor(AopAccess(SmoodAccess))
	 */
	Object exposedImplementation();
	
	/**
	 * The original value that came from a supplier that was bound to some denotation type with help of a {@link ComponentBinder}
	 */
	Object suppliedImplementation();
	
	/**
	 * The {@link ComponentBinder} that was used to create this DeployedComponent
	 */
	ComponentBinder<?, ?> binder();
}
