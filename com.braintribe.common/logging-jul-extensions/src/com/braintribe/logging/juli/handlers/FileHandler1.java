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
package com.braintribe.logging.juli.handlers;

import com.braintribe.logging.juli.formatters.simple.SimpleFormatter1;

/**
 * Extends {@link FileHandler} without adding any new methods or features. This class is used as a work-around for the problem that in JUL one can
 * only configure handlers by type (and not by instance). This is similar to {@link SimpleFormatter1}, except that for JULI this is not needed (since
 * JULI supports multiple handler configs, but JUL doesn't.)
 */
public class FileHandler1 extends FileHandler {
	// no additional methods or features
}
