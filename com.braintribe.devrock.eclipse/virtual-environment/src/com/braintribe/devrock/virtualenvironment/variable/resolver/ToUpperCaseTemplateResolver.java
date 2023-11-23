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
package com.braintribe.devrock.virtualenvironment.variable.resolver;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class ToUpperCaseTemplateResolver extends TemplateVariableResolver {
	public ToUpperCaseTemplateResolver() {
		super( "toUpper", "converts first char to uppercase");
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		String [] values = variable.getValues();
		if (values != null) {
			String [] nv = new String[ values.length];
			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				v = v.substring(0, 1).toUpperCase() + v.substring(1);
				nv[i] = v; 
			}
			variable.setValues(nv);
		}
		
		
		super.resolve(variable, context);
	}

	
}
	
