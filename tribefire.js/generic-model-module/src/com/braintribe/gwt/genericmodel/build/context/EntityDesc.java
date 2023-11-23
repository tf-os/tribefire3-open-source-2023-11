// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.genericmodel.client.itw.GwtEnhancedEntityStub;
import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * @author peter.gazdik
 */
public class EntityDesc {

	public JClassType entityIface;
	public boolean isAbstract;

	public Map<String, PropertyDesc> properties;
	public Map<String, TransientPropertyDesc> transientProperties;
	public List<Supplier<String>> nonPropertyMethodSourceSuppliers = newList();
	public List<EntityDesc> directSuperTypes = newList();
	public EntityDesc superTypeWithMostProperties;
	public String evaluatesToRef;
	public boolean hasInitializedProperty;
	public Boolean needsDecoy = null;
	public List<EntityDesc> directSubTypes = newList();
	private int nonAbstractSubTypeCount = -1;

	/**
	 * @return <tt>true</tt> iff this property is not inherited from any super-type
	 */
	public boolean introduces(String propertyName) {
		return properties.get(propertyName).declaringType == entityIface;
	}

	public boolean isInheritedFromSuperclass(String propertyName) {
		if (superTypeWithMostProperties == null)
			return false;
		else
			return superTypeWithMostProperties.properties.containsKey(propertyName);
	}

	public boolean isTransientInheritedFromSuperclass(String propertyName) {
		if (superTypeWithMostProperties == null)
			return false;
		else
			return superTypeWithMostProperties.transientProperties.containsKey(propertyName);
	}

	public boolean isTransientInherited(String propertyName) {
		for (EntityDesc superDesc : getDirectSuperTypes())
			if (superDesc.transientProperties.containsKey(propertyName))
				return true;
		return false;
	}

	// #########################################
	// ## . . . . . . . Getters . . . . . . . ##
	// #########################################

	public JClassType getEntityIface() {
		return entityIface;
	}

	public boolean getIsAbstract() {
		return isAbstract;
	}

	public boolean getNeedsDecoy() {
		if (needsDecoy == null)
			getNonAbstractSubTypeCount();

		return needsDecoy;
	}

	public List<EntityDesc> getDirectSuperTypes() {
		return directSuperTypes;
	}

	public EntityDesc getSuperTypeWithMostProperties() {
		return superTypeWithMostProperties;
	}

	public String getEntityTypeSingletonRef() {
		return getEntityTypeClassFullName() + ".INSTANCE";
	}

	public String getEntityTypeSingletonJsniRef() {
		return "@" + getEntityTypeClassFullName() + "::INSTANCE";
	}

	public String getEnhancedSuperClassFullName() {
		if (superTypeWithMostProperties != null) {
			return superTypeWithMostProperties.getEnhancedClassFullName();
		} else {
			return GwtEnhancedEntityStub.class.getName();
		}
	}

	public List<Supplier<String>> getNonPropertyMethodSourceSuppliers() {
		return nonPropertyMethodSourceSuppliers;
	}

	public String getEvaluatesToRef() {
		return evaluatesToRef;
	}

	public boolean getHasInitializedProperty() {
		return hasInitializedProperty;
	}

	// #########################################
	// ## . . . . . . Name Getters . . . . . .##
	// #########################################

	public String getEntityTypeClassSimpleName() {
		return getEntityIfaceSimpleName() + "__et";
	}

	public String getEntityTypeClassFullName() {
		return getFullNamePrefix() + "__et";
	}

	public String getEnhancedClassSimpleName() {
		return getEntityIfaceSimpleName() + "__gm";
	}

	public String getDecoyClassSimpleName() {
		return getEntityIfaceSimpleName() + "__decoy";
	}

	public String getEnhancedClassFullName() {
		return getFullNamePrefix() + "__gm";
	}

	public String getDecoyClassFullName() {
		return getFullNamePrefix() + "__decoy";
	}

	/**
	 * This differs from {@link #getEntityIfaceFullName()} in case of entities defined as static nested classes. This
	 * omits the outer class name and only returns package.SimpleName.
	 */
	private String getFullNamePrefix() {
		return entityIface.getPackage().getName() + '.' + getEntityIfaceSimpleName();
	}

	public String getEntityIfaceSimpleName() {
		return entityIface.getSimpleSourceName();
	}

	public String getEntityIfaceFullName() {
		return entityIface.getQualifiedSourceName();
	}

	public String getEntityIfaceJniSignature() {
		return entityIface.getJNISignature();
	}

	public void addSuperType(EntityDesc superDesc) {
		directSuperTypes.add(superDesc);
		superDesc.directSubTypes.add(this);
	}

	private int getNonAbstractSubTypeCount() {
		if (nonAbstractSubTypeCount == -1) {
			int impls = directSubTypes.stream().mapToInt(d -> d.getNonAbstractSubTypeCount()).sum();

			if (isAbstract) {
				if (impls <= 1) {
					needsDecoy = true;
					impls++;
				}
			} else {
				impls++;
			}

			if (needsDecoy == null)
				needsDecoy = false;

			nonAbstractSubTypeCount = impls;
		}

		return nonAbstractSubTypeCount;
	}
}
