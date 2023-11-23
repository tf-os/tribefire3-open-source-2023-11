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
package com.braintribe.utils.velocity;


import com.braintribe.common.lcd.transformer.Transformer;
import com.braintribe.common.lcd.transformer.TransformerException;
import com.braintribe.utils.Not;

/**
 * A simple {@link Transformer} that renders velocity templates using {@link SimpleVelocityTemplateRenderer}.
 * 
 * @author michael.lafite
 */
public class SimpleVelocityTemplateTransformer implements Transformer<String, String, Object> {

	private SimpleVelocityTemplateRenderer templateRenderer;

	public SimpleVelocityTemplateTransformer() {
		// nothing to do
	}
	
	public SimpleVelocityTemplateRenderer getTemplateRenderer() {
		if (this.templateRenderer == null) {
			this.templateRenderer = new SimpleVelocityTemplateRenderer();
		}
		return Not.Null(this.templateRenderer);
	}

	public void setTemplateRenderer(final SimpleVelocityTemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
	}

	
	@Override
	public String transform(final String input, final Object transformationContext) throws TransformerException {
		if (input == null) {
			throw new TransformerException("The passed input must not be null!");
		}

		getTemplateRenderer().setTemplateProvider(k -> input);
		return getTemplateRenderer().apply(transformationContext);
	}
}
