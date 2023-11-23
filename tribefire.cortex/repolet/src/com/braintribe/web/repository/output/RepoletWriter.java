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
package com.braintribe.web.repository.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

/**
 * Defines a writer of Repolet responses
 * 
 * @param <T>
 */
public interface RepoletWriter<T extends Writer> {

	/**
	 * Writes to the given {@code writer} an enumeration of {@code entries} available in the given {@code path}
	 * @param path
	 * @param breadCrumbs
	 * @param entries
	 * @param writer
	 * @throws IOException
	 */
	void writeList(String path, Collection<BreadCrumb> breadCrumbs, Collection<String> entries, T writer, Map<String, Object> attributes) throws IOException;

	/**
	 * Writes to the given {@code writer} a 404 page for the {@code path} that was not found 
	 * @param path
	 * @param printInspectedPaths
	 * @param inspectedPaths
	 * @param writer
	 * @throws IOException
	 */
	void writeNotFound(String path, boolean printInspectedPaths, Collection<String> inspectedPaths, T writer, Map<String, Object> attributes) throws IOException;
	 
}
