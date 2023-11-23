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
package com.braintribe.gwt.processdesigner.client.animation;

import org.vectomatic.dom.svg.OMSVGElement;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;

public class SvgElementAnimationContext {
	
	public Object element;
	public String attribute;
	public double startValue;
	public double endValue;
	public double currentValue;
	public long startTime;
	public long durationTime;
	public long freqency;
	public Timer timer;
	
	public void adapt(double normalizedTime){
		double delta = endValue - startValue;
				
		currentValue = startValue + (delta * normalizedTime);
//		System.err.println("adapting " + currentValue);
		if(element instanceof FlowPanel)
			((FlowPanel)element).getElement().setAttribute(attribute, currentValue + "");
		else if(element instanceof OMSVGElement)
			((OMSVGElement)element).setAttribute(attribute, currentValue + "");
	}

}
