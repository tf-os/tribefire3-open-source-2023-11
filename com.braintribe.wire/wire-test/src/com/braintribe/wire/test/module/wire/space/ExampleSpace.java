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
package com.braintribe.wire.test.module.wire.space;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.test.module.ConstantHolder;
import com.braintribe.wire.test.module.wire.contract.ExampleContract;

@Managed
public class ExampleSpace implements ExampleContract {

	private void foo() {
		try {
			Integer nums[] = new Integer[]{1,2,3};
			
			for (Integer element: nums) {
				System.out.println(element);
			}
		}
		catch (Exception e) {
			System.out.println("Error");
		}
		finally {
			System.out.println("unlock");
		}
	}
	
	@Managed
	public ConstantHolder constantHolderX() {
		ConstantHolder bean = new ConstantHolder();
		Integer nums[] = new Integer[]{1,2,3};
		
		for (Integer element: nums) {
			System.out.println(element);
		}
		
		return bean;
	}
	
	@Override
	@Managed
	public ConstantHolder constantHolder() {
		ConstantHolder bean = new ConstantHolder();
		Integer nums[] = new Integer[]{1,2,3};
		
		Integer element;
		int i = 0;
		int max;
		max = nums.length;
		Integer numsCopy[] = nums;
		for (; i < max; i++) {
			int J = 0;
			System.out.println(J);
			element = numsCopy[i];
			System.out.println(element);
		}
		
		return bean;
	}

	
	@Managed
	@Override
	public String text() {
		return "Hello World!";
	}

	@Managed
	@Override
	public String text(String prefix) {
		return prefix + "-foobar"; 
	}
	
	
}
