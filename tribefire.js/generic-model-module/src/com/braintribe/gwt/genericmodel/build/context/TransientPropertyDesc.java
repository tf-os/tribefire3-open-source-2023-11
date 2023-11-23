// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build.context;

import com.braintribe.model.generic.reflection.GmtsTransientProperty;

/**
 * @author peter.gazdik
 */
public class TransientPropertyDesc {

	public String name;
	public String Name;
	public String originalType;
	public EntityDesc ownerTypeDesc;

	public String getName() {
		return name;
	}

	public boolean getIsInheritedFromSuperclass() {
		return ownerTypeDesc.isTransientInheritedFromSuperclass(name);
	}

	public boolean getIsInherited() {
		return ownerTypeDesc.isTransientInherited(name);
	}

	public String getOriginalType() {
		return originalType;
	}

	public String getSetterName() {
		return "set" + Name;
	}

	public String getGetterName() {
		return "get" + Name;
	}

	public String getConstructorSource() {
		final String template = "new %s(INSTANCE, \"%s\", %s.class, %s, %s)";
		final String implClass = GmtsTransientProperty.class.getName();

		String getter = "e -> ((" + ownerTypeDesc.getEntityIfaceFullName() + ")e)." + getGetterName() + "()";
		String setter = "(e, v) -> ((" + ownerTypeDesc.getEntityIfaceFullName() + ")e)." + getSetterName() + "((" + originalType + ")v)";

		return String.format(template, implClass, name, originalType, getter, setter);
	}

}
