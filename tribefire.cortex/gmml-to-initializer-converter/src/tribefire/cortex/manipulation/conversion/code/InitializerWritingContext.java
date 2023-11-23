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
package tribefire.cortex.manipulation.conversion.code;

import com.braintribe.utils.StringTools;

import tribefire.cortex.sourcewriter.JavaSourceClass;
import tribefire.cortex.sourcewriter.JavaSourceWriter;

/**
 * @author peter.gazdik
 */
public class InitializerWritingContext {

	public final JscPool jscPool;
	public final JavaSourceWriter spaceWriter;
	public final JavaSourceWriter lookupWriter;

	public boolean newDateFunctionUsed = false;

	public final String lookupContractInstanceName;

	public InitializerWritingContext(JavaSourceWriter spaceWriter, JavaSourceWriter lookupWriter, JscPool jscPool) {
		this.jscPool = jscPool;
		this.spaceWriter = spaceWriter;
		this.lookupWriter = lookupWriter;
		this.lookupContractInstanceName = contractInstanceName(lookupWriter.sourceClass);
	}

	private static String contractInstanceName(JavaSourceClass contractClass) {
		String s = contractClass.simpleName;
		s = StringTools.removeSuffix(s, "Contract");
		s = StringTools.uncapitalize(s);
		return s;
	}

	/** Returns either the simple name or the fully-qualified name (if import failed) of this bean in given */
	public String typeNameInWriter(JavaSourceClass jsc, JavaSourceWriter writer) {
		boolean imported = writer.tryImport(jsc);
		return imported ? jsc.simpleName : jsc.fullName();
	}

}
