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
package com.braintribe.model.generic.proxy;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.SessionUnboundPropertyAccessInterceptor;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.Attribute;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericToStringBuilder;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.GlobalEntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

@SuppressWarnings("unusable-by-js")
public abstract class AbstractProxyEntityType implements EntityType<ProxyEntity> {

	private static final GenericModelType[] PARAMETERIZATION = new GenericModelType[0];
	
	protected final String typeSignature;
	private String simpleTypeName;
	protected final Map<String, AbstractProxyProperty> propertiesByName = new LinkedHashMap<>();
	protected List<Property> properties;
	private EntityType<?> resolvedType;
	private boolean resolved;

	public AbstractProxyEntityType(String typeSignature) {
		this.typeSignature = typeSignature;
	}

	public EntityType<?> getResolvedType() {
		if (!resolved) {
			resolvedType = GMF.getTypeReflection().findType(typeSignature);
			resolved = true;
		}

		return resolvedType;
	}

	@Override
	@Deprecated
	public Set<EntityType<?>> getInstantiableSubTypes() {
		return Collections.emptySet();
	}

	@Override
	@SuppressWarnings("deprecation") // this will stay, just the iface method will be dropped
	public String toString(ProxyEntity instance) {
		return GenericToStringBuilder.buildToString(instance, this);
	}

	@Override
	@Deprecated
	public Set<EntityType<?>> getSubTypes() {
		return Collections.emptySet();
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.entityType;
	}

	@Override
	public String getTypeName() {
		return typeSignature;
	}

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public Model getModel() {
		return null;
	}

	@Override
	public String getSelectiveInformation(Object instance) {
		return null;
	}

	@Override
	public GenericModelType getActualType(Object value) {
		return value == null ? this : ((GenericEntity) value).type();
	}

