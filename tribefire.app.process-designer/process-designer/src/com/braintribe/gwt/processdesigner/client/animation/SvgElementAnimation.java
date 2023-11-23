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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.user.client.Timer;

public class SvgElementAnimation {
	
	private static final SvgElementAnimation instance = new SvgElementAnimation();
	
	public static SvgElementAnimation getInstance() {
		return instance;
	}
	
	private Map<Object, SvgElementAnimationContext> animators = new HashMap<>();
	
	private SvgElementAnimation(){
		
	}
	
	public SvgElementAnimationContext startAnimation(final Object elementToAnimate, String attribute, double start, double end, long duration, long frequency){
		return startAnimation(elementToAnimate, attribute, start, end, duration, frequency, null);
	}
	
	public SvgElementAnimationContext startAnimation(final Object elementToAnimate, String attribute, double start, double end, long duration, long frequency,final AsyncCallback<Void> callback){
		SvgElementAnimationContext context = animators.get(elementToAnimate);
		if(context == null){		
			context = new SvgElementAnimationContext();
			final SvgElementAnimationContext innerContext = context;
			
			context.timer = new Timer() {
				@Override
				public void run() {
					try{
						double normalizedTime = SvgElementAnimation.getNormalizedTime(innerContext);
						if(normalizedTime >= 1){
							normalizedTime = 1;
							cancel();
							if(callback != null)
								callback.onSuccess(null);
						}
						
						double normalizedTimeSquared = normalizedTime * normalizedTime;
						double normalizedTimeCubed = normalizedTimeSquared * normalizedTime;
						
						normalizedTime = (-2*normalizedTimeCubed + 3*normalizedTimeSquared);
						
						innerContext.adapt(normalizedTime);
					}catch(Exception ex){
						if(callback != null)
							callback.onFailure(null);
					}
					
				}

			};
			
			context.element = elementToAnimate;
			context.attribute = attribute;
			context.startValue = start;
			context.currentValue = start;
			
			
			animators.put(elementToAnimate, context);
		
		}else{
			context.startValue = context.currentValue;
			context.timer.cancel();
		}
		
		context.endValue = end;
		context.startTime = System.currentTimeMillis();
		context.freqency = frequency;
		context.durationTime = duration;
		context.timer.scheduleRepeating((int)(((double)1/frequency) * 1000));
		return context;
	}
	
	public static double getNormalizedTime(SvgElementAnimationContext context) {
		long currentTime = System.currentTimeMillis();
		long delta = currentTime - context.startTime;
		double normalizedTime = delta / (double)context.durationTime;
//		System.err.println(normalizedTime);
		return normalizedTime;
	}
	

}
