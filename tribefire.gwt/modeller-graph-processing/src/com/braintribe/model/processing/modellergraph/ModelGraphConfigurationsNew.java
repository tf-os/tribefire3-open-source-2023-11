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
package com.braintribe.model.processing.modellergraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellergraph.condensed.CondensedType;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

public class ModelGraphConfigurationsNew{
	
	public boolean useAnimation = true;
	public boolean debugMode = false;
//	public boolean useMapper = false;
//	public boolean expertMode = false;
//	public boolean greyScale = false;
//	public int maxOrder = 3;
//	public int maxChildCount = 16;
	public int currentPage = 0;
	public ModellerView modellerView;
	
	public int circleStrokeWidth = 4;
	public int edgeStrokeWidth = 2;
	public int arrowWidth = 5;
	public int arrowHeight = 10;
	public int circleRadius = 8;
	public double focusEntityRadiusFactor = 0.15;
	public double childEntityRadiusFactor = 0.7;
	public double detailRadiusFactor = 0.15;
	public double childEntityOffset = 10;
	
	public int atmoshpereCount = 5;
	public List<Float> atmoshperesRadii;
		
	public Complex viewPortDimension;
	public Complex focusedTypeCenter;	
	public double focusedTypeRadius;
	public double childTypeRadius;
	public double detailRadius;
	public Complex childTypeCenter;
	public double focusedToChildTypeDistance;
	public double perimeter;
	public Complex leftDetailPoint;
	public Complex rightDetailPoint;
	
	public GmModellerMode modellerMode = GmModellerMode.condensed;
//	public DeviceKind deviceKind = DeviceKind.desktop;
	
	public String fontFamily = "Open Sans";
	public String fontSize = "18px";
	public String fontWeight = "normal";
	public String typeSourceFontSize = "18px";
	public int typeSourceHeight = 27;
	
	public TypesSourceUseCase currentTypeSourceUseCase = TypesSourceUseCase.goTo;
	
	public boolean isRendering = false;
	public boolean suppressRendering = false;
		
	public String currentFocusedType;
	public String currentLeftDetailType;
	public String currentRightDetailType;
	public String currentSelectedType;
	
	public Map<String, CondensedType> hiddenCondensedTypes = new HashMap<String, CondensedType>();
	
	public String doubleTypeSuffix = "---";
	
	public long startTime = 0;
	public NestedTransaction currentNestedTransaction = null;
	public GmProperty currentAddedProperty = null;
	
//	public final Set<String> addedTypes = new HashSet<String>();
	
	public ModelGraphConfigurationsNew() {
		
	}
	
	public void setViewPortDimension(Complex viewPortDimension) {
		this.viewPortDimension = viewPortDimension;
		focusedTypeCenter = new Complex(viewPortDimension.x / 2, viewPortDimension.y / 2);
		focusedTypeRadius = (float) (viewPortDimension.x * focusEntityRadiusFactor);
		childTypeRadius = (float) (focusedTypeRadius * childEntityRadiusFactor);		
		childTypeCenter = new Complex(childTypeRadius + childEntityOffset, focusedTypeCenter.y);
		detailRadius = (float) (viewPortDimension.x * detailRadiusFactor); 
		focusedToChildTypeDistance = (focusedTypeCenter.x - childTypeCenter.x);
		perimeter = 2 * Math.PI * focusedToChildTypeDistance;
		
		atmoshperesRadii = new ArrayList<>();
		long radiusStep = (long) ((focusedToChildTypeDistance - focusedTypeRadius - childTypeRadius) / atmoshpereCount);
		for(int i = 1; i < atmoshpereCount; i++) {
			atmoshperesRadii.add((float) (focusedTypeRadius + i * radiusStep));
		}
		
		leftDetailPoint = new Complex(detailRadius + 5, viewPortDimension.y / 2);
		rightDetailPoint = new Complex(viewPortDimension.x - detailRadius - 5, viewPortDimension.y / 2);
		
		fontSize = (this.viewPortDimension.x / 30) + "px";
		typeSourceFontSize = (this.viewPortDimension.x / 30) + "px";
	}

	public int getCircleStrokeWidth(double order) {
		switch ((int)order) {
		case 0: case -2:
			return 3;
		case 1:
			return 2;
		case 2:
			return 1;
		default:
			return 1;
		}
	}

	public double getChildEntityRadius(int order) {
		switch (order) {
		case 0:case -2:
			return focusedTypeRadius;
		case 1:
			return (focusedTypeRadius) * childEntityRadiusFactor;
		case 2:
			return (focusedTypeRadius *  childEntityRadiusFactor) * childEntityRadiusFactor;
		case 3:
			return ((focusedTypeRadius *  childEntityRadiusFactor) * childEntityRadiusFactor) * childEntityRadiusFactor;
		case -1:
			return detailRadius;
		default:
			return (focusedTypeRadius) * childEntityRadiusFactor;
		}
	}
}
