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
package com.braintribe.model.io.metamodel.render.render;

import java.util.Collection;
import java.util.List;

import com.braintribe.model.generic.tools.AbstractStringifier;

/**
 * @author peter.gazdik
 */
public abstract class CustomTypeRenderer extends AbstractStringifier {

	protected void printPackage(String packageName) {
		if (packageName.isEmpty())
			return;

		println("package ", packageName, ";");
		println();
	}

	protected void printImportGroups(List<List<String>> importGrouss) {
		for (List<String> imports : importGrouss)
			printImports(imports);
	}

	protected void printImports(Collection<String> imports) {
		if (imports.isEmpty())
			return;

		for (String i : imports)
			println("import ", i, ";");

		println();
	}

	protected void printAnnotations(List<String> annotations) {
		for (String annotation : annotations)
			println("@", annotation);
	}

	protected void printTypeEnd() {
		print("}");
	}
}
