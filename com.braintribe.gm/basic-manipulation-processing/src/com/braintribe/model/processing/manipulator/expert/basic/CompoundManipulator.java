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
package com.braintribe.model.processing.manipulator.expert.basic;

import java.util.List;

import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.manipulator.api.Manipulator;
import com.braintribe.model.processing.manipulator.api.ManipulatorContext;

public class CompoundManipulator implements Manipulator<CompoundManipulation> {
	public static final CompoundManipulator defaultInstance = new CompoundManipulator();
	
	@Override
	public void apply(CompoundManipulation manipulation, ManipulatorContext context) {
		List<Manipulation> manipulations = manipulation.getCompoundManipulationList();
		
		if (manipulations != null) {
			for (Manipulation childManipulation : manipulations) {
				context.apply(childManipulation);
			}
		}
	}
}
