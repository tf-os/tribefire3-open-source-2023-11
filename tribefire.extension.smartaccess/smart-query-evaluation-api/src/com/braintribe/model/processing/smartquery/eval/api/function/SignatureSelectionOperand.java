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
package com.braintribe.model.processing.smartquery.eval.api.function;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Source;

/**
 * 
 * @author peter.gazdik
 */
public interface SignatureSelectionOperand extends Operand {
	
	final EntityType<SignatureSelectionOperand> T = EntityTypes.T(SignatureSelectionOperand.class);

	// @formatter:off
	Source getSource();
	void setSource(Source source);
	// @formatter:on

	@Override
	int hashCode();

	@Override
	boolean equals(Object other);

	public static class DefaultMethods {
		public static int hashCode(SignatureSelectionOperand me) {
			return me.getSource().hashCode();
		}

		public static boolean equals(SignatureSelectionOperand me, Object obj) {
			if (me == obj) {
				return true;
			}

			if (obj == null) {
				return false;
			}

			return me.getSource() == ((SignatureSelectionOperand) obj).getSource();
		}
	}

}
