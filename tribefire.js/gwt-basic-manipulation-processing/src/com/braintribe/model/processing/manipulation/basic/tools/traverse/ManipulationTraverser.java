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

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;

/**
 * 
 * @author peter.gazdik
 */
public class ManipulationTraverser {

	public static void traverse(Manipulation m, ManipulationVisitor visitor) {
		switch (m.manipulationType()) {
			case COMPOUND: {
				CompoundManipulation cm = (CompoundManipulation) m;
				
				if (visitor.visit(cm)) {
					for (Manipulation mm: cm.getCompoundManipulationList()) {
						traverse(mm, visitor);
					}
				}
				return;
			}

			case ABSENTING:
				visitor.visit((AbsentingManipulation) m);
				return;
			case ADD:
				visitor.visit((AddManipulation) m);
				return;
			case CHANGE_VALUE:
				visitor.visit((ChangeValueManipulation) m);
				return;
			case CLEAR_COLLECTION:
				visitor.visit((ClearCollectionManipulation) m);
				return;
			case DELETE:
				visitor.visit((DeleteManipulation) m);
				return;
			case INSTANTIATION:
				visitor.visit((InstantiationManipulation) m);
				return;
			case MANIFESTATION:
				visitor.visit((ManifestationManipulation) m);
				return;
			case REMOVE:
				visitor.visit((RemoveManipulation) m);
				return;

			case VOID:
				// can be ignored
				return;

			default:
				throw new UnknownEnumException(m.manipulationType());
		}
	}

}
