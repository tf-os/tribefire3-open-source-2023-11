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
package com.braintribe.logging.juli.formatters.simple;

/**
 * Extends {@link SimpleFormatter} without adding any new methods or features. This class is used as a work-around for the problem that in JUL/JULI
 * one can only configure formatters by type (and not by instance). If one e.g. wants to have a verbose format with timestamps, class info, etc. for
 * the file handler and a more compact format for the console handler, this won't work when just using {@link SimpleFormatter} twice. Instead one can
 * use {@link SimpleFormatter1} and {@link SimpleFormatter2} (maximum is 10).
 */
// It seems that JUL/JULI unfortunately doesn't support inner classes. Therefore this class needs to be in its own file.
public class SimpleFormatter1 extends SimpleFormatter {
	// no additional methods or features
}
