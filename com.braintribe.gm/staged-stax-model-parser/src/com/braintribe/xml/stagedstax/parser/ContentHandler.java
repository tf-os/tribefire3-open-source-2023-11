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
package com.braintribe.xml.stagedstax.parser;

import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.Attributes;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.xml.stagedstax.parser.experts.ContentExpert;
import com.braintribe.xml.stagedstax.parser.experts.SkippingExpert;
import com.braintribe.xml.stagedstax.parser.registry.ContentExpertRegistry;

public class ContentHandler<T>  {
	private static Logger log = Logger.getLogger(ContentHandler.class);
	private ContentExpertRegistry registry;
	private Stack<ContentExpert> stack = new Stack<ContentExpert>();

	private Object result;
	
	
	@Configurable @Required
	public void setRegistry(ContentExpertRegistry registry) {
		this.registry = registry;
	}
	
	
	public void read( XMLStreamReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					startElement( reader.getNamespaceURI(), reader.getLocalName(), reader.getName().getLocalPart(), null);
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					endElement( reader.getNamespaceURI(), reader.getLocalName(), reader.getName().toString());
					break;
				}
				
				case XMLStreamConstants.CHARACTERS : {
					characters( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				}
				
				case XMLStreamConstants.PROCESSING_INSTRUCTION: {
					processingInstruction( reader.getPITarget(), reader.getPIData());
					break;
				}
				default: 
					break;
			}
			reader.next();
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) {
	
		// expert 
		ContentExpert parent = null;
		if (!stack.isEmpty() ) {
			parent = stack.peek();
		}		
		
		ContentExpert expert;
		if (parent instanceof SkippingExpert == false) { 
			expert = registry.pushTag( qName);		
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




	
	public void endElement(String uri, String localName, String qName) {
		
		ContentExpert expert = stack.pop();
		if (expert instanceof SkippingExpert == false) { 
			registry.popTag(qName);
		}
		if (stack.isEmpty() == false) {
			expert.endElement(stack.peek(), uri, localName, qName);
		} else {
			expert.endElement(null, uri, localName, qName);
			result = expert.getPayload();
		}						
	}

	public void characters(char[] ch, int start, int length)  {
		stack.peek().characters(ch, start, length);
	}
	
	@SuppressWarnings("unchecked")
	public T getResult() {
		return (T) result;
	}


	public void processingInstruction(String target, String data) {
		// questionable whether it should be that long.. 
		
		// expert 
		ContentExpert parent = null;
		if (!stack.isEmpty() ) {
			parent = stack.peek();
		}		
		
		ContentExpert expert;
		if (parent instanceof SkippingExpert == false) { 
			expert = registry.pushTag( "?" + target);		
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
		if (expert instanceof SkippingExpert == false) { 
			registry.popTag(target);
		}
	}
	
	

}
