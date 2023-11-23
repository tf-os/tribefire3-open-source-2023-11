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
package com.braintribe.utils.xml;

import java.util.function.Predicate;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TextIterator extends NodeIterator<Text> {

	private static class ElementFilter implements Predicate<Node> {
		private Predicate<Text> specificFilter;
		
		public ElementFilter(Predicate<Text> specificFilter) {
			super();
			this.specificFilter = specificFilter;
		}

		@Override
		public boolean test(Node node) {
			if (node instanceof Text) {
				Text text = (Text)node;
				return specificFilter == null || specificFilter.test(text);
			}
			else return false;
		}
	}
	
	public TextIterator(Element parent) {
		this(parent, (Predicate<Text>)null);
	}
	
	public TextIterator(Element parent, Predicate<Text> filter) {
		super(parent, new ElementFilter(filter));
	}
}
