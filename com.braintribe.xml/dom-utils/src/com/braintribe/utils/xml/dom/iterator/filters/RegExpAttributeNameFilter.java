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
package com.braintribe.utils.xml.dom.iterator.filters;

import java.util.function.Predicate;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * a filter that uses the name of an attribute as filter criterion the mask is a regular expression
 * 
 * @author pit
 * 
 */
public class RegExpAttributeNameFilter implements Predicate<Element> {

	private String regExp = null;

	public RegExpAttributeNameFilter(final String expr) {
		this.regExp = expr;
	}

	@Override
	public boolean test(final Element obj) {
		final NamedNodeMap attrnodes = obj.getAttributes();
		for (int i = 0; i < attrnodes.getLength(); i++) {
			final Node attrNode = attrnodes.item(i);
			if (attrNode.getTextContent().matches(this.regExp)) {
				return true;
			}
		}
		return false;
	}
}
