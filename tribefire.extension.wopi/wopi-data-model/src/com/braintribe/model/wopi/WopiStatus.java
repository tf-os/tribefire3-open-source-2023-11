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
package com.braintribe.model.wopi;

/**
 * Status of a {@link WopiSession}
 * 
 *
 */
public enum WopiStatus {
	/**
	 * {@link WopiSession} is ready to be used to edit/view documents
	 */
	open,
	/**
	 * {@link WopiSession} is expired because of too long opened {@link WopiSession} - can't be used for edit/view
	 * documents; will be cleanup up
	 */
	expired,
	/**
	 * {@link WopiSession} is closed by user interaction - can't be used for edit/view documents; will be cleaned up
	 */
	closed
}
