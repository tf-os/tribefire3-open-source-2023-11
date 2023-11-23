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
package com.braintribe.gwt.template.client;

import com.braintribe.gwt.template.client.model.MergeContext;
import com.braintribe.gwt.template.client.model.Sequence;
import com.braintribe.gwt.template.client.model.StaticText;
import com.braintribe.gwt.template.client.model.TemplateNode;
import com.braintribe.gwt.template.client.model.Variable;

public class Template {
	private TemplateNode rootNode;
	
	public Template(TemplateNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public String merge(MergeContext context) throws TemplateException {
		StringBuilder builder = new StringBuilder();
		
		rootNode.merge(builder, context);
		return builder.toString();
	}
	
	public static Template parse(String template) {
		Sequence sequence = new Sequence();
		
		int index = 0;
		
		while (index < template.length()) {
			int start = findNextPropertyStart(template, index);
			if (start != -1) {
				sequence.add(new StaticText(template.substring(index, start).replace("$${", "${")));
				index = start;
				int end = template.indexOf('}', start);
				if (end != -1) {
					String propertyName = template.substring(start + 2, end);
					sequence.add(new Variable(propertyName));
					index = end + 1;
				}
				else {
					sequence.add(new StaticText(template.substring(index).replace("$${", "${")));
					break;
				}
			}
			else {
				// end here
				sequence.add(new StaticText(template.substring(index).replace("$${", "${")));
				break;
			}
		}
		
		return new Template(sequence);
	}
	
	protected static int findNextPropertyStart(String str, int start) {
		int index = start;
		while (index < str.length()) {
			index = str.indexOf('$', index);
			if (index == -1) return -1;
			int previousIndex = index - 1;
			int nextIndex = index + 1;
			if (
					(previousIndex < start || str.charAt(previousIndex) != '$') &&  
					nextIndex < str.length() && 
					str.charAt(nextIndex) == '{'
				) {
				
				return index;
			}
			index++;
		}
		
		return -1;
	}

}
