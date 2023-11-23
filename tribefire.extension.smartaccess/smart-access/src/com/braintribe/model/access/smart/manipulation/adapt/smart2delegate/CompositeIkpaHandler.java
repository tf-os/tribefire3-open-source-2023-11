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
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;

/**
 * Covers properties with {@link CompositeKeyPropertyAssignment} mapping.
 */
public class CompositeIkpaHandler implements Smart2DelegateHandler<CompositeInverseKeyPropertyAssignment> {

	private final IkpaHandler ikpaHandler;

	private Set<InverseKeyPropertyAssignment> ikpas;

	public CompositeIkpaHandler(IkpaHandler ikpaHandler) {
		this.ikpaHandler = ikpaHandler;
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void loadAssignment(CompositeInverseKeyPropertyAssignment assignment) {
		this.ikpas = assignment.getInverseKeyPropertyAssignments();
	}

	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) throws ModelAccessException {
		for (InverseKeyPropertyAssignment kpa: ikpas) {
			ikpaHandler.loadAssignment(kpa);
			ikpaHandler.convertToDelegate(manipulation);
		}
	}

	@Override
	public void convertToDelegate(AddManipulation manipulation) throws ModelAccessException {
		for (InverseKeyPropertyAssignment kpa: ikpas) {
			ikpaHandler.loadAssignment(kpa);
			ikpaHandler.convertToDelegate(manipulation);
		}
	}

	@Override
	public void convertToDelegate(RemoveManipulation manipulation) throws ModelAccessException {
		for (InverseKeyPropertyAssignment kpa: ikpas) {
			ikpaHandler.loadAssignment(kpa);
			ikpaHandler.convertToDelegate(manipulation);
		}
	}

	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) throws ModelAccessException {
		for (InverseKeyPropertyAssignment kpa: ikpas) {
			ikpaHandler.loadAssignment(kpa);
			ikpaHandler.convertToDelegate(manipulation);
		}
	}

}
