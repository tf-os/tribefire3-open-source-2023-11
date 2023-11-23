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
package com.braintribe.model.processing.manipulation.basic.tools.traverse;

import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;

/**
 * 
 */
public class EmptyManipulationVisitor implements ManipulationVisitor {

	@Override
	public void visit(InstantiationManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(DeleteManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(ChangeValueManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(AddManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(RemoveManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(ClearCollectionManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(ManifestationManipulation m) {
		// implement in sub-type
	}

	@Override
	public void visit(AbsentingManipulation m) {
		// implement in sub-type
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(CompoundManipulation cm) {
		return true;
	}

}
