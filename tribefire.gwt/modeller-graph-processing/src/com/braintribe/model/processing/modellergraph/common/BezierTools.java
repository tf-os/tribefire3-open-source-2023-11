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
package com.braintribe.model.processing.modellergraph.common;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.Node;

public class BezierTools {
	
//	private static BezierTools instance = new BezierTools();
//	
//	public static BezierTools get(){
//		return instance;
//	}
	
	public BezierTools(){
		
	}
	
	//private ModelGraphConfigurationsNew modelGraphConfigurations;
	private List<Float> atmoshperesRadii = new ArrayList<>();
	
	public void setAtmoshperesRadii(List<Float> atmoshperesRadii) {
		this.atmoshperesRadii = atmoshperesRadii;
	}
	
	public Edge createCubicBezierCurve(Node fromNode, Node toNode, Complex focusEntityStartingPoint, Complex startPoint, Complex endPoint, double focusEntityRadius, double startRadius, double endRadius, boolean firstLevel, boolean isSibling, boolean circular, double order){
		Edge edge = Edge.T.create();
		
		edge.setFromNode(fromNode);
		edge.setToNode(toNode);
		edge.setOrder(order);
		
		Complex connectionCentre = Complex.getCentre(startPoint, endPoint);
		Complex directedConnectionCentre = connectionCentre.minus(focusEntityStartingPoint);
		
		float nearestAtmopshereRadius = 0;
		if(!isSibling)
			nearestAtmopshereRadius = getNearestAtmosphereRadius(focusEntityStartingPoint, startPoint,endPoint, directedConnectionCentre);
		else {
			if(atmoshperesRadii.size() > 0)
				nearestAtmopshereRadius = atmoshperesRadii.get(atmoshperesRadii.size()-1);
		}
		Complex atmosphereIntersectionPoint = null;
		
		if(focusEntityStartingPoint.nearBy(connectionCentre, 1)){
			Complex direction = endPoint.minus(startPoint).normalize();
			Complex perpDirection = new Complex(direction.y, -direction.x);
			atmosphereIntersectionPoint = focusEntityStartingPoint.plus(perpDirection.times(atmoshperesRadii.size() > 0 ? atmoshperesRadii.get(0) : 1));
		}else
			atmosphereIntersectionPoint = focusEntityStartingPoint.plus(directedConnectionCentre.normalize().times(nearestAtmopshereRadius));
		
		Complex directedAtmosphereIntersectionPoint = focusEntityStartingPoint.minus(atmosphereIntersectionPoint);
		
		startPoint = startPoint.plus(atmosphereIntersectionPoint.minus(startPoint).normalize().times(firstLevel ? focusEntityRadius : startRadius));
		endPoint = endPoint.plus(atmosphereIntersectionPoint.minus(endPoint).normalize().times(endRadius));
		
		edge.setStart(Complex.getPoint(startPoint));
		edge.setEnd(Complex.getPoint(endPoint));
		
		Complex curve1Direction = startPoint.minus(endPoint.minus(atmosphereIntersectionPoint).normalize().times(1.5));
		Complex curve2Direction = endPoint.minus(startPoint.minus(atmosphereIntersectionPoint).normalize().times(1.5));	
		
		Complex circularCenterDirection = atmosphereIntersectionPoint.minus(startPoint).normalize();
		Complex controlCompound1 = new Complex(circularCenterDirection.y, -circularCenterDirection.x);
		Complex controlCompound2 = new Complex(-circularCenterDirection.y, circularCenterDirection.x);
		
		Complex nodeCenter = Complex.getComplex(fromNode.getCenter());
		Complex direction = nodeCenter.minus(focusEntityStartingPoint);		
		Complex circlePoint = nodeCenter.plus(direction.normalize().times(fromNode.getRadius()));
		if(circular){
			edge.setStart(Complex.getPoint(circlePoint));
			edge.setEnd(Complex.getPoint(circlePoint));
			edge.setStartControl(Complex.getPoint(circlePoint.plus(controlCompound1.times(12))));
			edge.setEndControl(Complex.getPoint(circlePoint.plus(controlCompound2.times(12))));
		}else{
			edge.setStartControl(Complex.getPoint(curve1Direction));
			edge.setEndControl(Complex.getPoint(curve2Direction));
		}
		
		Complex perpAtmosphereIntersectionPoint = new Complex(directedAtmosphereIntersectionPoint.y, -directedAtmosphereIntersectionPoint.x);
		
		float bezierControlFactor = firstLevel == true ? 1 : (float)(endPoint.minus(startPoint).abs() * 0.2);
		Complex atmosphereControlPoint1 = atmosphereIntersectionPoint.minus(perpAtmosphereIntersectionPoint.normalize().times(bezierControlFactor));
		Complex atmosphereControlPoint2 = atmosphereIntersectionPoint.plus(perpAtmosphereIntersectionPoint.normalize().times(bezierControlFactor));
		
		if(atmosphereControlPoint1.minus(curve1Direction).abs() > atmosphereControlPoint2.minus(curve1Direction).abs()){
			Complex tempComplex = atmosphereControlPoint2;
			atmosphereControlPoint2 = atmosphereControlPoint1;
			atmosphereControlPoint1 = tempComplex;
		}		
		
		if(!circular){
			edge.setTurning(Complex.getPoint(atmosphereIntersectionPoint));
		}
		else{
//			Complex fakeControlPoint = startPoint.plus(atmosphereIntersectionPoint.minus(startPoint).normalize().times(35));
//			edge.setTurning(Complex.getPoint(fakeControlPoint));
//			edge.setTurning(Complex.getPoint(circlePoint.plus(direction.normalize().times(25))));
			edge.setTurning(Complex.getPoint(circlePoint));
		}
	
		return edge;		
	}
	
	public float getNearestAtmosphereRadius(Complex focusEntityStartingPoint, Complex p1, Complex p2, Complex directedIntersectionPoint){
		int j = 0;
		float distance = 0;
		Complex intersectionMiddlePoint = Complex.getCentre(p1, p2);
		for(int i = 0;i<atmoshperesRadii.size()-1;i++){			
			Complex atmosphereIntersectionPoint = focusEntityStartingPoint.plus(directedIntersectionPoint.normalize().times(atmoshperesRadii.get(i)));
			float newDistance = (float)intersectionMiddlePoint.minus(atmosphereIntersectionPoint).abs();
			if(distance == 0)
				distance = newDistance;
			if(newDistance < distance){
				distance = newDistance;
				j = i;
			}
		}
		return !atmoshperesRadii.isEmpty() ? atmoshperesRadii.get(j) : 0;
	}

}
