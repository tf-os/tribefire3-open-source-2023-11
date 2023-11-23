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
package com.braintribe.model.access.smart.manipulation.adapt.smart2delegate;

import java.util.Set;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.CompositeKpaPropertyWrapper;

/**
 * Covers properties with {@link CompositeKeyPropertyAssignment} mapping.
 */
public class CompositeKpaHandler implements Smart2DelegateHandler<CompositeKeyPropertyAssignment> {

	private final SmartManipulationProcessor smp;
	private final SmartManipulationContextVariables $;

	private final StandardHandler standardPmh;

	private Set<KeyPropertyAssignment> kpas;
	private CompositeKpaPropertyWrapper compositeEpm;

	public CompositeKpaHandler(SmartManipulationProcessor smp, StandardHandler standardPmh) {
		this.smp = smp;
		this.$ = smp.context();
		this.standardPmh = standardPmh;
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void loadAssignment(CompositeKeyPropertyAssignment assignment) {
		compositeEpm = (CompositeKpaPropertyWrapper) smp.modelExpert().resolveEntityPropertyMapping($.currentSmartType, $.currentAccess,
				$.currentSmartOwner.getPropertyName());

		this.kpas = assignment.getKeyPropertyAssignments();
	}

	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) throws ModelAccessException {
		for (KeyPropertyAssignment kpa: kpas) {
			EntityPropertyMapping epm = compositeEpm.getPartialEntityPropertyMapping(kpa);

			standardPmh.loadAssignment(kpa, epm);
			standardPmh.convertToDelegate(manipulation);
		}
	}

	@Override
	public void convertToDelegate(AddManipulation manipulation) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'CompositeKpaHandler.convertToDelegate' is not implemented supported!");
	}

	@Override
	public void convertToDelegate(RemoveManipulation manipulation) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'CompositeKpaHandler.convertToDelegate' is not implemented supported!");
	}

	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'CompositeKpaHandler.convertToDelegate' is not implemented supported!");
	}

}
