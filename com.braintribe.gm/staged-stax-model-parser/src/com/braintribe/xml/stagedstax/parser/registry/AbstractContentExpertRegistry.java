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
package com.braintribe.xml.stagedstax.parser.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.braintribe.xml.stagedstax.parser.experts.ContentExpert;
import com.braintribe.xml.stagedstax.parser.factory.ContentExpertFactory;

public class AbstractContentExpertRegistry implements ContentExpertRegistry {
	private Map<String, ContentExpertFactory> globalTagToExpertMap = new HashMap<String, ContentExpertFactory>();
	private ContentExpertFactory rootFactory;
	private Stack<ContentExpertFactory> factoryStack = new Stack<>();
	
	@Override
	public void addExpertFactory(String tag, ContentExpertFactory factory) {
		if (tag.startsWith("*")) {
			String suspect = tag.substring(tag.indexOf('/') + 1);			
			globalTagToExpertMap.put( suspect, factory);
		}
		else {
			String [] tags = tag.split( "/");
			int len  = tags.length;
			if (len == 1) {
				rootFactory = factory;
			}
			else {
				ContentExpertFactory handlingFactory = rootFactory;
				ContentExpertFactory parentFactory = rootFactory;
				for (int i = 1; i < len-1; i++) {
					handlingFactory = parentFactory.getChainedFactory(tags[i]);
					parentFactory = handlingFactory;
				}	
				handlingFactory.chainFactory( tags[len-1], factory);
			}
		}
	}

	@Override
	public synchronized ContentExpert pushTag(String tag) {		
		ContentExpertFactory factory = null;
		
		ContentExpertFactory currentFactory = factoryStack.isEmpty() ? null : factoryStack.peek();
	
		if (currentFactory == null) {
			factoryStack.push( rootFactory);
		}		
		else {				
			// might be a global processing instruction
			if (tag.startsWith( "?")) {
			
				factory = globalTagToExpertMap.get( tag);
				if (factory != null) {
					factoryStack.push(factory);
					return factory.newInstance();
				}	
			}
			// no global, try standard expert 
			factory = currentFactory.getChainedFactory( tag);
			if (factory != null) {
				// no standard, might be a repeating expert for children 
				factoryStack.push(factory);
			}
			else {
				factory = currentFactory.getChainedFactory("*");
				if (factory != null) {
					factoryStack.push(factory);
				}
				else {
					//System.out.println("no factory for tag [" + tag + "]");
					return null;
				}
			}
		}
	
		return factoryStack.peek().newInstance();
	}

	@Override
	public void popTag(String tag) {
		factoryStack.pop();
		
	}
	
	
	
	
}
