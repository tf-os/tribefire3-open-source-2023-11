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
package com.braintribe.model.generic.template;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

/**
 * <h3>Example</h3> "Hello ${name}"
 * <p>
 * consists of two fragments:
 * <ol>
 * <li>"Hello "
 * <li>"name"
 * </ol>
 * 
 * First is plain text, second is a {@link #isPlaceholder() placeholder}.
 */
@JsType(namespace = GmCoreApiInteropNamespaces.template)
public interface TemplateFragment {

	/**
	 * Placeholder fragment is created from the content of a placeholder expression, e.g. "name" in "Hello ${name}".
	 */
	boolean isPlaceholder();

	/**
	 * The text of either a static template part or the text content of a placeholder expression such as ${name}
	 */
	String getText();
}
