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
package com.braintribe.model.processing.modellergraph.animation;

public class ModelGraphStateAnimationVector {
	
	private double[] components;
	
	public ModelGraphStateAnimationVector(int componentCount) {
		components = new double[componentCount];
	}
	
	public ModelGraphStateAnimationVector(double... components) {
		this.components = components;
	}
	
	public void setComponents(double[] components) {
		this.components = components;
	}
	
	public double[] getComponents() {
		return components;
	}
	
	public double getComponent(int i) {
		return components[i];
	}
	
	public void setComponent(int i, double value) {
		components[i] = value;
	}

	public ModelGraphStateAnimationVector plus(ModelGraphStateAnimationVector animationVector){
		double resultArray[] = new double[components.length];
		for(int i = 0;i < components.length; i++){			
			resultArray[i] = components[i] + animationVector.getComponent(i);
		}
		return new ModelGraphStateAnimationVector(resultArray);
	}
	
	public ModelGraphStateAnimationVector minus(ModelGraphStateAnimationVector animationVector){
		double resultArray[] = new double[components.length];
		for(int i = 0;i < components.length; i++){			
			resultArray[i] = components[i] - animationVector.getComponent(i);
		}
		return new ModelGraphStateAnimationVector(resultArray);
	}
	
	public ModelGraphStateAnimationVector times(double times){
		double resultArray[] = new double[components.length];
		for(int i = 0;i < components.length; i++){			
			resultArray[i] = components[i] * times;
		}
		return new ModelGraphStateAnimationVector(resultArray);		
	}
	
	public double abs(){
		double abs = 0;
		for(int i = 0;i < components.length; i++){	
			double componentValue = components[i];
			abs += componentValue * componentValue;
		}
		return Math.sqrt(abs);
	}
	
	public ModelGraphStateAnimationVector normalize(){
		return times(1/abs());		
	}
}
