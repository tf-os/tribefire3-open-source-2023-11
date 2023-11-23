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
package com.braintribe.logging;

import java.awt.Component;

public interface ErrorFeedback {

	public void reportError(String msg, Throwable ex, boolean critical);

	/**
	 * reports an error
	 * 
	 * @param msg
	 *            the error message
	 * @param ex
	 *            the exception that cause the error (or null)
	 * @param critical
	 *            if true, the error is critial. Exiting the application may be recommended.
	 */
	public void reportError(Component component, String msg, Throwable ex, boolean critical);

	/**
	 * reports an error, prompting for retry
	 * 
	 * @param msg
	 *            the error message
	 * @param ex
	 *            the exception that cause the error (or null)
	 * @param allowExit
	 *            if true, the user may choose to terminate the application.
	 */
	public boolean reportErrorWithRetry(Component component, String msg, Throwable ex, boolean allowExit);
}
