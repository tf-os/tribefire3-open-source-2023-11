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
package com.braintribe.spring.support;

import java.awt.Color;

import org.springframework.beans.factory.FactoryBean;

public class GradientColorFactoryBean implements FactoryBean {
	private Color startColor = Color.black;
	private Color endColor = Color.white;
	private float gradient = 0.5f;
	
	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}
	
	public void setEndColor(Color endColor) {
		this.endColor = endColor;
	}
	
	public void setGradient(float gradient) {
		this.gradient = gradient;
	}
	
	public Object getObject() throws Exception {
		float[] startValues = startColor.getRGBComponents(null); 
		float[] endValues = endColor.getRGBComponents(null);
		
		float[] gradientValues = new float[startValues.length];
		
		for (int i = 0; i < gradientValues.length; i++) {
			float s = startValues[i];
			float e = endValues[i];
			float g = s + (e - s) * gradient;
			gradientValues[i] = g;
		}
		
		return new Color(gradientValues[0], gradientValues[1], gradientValues[2], gradientValues[3]);
	}
	
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return Color.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}
