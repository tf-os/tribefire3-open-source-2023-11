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
package com.braintribe.model.processing.itw.synthesis.gm.experts.template;

import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_ID;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE_ALT;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_TYPE_SHORT;
import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.SI_RUNTIME_ID;

public class MagicVariableNames {

	public enum MagicVarKind {
		none,
		var_type,
		var_type_short,
		var_id,
		var_runtimeId
	}

	public static MagicVarKind getMagicVarKind(String varName) {
		switch (varName) {
			case SI_TYPE:
			case SI_TYPE_ALT:
				return MagicVarKind.var_type;
			case SI_TYPE_SHORT:
				return MagicVarKind.var_type_short;
			case SI_ID:
				return MagicVarKind.var_id;
			case SI_RUNTIME_ID:
				return MagicVarKind.var_runtimeId;
			default:
				return MagicVarKind.none;
		}
	}

}
