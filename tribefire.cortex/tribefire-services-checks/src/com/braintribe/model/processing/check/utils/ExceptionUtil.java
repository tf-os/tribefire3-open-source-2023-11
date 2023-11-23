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
package com.braintribe.model.processing.check.utils;

public class ExceptionUtil {

	private static final String EXCEPTION_SUFIX = "Exception:";
	
	private ExceptionUtil() {
		//private constructor to ensure only static usage of this class.
	}
	
	public static String getLastMessage(Throwable throwable) {
		if (throwable == null)
			return null;
		
		String message = throwable.getMessage();
		while ((throwable = throwable.getCause()) != null) {
			if (throwable.getMessage() != null) {
				message = throwable.getMessage();
			}
		}
		
		if (message != null) {
			int index = message.indexOf(EXCEPTION_SUFIX);
			if (index != -1) {
				String initialPart = message.substring(0, index);
				if (!initialPart.contains(" ") && initialPart.length() > EXCEPTION_SUFIX.length()) {
					message = message.substring(index + EXCEPTION_SUFIX.length()).trim();
				}
			}
		}
		
		return message;
	}
}
