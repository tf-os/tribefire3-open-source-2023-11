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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.template)
public interface Template {
	List<TemplateFragment> fragments();
	
	String expression();
	
	default boolean isStaticOnly() {
		int size = fragments().size();
		return size == 0 || (size == 1 && !fragments().get(0).isPlaceholder());
	}

	default String evaluate(Function<String, String> placeholderResolver) {
		return fragments().stream() //
				.map(f -> f.isPlaceholder() ? placeholderResolver.apply(f.getText()): f.getText()) //
				.collect(Collectors.joining());
	}
	
	/**
	 * parses an expression that can contain a sequence of static text and placeholders like "static text with a ${placeholder}."
	 * @throws IllegalArgumentException if the expression has syntax errors.
	 */
	
	static Template parse(String expression) throws IllegalArgumentException {
		return TemplateParser.parse(expression);
	}
}
