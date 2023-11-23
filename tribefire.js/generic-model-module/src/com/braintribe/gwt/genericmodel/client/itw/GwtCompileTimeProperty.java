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
package com.braintribe.gwt.genericmodel.client.itw;

import static com.braintribe.gwt.genericmodel.client.itw.ScriptOnlyItwTools.eval;

import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * @author peter.gazdik
 */
public class GwtCompileTimeProperty extends GwtScriptProperty {

	private EntityType<?> declaringType;
	private Supplier<?> initializerSupplier;

	public GwtCompileTimeProperty(String propertyName, boolean nullable, boolean confidential) {
		super(propertyName, nullable, confidential);
		setAccessors();
	}

	public void configure(GenericModelType type, EntityType<?> declaringType, Object initializer) {
		configure(type, declaringType, null);
		setInitializer(initializer);
	}

	public void configure(GenericModelType type, EntityType<?> declaringType, Supplier<?> initializerSupplier) {
		setPropertyType(type);
		this.declaringType = declaringType;
		this.initializerSupplier = initializerSupplier;
	}

	@Override
	public EntityType<?> getDeclaringType() {
		return declaringType;
	}

	@Override
	public Object getInitializer() {
		if (initializer == null && initializerSupplier != null) {
			initializer = initializerSupplier.get();
			initializerSupplier = null;
		}

		return initializer;
	}

	private void setAccessors() {
		String getDirectSource = "(function(e){return e." + getFieldName() + ";})";
		String setDirectSource = "(function(e,v){e." + getFieldName() + "=v;})";

		ScriptOnlyItwTools.setProperty(this, RuntimeMethodNames.instance.propertyGetDirectUnsafe(), eval(getDirectSource));
		ScriptOnlyItwTools.setProperty(this, RuntimeMethodNames.instance.propertySetDirectUnsafe(), eval(setDirectSource));
	}

}
