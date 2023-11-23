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

import com.google.gwt.user.client.Random;

/**
 * 
 * @author peter.gazdik
 */
public class ColorPicker extends Picker {

	protected int sub_defaultValue; 
	protected int sub_constructorStaticValue;
	protected int sub_directStaticValue = 111;
//	protected int sub_blockStaticValue;

	protected int sub_constructorDynamicValue;
	protected int sub_directDynamicValue = Random.nextInt();
//	protected int sub_blockDynamicValue;

	protected static int sub_staticDirectValue = 5555;
//	protected static int sub_staticBlockValue;

	protected static int sub_staticDynamicValue = Random.nextInt();
//	protected static int sub_staticBlockDynamicValue;

	public ColorPicker() {
		sub_constructorStaticValue = 222;
		sub_constructorDynamicValue = Random.nextInt();
	}
//
//	{
//		sub_directStaticValue = 333;
//		sub_directDynamicValue = Random.nextInt();
//	}

//	static {
//		sub_staticBlockValue = 6666;
//		sub_staticBlockDynamicValue = Random.nextInt();
//	}

	public int pickColor(String userName) {
		return userName == null ? 0 : userName.hashCode();
	}

}
