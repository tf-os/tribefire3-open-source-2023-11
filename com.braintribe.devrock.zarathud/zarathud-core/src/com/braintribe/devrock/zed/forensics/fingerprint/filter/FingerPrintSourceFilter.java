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
package com.braintribe.devrock.zed.forensics.fingerprint.filter;

import java.util.function.Predicate;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasGenericNature;
import com.braintribe.zarathud.model.data.natures.HasMethodsNature;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class FingerPrintSourceFilter implements Predicate<ZedEntity> {
	
	private FingerPrint fingerPrint;

	public FingerPrintSourceFilter(FingerPrint fp) {
		this.fingerPrint = fp;				
	}

	@Override
	public boolean test(ZedEntity t) {
		// compare all fields other than ISSUE..
		// type 
		FingerPrint tfp = FingerPrintExpert.build( t);
		if (FingerPrintExpert.matches( tfp, fingerPrint))
			return true;
		
		// methods
		if (t instanceof HasMethodsNature) {			
			for (MethodEntity me : ((HasMethodsNature) t).getMethods()) {
				FingerPrint mfp = FingerPrintExpert.build(me);
				if (FingerPrintExpert.matches( mfp, fingerPrint)) {
					return true;
				}
				// return type, arguments, exceptions (not supported yet)
			} 			
		}
		// fields
		if (t instanceof HasFieldsNature) {
			for (FieldEntity fe : ((HasFieldsNature) t).getFields()) {
				FingerPrint ffp = FingerPrintExpert.build( fe);
				if (FingerPrintExpert.matches(ffp, fingerPrint)) {
					return true;
				}
			}
		}
		// generic entity stuff
		if (t instanceof HasGenericNature) {
			// properties are current *not* part of the extract..?? 
		}
		
		
		
		return false;
	}

}
