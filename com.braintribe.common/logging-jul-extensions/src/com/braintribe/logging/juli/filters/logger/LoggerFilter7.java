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
package com.braintribe.logging.juli.filters.logger;

import com.braintribe.logging.juli.formatters.simple.SimpleFormatter1;

/**
 * Extends {@link LoggerFilter} without adding any new methods or features. The purpose of this extension is to be able to configure multiple
 * <code>LoggerFilter</code> instances (with different settings). This is the same work-around as used by {@link SimpleFormatter1}.
 */
public class LoggerFilter7 extends LoggerFilter {
	// Intentionally left empty
}
