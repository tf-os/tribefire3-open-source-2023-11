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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;

/**
 * 
 * @author peter.gazdik
 */
public class ConversionWrapper {

	private SmartConversion conversion;
	private ConversionWrapper inverse;

	public static ConversionWrapper instanceFor(SmartConversion conversion) {
		return conversion == null ? null : new ConversionWrapper(conversion);
	}

	private ConversionWrapper() {
	}

	private ConversionWrapper(SmartConversion conversion) {
		this.conversion = conversion;

		this.inverse = new ConversionWrapper();
		this.inverse.inverse = this;
	}

	public SmartConversion actualConversion() {
		return conversion == null ? inverse.conversion : conversion;
	}

	public static ConversionWrapper inverseOf(ConversionWrapper cw) {
		return cw == null ? null : cw.inverse;
	}

	public static SmartConversion extractConversion(ConversionWrapper cw) {
		return cw == null ? null : cw.conversion;
	}

}
