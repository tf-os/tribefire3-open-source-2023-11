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
package com.braintribe.model.processing.itw.synthesis.java;

/**
 * Constants that are used to initialize primitive properties in case we are using the Object array to represent properties (efficient mode). 
 */
public class JvmConstants {

	private static char c;
	public static Character DEFAULT_CHAR = c;
	
	private static byte b;
	public static Byte DEFAULT_BYTE = b;
	
	private static short s;
	public static Short DEFAULT_SHORT = s;
	
	private static int i;
	public static Integer DEFAULT_INT = i;
	
	private static long l;
	public static Long DEFAULT_LONG = l;
	
	private static float f;
	public static Float DEFAULT_FLOAT = f;
	
	private static double d;
	public static Double DEFAULT_DOUBLE = d;
	
	private static boolean bool;
	public static Boolean DEFAULT_BOOLEAN = bool;
	
}
