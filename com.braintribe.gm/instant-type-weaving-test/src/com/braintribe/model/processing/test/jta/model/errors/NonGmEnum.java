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
package com.braintribe.model.processing.test.jta.model.errors;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

/**
 * This is not a GM enum, because it doesn't extends {@link EnumBase}.
 * <p>
 * We test that a property of this type is forbidden and {@link JavaTypeAnalysis} throws an exception in such case. Transient property is OK though.
 * 
 * @author peter.gazdik
 */
public enum NonGmEnum {
	x,
	y,
	z
}
