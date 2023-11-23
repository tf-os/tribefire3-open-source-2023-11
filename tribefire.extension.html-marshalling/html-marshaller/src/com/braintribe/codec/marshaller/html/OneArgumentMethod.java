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

import java.util.List;
import java.util.function.Function;

import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public interface OneArgumentMethod<T> extends TemplateMethodModelEx {
	@Override
	default Object exec(List arguments) throws TemplateModelException {
		Object firstArgument = unwrap(arguments.get(0));
		
		return execute((T) firstArgument);
	}
	
	Object execute(T argument);
	
	static Object unwrap(Object freemarkerArgument) {
		Object originalObject;
		if (freemarkerArgument instanceof WrapperTemplateModel) {
			WrapperTemplateModel argument = (WrapperTemplateModel) freemarkerArgument;
			originalObject = argument.getWrappedObject();
		} else {
			originalObject = freemarkerArgument;
		}
		return originalObject;
	}
	
	static <T> OneArgumentMethod<T> of(Function<T, Object> impl){
		return impl::apply;
	}
}
