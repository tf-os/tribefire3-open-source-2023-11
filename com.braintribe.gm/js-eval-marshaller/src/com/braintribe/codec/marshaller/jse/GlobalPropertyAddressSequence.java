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
package com.braintribe.codec.marshaller.jse;

public class GlobalPropertyAddressSequence extends PoolAddressSequence  {
	private static final char[] firstDigitChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$0123456789".toCharArray();
	
	private int[] sequence = new int[6];
	private int digits = 1;
	public int count = 0;
	
	public char[] next() {
		count++;
		char extract[] = new char[digits];
		char chars[] = firstDigitChars;
		int base = chars.length;
		boolean carry = true;
		for (int i = 0; i < digits; i++) {
			int d = sequence[i];
			
			extract[i] = chars[d];
			
			if (carry) {
				d++;
				
				if (d == base) {
					sequence[i] = 0;
				}
				else {
					sequence[i] = d;
					carry = false;
				}
			}
			
			if (i == 0) {
				chars = GlobalPropertyAddressSequence.chars;
				base = chars.length;
			}
		}
		
		if (carry) {
			digits++;
		}
		
		return extract;
	}
	
	public int getCount() {
		return count;
	}
	
	public static void main(String[] args) {
		GlobalPropertyAddressSequence sequence = new GlobalPropertyAddressSequence();
		for (int i = 0; i < 100000; i++) {
			System.out.println(new String(sequence.next()));
		}
	}
}
