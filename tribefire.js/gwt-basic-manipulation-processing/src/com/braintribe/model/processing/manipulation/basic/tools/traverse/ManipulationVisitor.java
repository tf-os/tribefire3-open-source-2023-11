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
 * Used in combination with {@link ManipulationTraverser}.
 * 
 * @author peter.gazdik
 */
public interface ManipulationVisitor {

	void visit(InstantiationManipulation m);
	void visit(DeleteManipulation m);

	void visit(ChangeValueManipulation m);

	void visit(AddManipulation m);
	void visit(RemoveManipulation m);
	void visit(ClearCollectionManipulation m);

	void visit(ManifestationManipulation m);
	void visit(AbsentingManipulation m);

	/**
	 * @return true iff given {@link CompoundManipulation} should be traversed recursively
	 */
	boolean visit(CompoundManipulation cm);

}
