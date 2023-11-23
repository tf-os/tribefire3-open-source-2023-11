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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.value.ValueDescriptor;

public abstract class AbstractProperty implements Property {
	private GenericModelType propertyType;
	private final String propertyName;
	private final boolean isIdentifier; 
	private final boolean isPartition; 
	private final boolean isGlobalId; 
	private final boolean confidential; 
	private final boolean nullable; 
	protected Object initializer;
	private PropertyCriterion criterion;

	public AbstractProperty(String propertyName, boolean nullable, boolean confidential) {
		this.propertyName = propertyName;
		this.isIdentifier = GenericEntity.id.equals(propertyName);
		this.isPartition = GenericEntity.partition.equals(propertyName);
		this.isGlobalId = GenericEntity.globalId.equals(propertyName);
		this.nullable = nullable;
		this.confidential = confidential;
	}

	@Override
	public final <T> T get(GenericEntity entity) {
		return (T) entity.read(this);
	}

	@Override
	public final void set(GenericEntity entity, Object value) {
		entity.write(this, value);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T getDirect(GenericEntity entity) {
		return getDirectUnsafe(entity);
	}

	@Override
	public Object setDirect(GenericEntity entity, Object value) {
		if (value == null && !nullable)
			throw new IllegalArgumentException("Value property '" + getName() + "'  cannot be null. Entity: " + entity);

		if (!getType().isValueAssignable(value))
			throw new IllegalArgumentException("Cannot assign value '" + value + "' of type '" + GMF.getTypeReflection().getType(value)
					+ "' to property '" + qualifiedName() + "' of type '" + getType() + "' Entity: " + entity);

		Object result = getDirectUnsafe(entity);
		setDirectUnsafe(entity, value);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public <T extends ValueDescriptor> T getVd(GenericEntity entity) {
		return (T) entity.readVd(this);
	}

	@Override
	public void setVd(GenericEntity entity, ValueDescriptor value) {
		entity.writeVd(this, value);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends ValueDescriptor> T getVdDirect(GenericEntity entity) {
		Object fieldValue = getDirectUnsafe(entity);
		return (T) VdHolder.getValueDescriptorIfPossible(fieldValue);
	}

	@Override
	public Object setVdDirect(GenericEntity entity, ValueDescriptor value) {
		if (value == null)
			throw new NullPointerException("ValueDescriptor cannot be null. Setting property '" + qualifiedName() + "' of: " + entity);

		Object result = getDirectUnsafe(entity);
		setDirectUnsafe(entity, VdHolder.newInstance(value));
		return result;
	}

	private String qualifiedName() {
		return getDeclaringType().getTypeSignature() + "." + getName();
	}

	@Override
	public EntityType<?> getFirstDeclaringType() {
		EntityType<?> result;
		Property p = this;

		do {
			result = p.getDeclaringType();

			p = null;
			for (EntityType<?> superType : result.getSuperTypes()) {
				p = superType.findProperty(propertyName);
				if (p != null)
					break;
			}

		} while (p != null);

		return result;
	}

	public void setPropertyType(GenericModelType propertyType) {
		this.propertyType = propertyType;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public boolean isConfidential() {
		return confidential;
	}

	@Override
	public GenericModelType getType() {
		return propertyType;
	}

	@Override
	public Class<?> getJavaType() {
		return nullable ? propertyType.getJavaType() : ((SimpleType) propertyType).getPrimitiveJavaType();
	}

	@Override
	public String getName() {
		return propertyName;
	}

	@Override
	public boolean isIdentifier() {
		return isIdentifier;
	}

	@Override
	public boolean isPartition() {
		return isPartition;
	}

	@Override
	public boolean isGlobalId() {
		return isGlobalId;
	}

	@Override
	public Object getInitializer() {
		return initializer;
	}

	public void setInitializer(Object initializer) {
		this.initializer = initializer;
	}

	/**
	 * @return true iff this property is either an {@link #isIdentifier() identifier} or it's the {@link #isPartition()
	 *         partition} property (i.e. is used to uniquely identify an entity)
	 */
	@Override
	public boolean isIdentifying() {
		return isIdentifier() || isPartition();
	}

	/** Accesses the property directly, bypassing any possible configured {@link PropertyAccessInterceptor}s */
	@Override
	public abstract <T> T getDirectUnsafe(GenericEntity entity);
	@Override
	public abstract void setDirectUnsafe(GenericEntity entity, Object value);

	@Override
	public AbsenceInformation getAbsenceInformation(GenericEntity entity) {
		Object value = getDirectUnsafe(entity);
		return VdHolder.getAbsenceInfoIfPossible(value);
	}

	/**
	 * @throws IllegalArgumentException
	 *             in case <tt>ai</tt> is <tt>null</tt>. Why? Before we had two different fields, one for property value
	 *             and another for {@link AbsenceInformation}, so after setting the property value we would have to set
	 *             AI to <tt>null</tt>. This is no longer the case, we only have one field for both, thus an invocation
	 *             setting it to <tt>null</tt> would either overwrite the original value, or we could ignore it, but in
	 *             that case it's better if the method is not called at all.
	 */
	@Override
	public void setAbsenceInformation(GenericEntity entity, AbsenceInformation ai) {
		setDirectUnsafe(entity, VdHolder.newInstance(ai));
	}

	@Override
	public boolean isAbsent(GenericEntity entity) {
		return VdHolder.isAbsenceInfo(getDirectUnsafe(entity));
	}

	@Override
	public boolean isEmptyValue(Object value) {
		return propertyType.isEmpty(value) || //
				(!nullable && propertyType.getDefaultValue().equals(value));
	}

	@Override
	public Object getDefaultValue() {
		return initializer != null ? initializer : getDefaultRawValue();
	}

	@Override
	public Object getDefaultRawValue() {
		return nullable ? null : propertyType.getDefaultValue();
	}

	@Override
	public String toString() {
		return "property[" + propertyName + "-" + getDeclaringType().getTypeSignature() + " of type " + propertyType.getTypeSignature() + "]";
	}

	public PropertyCriterion acquireCriterion() {
		if (criterion == null) {
			PropertyCriterion pc = PropertyCriterion.T.createPlainRaw();
			pc.setPropertyName(propertyName);
			pc.setTypeSignature(propertyType.getTypeSignature());

			criterion = pc;
		}

		return criterion;
	}

}
