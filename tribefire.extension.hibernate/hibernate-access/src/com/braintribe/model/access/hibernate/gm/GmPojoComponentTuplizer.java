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
package com.braintribe.model.access.hibernate.gm;

import org.hibernate.mapping.Component;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.component.PojoComponentTuplizer;

/**
 * Hibernate does not use interceptors for embedded properties (components), thus we need a different way how to tweak
 * it to be able to instantiate GM entities.
 * 
 * As of the time this was written (21.3.2018), we define this tuplizer for every embedded property, i.e. every
 * "component" tag in the mapping xmls.
 * 
 * @author peter.gazdik
 */
public class GmPojoComponentTuplizer extends PojoComponentTuplizer {

	private static final long serialVersionUID = -6809959369852886449L;

	public GmPojoComponentTuplizer(Component component) {
		super(component);
	}

	@Override
	protected Instantiator buildInstantiator(Component component) {
		return new GmPojoInstantiator(component, null);
	}
}
