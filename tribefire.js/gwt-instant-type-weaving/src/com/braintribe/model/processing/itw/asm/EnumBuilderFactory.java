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
package com.braintribe.model.processing.itw.asm;

import java.util.List;

/**
 * A factory for EnumBuilders - in case somebody wants to extend the behavior of the {@link EnumBuilder}, which is a
 * little tricky as the enum constants are initialized in the {@Code <clinit>} method. Thus if you want extend it,
 * you need a sub-type which adds some more bytecode to the default implementation.
 * 
 * @author peter.gazdik
 */
public interface EnumBuilderFactory<T extends EnumBuilder> {

	T create(AsmClassPool classPool, String typeSignature, List<AsmEnumConstant> constants, List<AsmClass> superInterfaces);

}
