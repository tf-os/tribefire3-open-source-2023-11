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

public class ColorFactoryBean implements FactoryBean {
	private int alpha = 255;
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	
	public void setRed(int red) {
		this.red = red;
	}
	
	public void setGreen(int green) {
		this.green = green;
	}
	
	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public void setRedFloat(float red) {
		this.red = (int)(red * 255 + 0.5);
	}
	
	public void setGreenFloat(float green) {
		this.green = (int)(green * 255 + 0.5);;
	}
	
	public void setBlueFloat(float blue) {
		this.blue = (int)(blue * 255 + 0.5);;
	}
	
	public void setAlphaFloat(float alpha) {
		this.alpha = (int)(alpha * 255 + 0.5);;
	}
	
	public void setValue(int value) {
		setValueWithAlpha(value | 0xff000000);
	}
	
	public void setValueWithAlpha(int value) {
		alpha = (value >> 24) & 0xFF;
		red = (value >> 16) & 0xFF;
		green = (value >> 8) & 0xFF;
		blue = (value >> 0) & 0xFF;
	}
	
	public Object getObject() throws Exception {
		return new Color(red, green, blue, alpha);
	}
	
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return Color.class;
	}
	
	public boolean isSingleton() {
		return true;
	}
}
