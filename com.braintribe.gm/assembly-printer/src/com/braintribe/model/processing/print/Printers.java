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
package com.braintribe.model.processing.print;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.print.experts.GenericPrinter;
import com.braintribe.model.processing.print.experts.GenericRefPrinter;

/**
 * @author peter.gazdik
 */
public interface Printers {

	static BasicCustomizableAssemblyPrinter newEmptyPrinter() {
		return new BasicCustomizableAssemblyPrinter();
	}

	static BasicCustomizableAssemblyPrinter newGenericPrinter() {
		BasicCustomizableAssemblyPrinter result = new BasicCustomizableAssemblyPrinter();
		result.registerFullPrinter(GenericEntity.T, new GenericPrinter());
		result.registerRefPrinter(GenericEntity.T, new GenericRefPrinter(false, false, false));
		result.ignoreProperty(GenericEntity.T.getProperty(GenericEntity.id));
		return result;
	}


}
