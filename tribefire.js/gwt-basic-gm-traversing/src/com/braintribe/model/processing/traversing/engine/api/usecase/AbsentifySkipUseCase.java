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
package com.braintribe.model.processing.traversing.engine.api.usecase;

import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.processing.traversing.api.SkipUseCase;

/**
 * If a skip occurs, properties have to be absentified, i.e. they will be replaced with {@link AbsenceInformation} in the cloned assembly
 *
 */
public class AbsentifySkipUseCase implements SkipUseCase {

	public static final AbsentifySkipUseCase INSTANCE = new AbsentifySkipUseCase();

	private AbsentifySkipUseCase() {
	}

}
