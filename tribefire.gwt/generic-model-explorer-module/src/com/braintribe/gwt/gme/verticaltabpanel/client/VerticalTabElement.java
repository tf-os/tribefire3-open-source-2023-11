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
package com.braintribe.gwt.gme.verticaltabpanel.client;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;


import com.braintribe.gwt.action.client.Action;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.user.client.ui.Widget;

/**
 * Model containing info on each element in the {@link VerticalTabPanel}.
 * @author michel.docouto
 *
 */
public class VerticalTabElement {
	
	private String name;
	private String text;
	private String description;
	private Supplier<? extends Widget> widgetSupplier;
	private Widget widget;
	private ResourcePrototype icon;
	private Object modelObject;
	private boolean staticElement;
	private List<Action> tabElementActions;
	private WorkbenchActionContext<?> workbenchActionContext;
	private boolean visible = true;
	private boolean systemVisible = true;	
	private boolean marked = false;
	private boolean systemConfigurable = false;	
	private Element element;
	private boolean denyIconUpdate = false;	
	private boolean denyDescriptionUpdate = false;
	private String id; //RVE generated on create element
	private String additionalText;
	private boolean additionalaShow = false;
	private String additionalClass;
	private Random random;
	
	public VerticalTabElement(String name, String text, String description, Supplier<? extends Widget> widgetSupplier, ResourcePrototype icon, Object modelObject, boolean staticElement,
			WorkbenchActionContext<?> workbenchActionContext) {
		setName(name);
		setText(text);
		setDescription(description);
		setWidgetSupplier(widgetSupplier);
		setIcon(icon);
		setModelObject(modelObject);
		setStatic(staticElement);
		setWorkbenchActionContext(workbenchActionContext);		
		generateId();
	}

	public VerticalTabElement(String name, String text, String description, ResourcePrototype icon, Object modelObject, boolean staticElement,
			WorkbenchActionContext<?> workbenchActionContext, Widget widget) {
		setName(name);
		setText(text);
		setDescription(description);
		setWidget(widget);
		setIcon(icon);
		setModelObject(modelObject);
		setStatic(staticElement);
		setWorkbenchActionContext(workbenchActionContext);
		generateId();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}	
	
	public Widget getWidget() {
		if (widget != null)
			return widget;
		
		if (widgetSupplier != null)
			widget = widgetSupplier.get();
		return widget;
	}
	
	public Supplier<? extends Widget> getWidgetSupplier() {
		return widgetSupplier;
	}
	
	public Widget getWidgetIfSupplied() {
		return widget;
	}
	
	public void setWidgetSupplier(Supplier<? extends Widget> widgetSupplier) {
		this.widgetSupplier = widgetSupplier;
	}
	
	public ResourcePrototype getIcon() {
		return icon;
	}
	
	public void setIcon(ResourcePrototype icon) {
		this.icon = icon;
	}
	
	public Object getModelObject() {
		return modelObject;
	}
	
	public void setModelObject(Object modelObject) {
		this.modelObject = modelObject;
	}
	
	public boolean isStatic() {
		return staticElement;
	}
	
	public void setStatic(boolean staticElement) {
		this.staticElement = staticElement;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visibleElement) {
		this.visible = visibleElement;
	}	
		
	public List<Action> getTabElementActions() {
		return tabElementActions;
	}
	
	public void setTabElementActions(List<Action> tabElementActions) {
		this.tabElementActions = tabElementActions;
	}
	
	public WorkbenchActionContext<?> getWorkbenchActionContext() {
		return workbenchActionContext;
	}
	
	public void setWorkbenchActionContext(WorkbenchActionContext<?> workbenchActionContext) {
		this.workbenchActionContext = workbenchActionContext;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public void setElement(Element element) {
		this.element = element;
	}
	
	public Element getElement() {
		return element;
	}

	public Boolean isSystemVisible() {
		return systemVisible;
	}

	public void setSystemVisible(Boolean systemVisible) {
		this.systemVisible = systemVisible;
	}

	public Boolean isSystemConfigurable() {
		return systemConfigurable;
	}

	public void setSystemConfigurable(Boolean systemConfigurable) {
		this.systemConfigurable = systemConfigurable;
	}

	public boolean getDenyIconUpdate() {
		return denyIconUpdate;
	}

	public void setDenyIconUpdate(boolean denyUpdate) {
		this.denyIconUpdate = denyUpdate;
	}

	public boolean getDenyDescriptionUpdate() {
		return denyDescriptionUpdate;
	}

	public void setDenyDescriptionUpdate(boolean denyDescriptionUpdate) {
		this.denyDescriptionUpdate = denyDescriptionUpdate;
	}
	
	public String getId() {
		return id;
	}
	
	private void generateId() {
		if (widgetSupplier != null)
			id = Integer.toString(System.identityHashCode(widgetSupplier));
		else if (modelObject != null)
			id = Integer.toString(System.identityHashCode(modelObject));
		else if (workbenchActionContext != null)
			id = Integer.toString(System.identityHashCode(modelObject));
		else {
			Random rn = getRandom();
			id = Integer.toString(rn.nextInt(999) + 1);
		}			
	}
	
	private Random getRandom() {
		if (random == null)
			random = new Random();
		
		return random;
	}

	public String getAdditionalText() {
		return additionalText;
	}

	public void setAdditionalText(String additionalText) {
		this.additionalText = additionalText;
	}

	public boolean isAdditionalaShow() {
		return additionalaShow;
	}

	public void setAdditionalaShow(boolean additionalaShow) {
		this.additionalaShow = additionalaShow;
	}

	public String getAdditionalClass() {
		return additionalClass;
	}

	public void setAdditionalClass(String additionalClass) {
		this.additionalClass = additionalClass;
	}

}
