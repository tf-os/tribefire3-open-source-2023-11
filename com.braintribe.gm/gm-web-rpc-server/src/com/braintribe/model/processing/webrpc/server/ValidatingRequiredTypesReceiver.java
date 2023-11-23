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
package com.braintribe.model.processing.webrpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

public class ValidatingRequiredTypesReceiver implements Consumer<Set<String>> {
	
	@Override
	public void accept(Set<String> requiredTypes) {
		GenericModelTypeReflection reflection = GMF.getTypeReflection();

		List<String> missingTypeSignatures = null;
		
		for (String typeSignature: requiredTypes) {
			GenericModelType type = reflection.findType(typeSignature);
			
			if (type == null) {
				if (missingTypeSignatures == null)
					missingTypeSignatures = new ArrayList<>();
				
				missingTypeSignatures.add(typeSignature);
			}
		}
		
		if (missingTypeSignatures != null)
			throw new IllegalStateException("The following GM Types are required but are not present at the Classloader: " + missingTypeSignatures);
	}
}
