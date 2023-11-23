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
package com.braintribe.gwt.customization.client.picker;

import com.braintribe.gwt.customization.client.api.IPicker;
import com.google.gwt.user.client.Random;

/**
 * 
 * @author peter.gazdik
 */
public class Picker implements IPicker {

	protected int defaultValue; 
	protected int constructorStaticValue;
	protected int directStaticValue = 111;
	protected int blockStaticValue;

	protected int constructorDynamicValue;
	protected int directDynamicValue = Random.nextInt();
	protected int blockDynamicValue;

	protected static int staticDirectValue = 5555;
	protected static int staticBlockValue;

	protected static int staticDynamicValue = Random.nextInt();
	protected static int staticBlockDynamicValue;

	public Picker() {
		constructorStaticValue = 222;
		constructorDynamicValue = Random.nextInt();
	}

	public Picker(int constructorStaticValue) {
		this.constructorStaticValue = constructorStaticValue;
		this.constructorDynamicValue = Random.nextInt();
	}
	
	{
		blockStaticValue = 333;
		blockDynamicValue = Random.nextInt();
	}

	static {
		staticBlockValue = 6666;
		staticBlockDynamicValue = Random.nextInt();
	}

	@Override
	public String pickerType() {
		return "Picker";
	}

}
