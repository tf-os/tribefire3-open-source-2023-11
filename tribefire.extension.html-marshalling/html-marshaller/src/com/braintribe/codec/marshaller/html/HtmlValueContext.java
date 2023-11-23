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
package com.braintribe.codec.marshaller.html;

import java.util.IdentityHashMap;
import java.util.Map;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.html.model.HtmlValue;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;

public class HtmlValueContext {
	private final Map<Object, HtmlValue> visitedObjects = new IdentityHashMap<>();
	private GenericModelType currentlyExpectedType;
	private GmSerializationOptions options;
	
	public HtmlValueContext(GmSerializationOptions options) {
		this.options = options;
	}

	public HtmlValue getVisitedObject(Object object) {
		return visitedObjects.get(object);
	}
	
	public <T extends HtmlValue> T registerObject(Object value, EntityType<T> entityType) {
		T htmlValue = entityType.create();
		
		if (value != null)
			visitedObjects.put(value, htmlValue);
		
		enrichHtmlValue(value, htmlValue, this);
		
		return htmlValue;
	}
	
	HtmlValueContext expectingType(GenericModelType type) {
		currentlyExpectedType = type;
		return this;
	}
	
	public GenericModelType getCurrentlyExpectedType() {
		return currentlyExpectedType;
	}
	
	public static void enrichHtmlValue(Object value, HtmlValue htmlValue, HtmlValueContext context) {
		htmlValue.setValue(value);
		String typeSignature = GMF.getTypeReflection().getType(value).getTypeSignature();
		htmlValue.setTypeSignature(typeSignature);
		htmlValue.setExpectedTypeSignature(context.currentlyExpectedType.getTypeSignature());
	}
	
	public GmSerializationOptions getOptions() {
		return options;
	}
}
