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
package org.apache.juli;

import java.util.logging.Logger;

import org.apache.juli.ClassLoaderLogManager.ClassLoaderLogInfo;
import org.apache.juli.ClassLoaderLogManager.LogNode;

/**
 * Simple helper class used to access package-visible members of classes in the <code>org.apache.juli</code> package.
 *
 * @author michael.lafite
 */
public class PackageVisibilityAccessHelper {

	public static ClassLoaderLogInfo invokeClassLoaderLogInfoConstructor(final LogNode rootNode) {
		return new ClassLoaderLogInfo(rootNode);
	}

	public static LogNode invokeLogNodeConstructor(final LogNode parent, final Logger logger) {
		return new LogNode(parent, logger);
	}
}
