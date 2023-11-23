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
package com.braintribe.model.generic.reflection;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ClassMapper {

	// @formatter:off
	static Class<?>[] classes = {
			String.class,
			Boolean.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class,
			BigDecimal.class,
			Date.class,
			java.sql.Time.class,
			java.sql.Date.class,
			java.sql.Timestamp.class,
			java.util.ArrayList.class,
			java.util.LinkedList.class,
			java.util.HashSet.class,
			java.util.LinkedHashSet.class,
			java.util.TreeSet.class,
			java.util.HashMap.class,
			java.util.LinkedHashMap.class,
			java.util.IdentityHashMap.class,
			java.util.TreeMap.class
	};
	// @formatter:on
	 
	public static void main(String[] args) {
		Set<Integer> hashes = new HashSet<>();

		for (Class<?> clazz : classes) {
			String name = clazz.getSimpleName();
			String fullName = clazz.getName();
//			int hash = name.charAt(0) ^ name.charAt(name.length() - 1);
			int hash = count(name.charAt(0), name.charAt(name.length() - 1), fullName.length());
			hashes.add(hash);
			System.out.println(name + " -> " + hash);
		}

		System.out.println(classes.length + " : " + hashes.size());
		System.out.println(hashes);

	}

	private static int count(int a, int b, int c) {
//		return (b + c) ^ a;
		return a ^ b + c;
	}

}
