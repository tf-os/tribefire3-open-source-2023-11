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
package com.braintribe.gwt.gmview.client;

 import java.util.List;

import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.workbench.InstantiationAction;
import com.google.gwt.user.client.ui.Widget;

 public class InstantiationData {

 	private GenericModelType type;
 	private List<InstantiationAction> instantiationActions;
	private ModelPathElement pathElement;
	private boolean showGima;
	private boolean isFreeInstantiation;
	private String namePrefix;
	private String gimaTitle;
	private Widget parentWidget;
	private boolean handlingAdd;
	private boolean isTransient;

	public InstantiationData(ModelPathElement pathElement, boolean showGima, boolean isFreeInstantiation, String namePrefix, boolean handlingAdd, boolean isTransient) {
		this.pathElement = pathElement;
		this.showGima = showGima;
		this.isFreeInstantiation = isFreeInstantiation;
		this.namePrefix = namePrefix;
		this.handlingAdd = handlingAdd;
		this.isTransient = isTransient;
	}

	public InstantiationData(GenericModelType type, List<InstantiationAction> instantiationActions, boolean showGima, boolean isFreeInstantiation,
			String namePrefix, String gimaTitle, boolean handlingAdd, boolean isTransient) {
		this.type = type;
		this.instantiationActions = instantiationActions;
		this.showGima = showGima;
		this.isFreeInstantiation = isFreeInstantiation;
		this.namePrefix = namePrefix;
		this.gimaTitle = gimaTitle;
		this.handlingAdd = handlingAdd;
		this.isTransient = isTransient;
	}

 	public GenericModelType getType() {
		return type;
	}
 	
 	public List<InstantiationAction> getInstantiationActions() {
		return instantiationActions;
	}
 	
 	public void setPathElement(ModelPathElement pathElement) {
		this.pathElement = pathElement;
	}

 	public ModelPathElement getPathElement() {
		return pathElement;
	}

 	public boolean isShowGima() {
		return showGima;
	}

 	public boolean isFreeInstantiation() {
		return isFreeInstantiation;
	}

 	public String getNamePrefix() {
		return namePrefix;
	}

 	public String getGimaTitle() {
		return gimaTitle;
	}
 	
 	public void setParentWidget(Widget parentWidget) {
		this.parentWidget = parentWidget;
	}

 	public Widget getParentWidget() {
		return parentWidget;
	}
 	
 	public void setHandlingAdd(boolean handlingAdd) {
		this.handlingAdd = handlingAdd;
	}
 	
 	public boolean isHandlingAdd() {
		return handlingAdd;
	}
 	
 	public boolean isTransient() {
		return isTransient;
	}

 }