	@Override
	public Object getValueSnapshot(Object value) {
		return value;
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public boolean isInstance(Object value) {
		return value instanceof ProxyEntity && ((ProxyEntity) value).type() == this;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public boolean isEntity() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public <T extends GenericModelType> T cast() {
		return (T) this;
	}

	@Override
	public boolean areEntitiesReachable() {
		return true;
	}

	@Override
	public boolean areCustomInstancesReachable() {
		return true;
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null;
	}

	@Override
	public boolean isVd() {
		return false;
	}

	@Override
	public boolean isBase() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public GenericModelType[] getParameterization() {
		return PARAMETERIZATION;
	}

	@Override
	public void traverse(TraversingContext traversingContext, Object instance) throws GenericModelException {
		throw new UnsupportedOperationException("traverse is not possible for ProxyEntity");
	}

	@Override
	public TraversingContext traverse(Object instance, Matcher matcher, TraversingVisitor traversingVisitor) throws GenericModelException {
		throw new UnsupportedOperationException("traverse is not possible for ProxyEntity");
	}

	@Override
	public Object clone(Object instance, Matcher matcher, StrategyOnCriterionMatch strategy) throws GenericModelException {
		throw new UnsupportedOperationException("clone is not possible for ProxyEntity");
	}

	@Override
	public <T> T clone(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException {
		throw new UnsupportedOperationException("clone is not possible for ProxyEntity");
	}

	@Override
	public Class<ProxyEntity> getJavaType() {
		return ProxyEntity.class;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public String getShortName() {
		if (simpleTypeName == null) {
			int index = typeSignature.lastIndexOf('.');
			if (index != -1) {
				simpleTypeName = typeSignature.substring(index + 1);
			} else {
				simpleTypeName = typeSignature;
			}
		}
		return simpleTypeName;
	}

	@Override
	public List<EntityType<?>> getSuperTypes() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<EntityType<?>> getTransitiveSuperTypes(boolean includeSelf, boolean distinct) {
		return includeSelf ? Collections.singleton(this) : Collections.emptySet();
	}

	@Override
	public boolean hasExplicitSelectiveInformation() {
		return false;
	}

	@Override
	public Property getIdProperty() {
		return getProperty(GenericEntity.id);
	}

	@Override
	public List<Property> getProperties() {
		if (properties == null) {
			properties = new ArrayList<>(propertiesByName.values());
		}
		return properties;
	}

	public List<ProxyProperty> getProxyProperties() {
		return (List<ProxyProperty>) (List<?>) getProperties();
	}

	@Override
	public abstract Property getProperty(String name) throws GenericModelException;

	@Override
	public Property findProperty(String name) {
		return propertiesByName.get(name);
	}

	@Override
	public List<Property> getCustomTypeProperties() {
		return getProperties();
	}

	@Override
	public List<Property> getDeclaredProperties() {
		return getProperties();
	}

	@Override
	public List<TransientProperty> getTransientProperties() {
		return emptyList();
	}

	@Override
	public TransientProperty getTransientProperty(String name) throws GenericModelException {
		throw new GenericModelException("Cannot get transient property '" + name + "' on proxy type '" + getTypeName()
				+ "'. Transient properties are not supported on proxy types.");
	}

	@Override
	public TransientProperty findTransientProperty(String name) {
		return null; // null, because we have no transient properties on this level
	}

	@Override
	public List<TransientProperty> getDeclaredTransientProperties() {
		return emptyList();
	}

	@Override
	public Stream<Attribute> getAttributes() {
		return (Stream<Attribute>) (Stream<? extends Attribute>) getProperties().stream();
	}

	@Override
	public GenericModelType getEvaluatesTo() {
		return null;
	}

	@Override
	public GenericModelType getEffectiveEvaluatesTo() {
		return null;
	}

	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		return false;
	}

	@Override
	public boolean isAssignableFrom(EntityType<?> entityType) {
		return false;
	}

	@Override
	public EntityReference createReference(ProxyEntity entity, Object idValue) {
		EntityReference ref;

		if (idValue != null) {
			ref = PersistentEntityReference.T.createPlain();
			ref.setRefId(idValue);

		} else {
			ref = PreliminaryEntityReference.T.createPlain();
			ref.setRefId(entity.runtimeId());
		}

		ref.setTypeSignature(typeSignature);
		ref.setRefPartition(entity.getPartition());

		return ref;
	}

	@Override
	public EntityReference createGlobalReference(ProxyEntity entity, String globalId) {
		EntityReference ref;

		if (globalId != null) {
			ref = GlobalEntityReference.T.createPlain();
			ref.setRefId(globalId);

		} else {
			ref = PreliminaryEntityReference.T.createPlain();
			ref.setRefId(entity.runtimeId());
		}

		ref.setTypeSignature(typeSignature);
		ref.setRefPartition(entity.getPartition());

		return ref;
	}

	@Override
	public <E extends ProxyEntity> E initialize(E entity) {
		return entity;
	}

	@Override
	public ProxyEntity create() {
		return create(SessionUnboundPropertyAccessInterceptor.INSTANCE);
	}

	@Override
	public ProxyEntity create(String globalId) {
		ProxyEntity result = create();
		result.setGlobalId(globalId);
		return result;
	}

	@Override
	public ProxyEntity createPlain() {
		return new ProxyPlainEntity(this);
	}

	@Override
	public ProxyEntity create(PropertyAccessInterceptor pai) {
		return new ProxyEnhancedEntity(this, pai);
	}

	@Override
	public ProxyEntity createPlainRaw() {
		return createPlain();
	}

	@Override
	public ProxyEntity createRaw() {
		return create();
	}

	@Override
	public ProxyEntity createRaw(PropertyAccessInterceptor pai) {
		return create(pai);
	}

	@Override
	public Class<? extends ProxyEntity> plainClass() {
		return ProxyEntity.class;
	}

	@Override
	public Class<? extends ProxyEntity> enhancedClass() {
		return ProxyEntity.class;
	}

	@Override
	public int compareTo(GenericModelType o) {
		if (o == null)
			return 1;

		if (this == o)
			return 0;

		String myTypeSignature = this.getTypeSignature();
		String otherTypeSignature = o.getTypeSignature();

		return myTypeSignature.compareTo(otherTypeSignature);
	}

}
