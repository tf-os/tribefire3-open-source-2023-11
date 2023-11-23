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
package com.braintribe.utils.template.model;

import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.utils.template.TemplateException;

public class MergeContext {
	
	private static Logger logger = Logger.getLogger(MergeContext.class);
			
	private boolean sourceMode;
	private Function<String, ?> variableProvider;
	private Function<String, String> literalEscaper;
	
	public void setLiteralEscaper(Function<String, String> literalEscaper) {
		this.literalEscaper = literalEscaper;
	}
	
	public void setVariableProvider(Function<String, ?> variableProvider) {
		this.variableProvider = variableProvider;
	}
	
	public void setSourceMode(boolean sourceMode) {
		this.sourceMode = sourceMode;
	}
	
	public boolean isSourceMode() {
		return sourceMode;
	}
	
	public Function<String, ?> getVariableProvider() {
		return variableProvider;
	}
	
	public Object getVariableValue(String variableName) throws TemplateException {
		try {
			Object result = variableProvider.apply(variableName);
			if (result == null) {
				logger.info(() -> "The variable "+variableName+" could not be resolved to an actual value.");
			}
			return result;
		} catch (RuntimeException e) {
			throw new TemplateException("error while accessing variable " + variableName , e);
		}
	}
	
	public String createSourceLiteral(String text) throws TemplateException {
		if (literalEscaper != null) {
			try {
				text = literalEscaper.apply(text);
			} catch (RuntimeException e) {
				throw new TemplateException("error while escaping literal string", e);
			}
		}
			
		return '"' + text + '"';
	}
}
