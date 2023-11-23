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
package com.braintribe.xml.parser;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.utils.xml.parser.sax.SaxParserContentHandler;
import com.braintribe.xml.parser.experts.ContentExpert;
import com.braintribe.xml.parser.experts.SkippingExpert;
import com.braintribe.xml.parser.registry.ContentExpertRegistry;

public class ContentHandler<T> extends SaxParserContentHandler {
	private static Logger log = Logger.getLogger(ContentHandler.class);
	private ContentExpertRegistry registry;
	private Stack<ContentExpert> stack = new Stack<ContentExpert>();
	private Stack<String> pathStack = new Stack<String>();

	private Object result;
	
	@Configurable @Required
	public void setRegistry(ContentExpertRegistry registry) {
		this.registry = registry;
	}
	

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		// path 			
		String path = push(qName);
		
		// expert 
		ContentExpert parent = null;
		if (!stack.isEmpty() ) {
			parent = stack.peek();
		}		
		
		ContentExpert expert;
		if (parent instanceof SkippingExpert == false) { 
			expert = registry.getExpertForTag( path);		
			if (expert == null) {
				expert = new SkippingExpert();
				String msg = "no expert found for tag [" + qName + "], skipping";
				if (log.isDebugEnabled()) {
					log.debug( msg);				
				}			
			}
		}
		else {
			// if parent's a skipping expert, the child must be one too
			expert = new SkippingExpert();
		}
		expert.setTag(qName);						
		expert.startElement( parent, uri, localName, qName, atts);
		stack.push(expert);
	}




	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		pop( qName);
		ContentExpert expert = stack.pop();
		if (stack.isEmpty() == false) {
			expert.endElement(stack.peek(), uri, localName, qName);
		} else {
			expert.endElement(null, uri, localName, qName);
			result = expert.getPayload();
		}						
	}



	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		stack.peek().characters(ch, start, length);
	}
	
	@SuppressWarnings("unchecked")
	public T getResult() {
		return (T) result;
	}


	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// questionable whether it should be that long.. 
		String path = peek() + "?" + target;
		
		// expert 
		ContentExpert parent = null;
		if (!stack.isEmpty() ) {
			parent = stack.peek();
		}		
		
		ContentExpert expert;
		if (parent instanceof SkippingExpert == false) { 
			expert = registry.getExpertForTag( path);		
			if (expert == null) {
				expert = new SkippingExpert();
				String msg = "no expert found for tag [?" + target + "], skipping";
				if (log.isDebugEnabled()) {
					log.debug( msg);				
				}			
			}
		}
		else {
			// if parent's a skipping expert, the child must be one too
			expert = new SkippingExpert();
		}
		expert.setTag(target);						
		
		expert.startElement( parent, null, target, target, null);
		expert.characters(data.toCharArray(), 0, data.length());
		
		if (stack.isEmpty() == false) {
			expert.endElement(stack.peek(), null, target, target);
		} else {
			expert.endElement(null, null, target, target);
			result = expert.getPayload();
		}									
	}
	
	
	public String push(String qName) {
		String path = null;
		if (pathStack.empty()) {
			path = qName;
		} 
		else {
			path = pathStack.peek() + "/" + qName;
		}		
		pathStack.push(path);
		return path;
	}
	

	public void pop(String qname) {
		pathStack.pop();
	}

	public String peek() {
		if (pathStack.isEmpty()) 
			return "";
		else 
			return pathStack.peek() + "/";
	}
	
	
}
