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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.ConstantPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.ConvertibleQualifiedProperty;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools;

/**
 * 
 */
public abstract class EntityPropertyMapping {

	public static enum EpmType {
		asIs,
		qualified,
		kpa,
		ikpa,
		link,
		orderedLink,
		compositeKpa,
		compositeIkpa,
		constant;

		public boolean isVirtual() {
			return this == constant;
		}
	}

	public final EntityMapping em;
	public final EpmType type;

	protected EntityPropertyMapping(EntityMapping em, EpmType type) {
		this.em = em;
		this.type = type;
	}

	public final IncrementalAccess getAccess() {
		return em.getAccess();
	}

	public final GmEntityType getDelegateEntityType() {
		return em.getDelegateEntityType();
	}

	public abstract String getDelegatePropertyName();

	public abstract GmType getDelegatePropertyType();

	public abstract boolean isDelegatePropertyId();

	public abstract SmartConversion getConversion();

	public final boolean isVirtual() {
		return type.isVirtual();
	}

	static abstract class DelegatedPropertyMapping extends EntityPropertyMapping {
		protected DelegatedPropertyMapping(EntityMapping em, EpmType type) {
			super(em, type);
		}

		@Override
		public String getDelegatePropertyName() {
			return getDelegateProperty().getName();
		}

		@Override
		public GmType getDelegatePropertyType() {
			return getDelegateProperty().getType();
		}

		// PGA TODO OPTIMIZE
		@Override
		public boolean isDelegatePropertyId() {
			return getDelegateProperty().isId();
		}

		protected abstract GmProperty getDelegateProperty();
	}

	public static class QualifiedPropertyWrapper extends DelegatedPropertyMapping {
		private final GmProperty delegateProperty;
		private final SmartConversion conversion;

		public QualifiedPropertyWrapper(EntityMapping em, KeyPropertyAssignment kpa) {
			super(em, EpmType.kpa);

			ConvertibleQualifiedProperty property = kpa.getProperty();
			this.delegateProperty = property.getProperty();
			this.conversion = property.getConversion();
		}

		public QualifiedPropertyWrapper(EntityMapping em, QualifiedPropertyAssignment qpa) {
			super(em, EpmType.qualified);
			this.delegateProperty = qpa.getProperty();
			this.conversion = qpa.getConversion();
		}

		@Override
		public GmProperty getDelegateProperty() {
			return delegateProperty;
		}

		@Override
		public SmartConversion getConversion() {
			return conversion;
		}

	}

	public static class PropertyAsIsWrapper extends DelegatedPropertyMapping {
		private final SmartConversion conversion;
		private final String smartProperty;

		public PropertyAsIsWrapper(EntityMapping em, String smartProperty, SmartConversion conversion) {
			super(em, EpmType.asIs);
			this.conversion = conversion;
			this.smartProperty = smartProperty;
		}

		@Override
		public GmProperty getDelegateProperty() {
			return SmartQueryPlannerTools.resolveProperty(getDelegateEntityType(), smartProperty);
		}

		@Override
		public SmartConversion getConversion() {
			return conversion;
		}
	}

	/**
	 * This is also used as wrapper for {@link CompositeInverseKeyPropertyAssignment} (as the two are the same).
	 */
	public static class CompositeKpaPropertyWrapper extends DelegatedPropertyMapping {
		private final Set<? extends KeyPropertyAssignment> kpas;
		private Map<KeyPropertyAssignment, EntityPropertyMapping> partialEpms;

		public CompositeKpaPropertyWrapper(EntityMapping em, CompositeKeyPropertyAssignment assignment) {
			this(em, assignment.getKeyPropertyAssignments(), EpmType.compositeKpa);
		}

		public CompositeKpaPropertyWrapper(EntityMapping em, CompositeInverseKeyPropertyAssignment assignment) {
			this(em, assignment.getInverseKeyPropertyAssignments(), EpmType.compositeIkpa);
		}

		private CompositeKpaPropertyWrapper(EntityMapping em, Set<? extends KeyPropertyAssignment> kpas, EpmType type) {
			super(em, type);
			this.kpas = kpas;
		}

		@Override
		public GmProperty getDelegateProperty() {
			throw new UnsupportedOperationException(
					"Method 'EntityPropertyMapping.CompositeKpaPropertyWrapper.getDelegateProperty' is not implemented yet!");
		}

		@Override
		public SmartConversion getConversion() {
			throw new UnsupportedOperationException(
					"Method 'EntityPropertyMapping.CompositeKpaPropertyWrapper.getConversion' is not implemented yet!");
		}

		public EntityPropertyMapping getPartialEntityPropertyMapping(KeyPropertyAssignment kpa) {
			return acquirePartialEpms().get(kpa);
		}

		private Map<KeyPropertyAssignment, EntityPropertyMapping> acquirePartialEpms() {
			if (partialEpms == null) {
				partialEpms = newMap();

				for (KeyPropertyAssignment kpa: kpas) 
					partialEpms.put(kpa, new QualifiedPropertyWrapper(em, kpa));
			}

			return partialEpms;
		}
	}

	static abstract class VirtualPropertyMapping extends EntityPropertyMapping {
		protected VirtualPropertyMapping(EntityMapping em, EpmType type) {
			super(em, type);
		}
	}

	public static class ConstantPropertyWrapper extends VirtualPropertyMapping {
		private final GmProperty smartProperty;
		private final String delegatePropertyName;
		private final Object constantValue;

		public ConstantPropertyWrapper(EntityMapping em, GmProperty smartProperty, ConstantPropertyAssignment cpa) {
			super(em, EpmType.constant);

			this.smartProperty = smartProperty;
			this.constantValue = cpa.getValue();
			this.delegatePropertyName = "virtual:" + smartProperty.getName();
		}

		@Override
		public SmartConversion getConversion() {
			return null;
		}

		@Override
		public String getDelegatePropertyName() {
			return delegatePropertyName;
		}

		@Override
		public GmType getDelegatePropertyType() {
			return smartProperty.getType();
		}

		@Override
		public boolean isDelegatePropertyId() {
			return false;
		}

		public String getSmartPropertyName() {
			return smartProperty.getName();
		}
		
		public Object getConstantValue() {
			return constantValue;
		}
	}

}
