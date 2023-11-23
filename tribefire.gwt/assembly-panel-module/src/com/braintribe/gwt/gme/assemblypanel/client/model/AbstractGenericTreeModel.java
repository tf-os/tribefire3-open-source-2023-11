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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil.ValueDescriptionBean;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.shared.FastMap;

public abstract class AbstractGenericTreeModel {
	private static Long lastId = 0l;
	protected Object modelObject;
	protected Property property;
	protected boolean notCompleted;
	protected List<AbstractGenericTreeModel> children;
	protected AbstractGenericTreeModel parent;
	protected Map<String, Object> extraProperties = new FastMap<>();
	protected Long id;
	private ValueDescriptionBean label;
	private ImageResource icon;
	
	public AbstractGenericTreeModel() {
		id = lastId++;
	}
	
	public Long getId() {
		return id;
	}
	
	public abstract AbstractGenericTreeModel getDelegate();
	
	public void setModelObject(Object modelObject, @SuppressWarnings("unused") int depth) {
		this.modelObject = modelObject;
	}
	
	public <X> X getModelObject() {
		return (X) modelObject;
	}
	
	public void setProperty(Property property) {
		this.property = property;
	}
	
	public Property getProperty() {
		return property;
	}
	
	public String getPropertyName() {
		return property != null ? property.getName() : null;
	}
	
	public abstract <X extends GenericModelType> X getElementType();
	
	/**
	 * Returns true if this TreeModel refers to the given object.
	 */
	public boolean refersTo(Object object) {
		if (modelObject == object)
			return true;
		
		if (!(object instanceof EnhancedCollection))
			return false;
		
		if (object instanceof EnhancedList) {
			if (modelObject == ((EnhancedList<?>) object).getDelegate())
				return true;
		} else if (object instanceof EnhancedSet) {
			if (modelObject == ((EnhancedSet<?>) object).getDelegate())
				return true;
		} else if (object instanceof EnhancedMap) {
			if (modelObject == ((EnhancedMap<?,?>) object).getDelegate())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether this model children were prepared already.
	 */
	public boolean getNotCompleted() {
		return notCompleted;
	}
	
	public List<AbstractGenericTreeModel> getChildren() {
		return children;
	}
	
	public void setChildren(List<AbstractGenericTreeModel> children) {
		this.children = children;
	}
	
	public AbstractGenericTreeModel getParent() {
		return parent;
	}
	
	public void setParent(AbstractGenericTreeModel parent) {
		this.parent = parent;
	}
	
	public int getChildCount() {
		return children == null ? 0 : children.size();
	}
	
	public AbstractGenericTreeModel getChild(int index) {
		if ((index < 0) || (index >= children.size()))
			return null;
	    return children.get(index);
	}
	
	public void add(AbstractGenericTreeModel child) {
		if (children == null)
			children = new ArrayList<>();
		
		children.add(child);
		child.setParent(this);
	}
	
	public int indexOf(AbstractGenericTreeModel child) {
		if (children != null)
			return children.indexOf(child);
		
		return -1;
	}
	
	public void insert(AbstractGenericTreeModel child, int index) {
		if (children == null)
			children = new ArrayList<>();
		children.add(index, child);
		child.setParent(this);
	}
	
	public boolean remove(AbstractGenericTreeModel child) {
		if (children != null) {
			boolean removed = children.remove(child);
			if (removed)
				child.setParent(null);
			return removed;
		}
		
		return false;
	}
	
	public boolean remove(int index) {
		if (index >= 0 && index < getChildCount())
			return remove(getChild(index));
		
		return false;
	}
	
	public void clear() {
		if (children != null) {
			children.forEach(child -> child.setParent(null));
			children.clear();
		}
	}
	
	public <X> X set(String property, X value) {
		return (X) extraProperties.put(property, value);
	}
	
	public <X> X get(String property) {
		return (X) extraProperties.get(property);
	}
	
	public TreePropertyModel getTreePropertyModel(@SuppressWarnings("unused") Property property) {
		return null;
	}
	
	public EntityTreeModel getEntityTreeModel() {
		return null;
	}
	
	public ValueDescriptionBean getLabel() {
		return label;
	}
	
	public void setLabel(ValueDescriptionBean label) {
		this.label = label;
	}
	
	public boolean isCollectionTreeModel() {
		return false;
	}
	
	public ImageResource getIcon() {
		return icon;
	}
	
	public void setIcon(ImageResource icon) {
		this.icon = icon;
	}
	
}
