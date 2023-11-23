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
package com.braintribe.model.processing.itw.analysis.protomodel;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.MetaDataAnnotations;
import com.braintribe.model.generic.annotation.meta.api.RepeatableMdaHandler.RepeatableAggregatorMdaHandler;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.meta.restriction.GmTypeRestriction;
import com.braintribe.model.processing.itw.analysis.api.EagerType;
import com.braintribe.model.weaving.ProtoGmBaseType;
import com.braintribe.model.weaving.ProtoGmBooleanType;
import com.braintribe.model.weaving.ProtoGmDateType;
import com.braintribe.model.weaving.ProtoGmDecimalType;
import com.braintribe.model.weaving.ProtoGmDoubleType;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmEnumConstant;
import com.braintribe.model.weaving.ProtoGmEnumType;
import com.braintribe.model.weaving.ProtoGmFloatType;
import com.braintribe.model.weaving.ProtoGmIntegerType;
import com.braintribe.model.weaving.ProtoGmListType;
import com.braintribe.model.weaving.ProtoGmLongType;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmModelElement;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmSetType;
import com.braintribe.model.weaving.ProtoGmStringType;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;

/**
 * @author peter.gazdik
 */
public class EagerMetaModelTypes {

	private static final List<Class<? extends GenericEntity>> list = newList();

	// @formatter:off
	public static final EagerType<ProtoGmMetaModel, GmMetaModel> eagerProtoGmMetaModel = new EagerTypeBase<ProtoGmMetaModel, GmMetaModel>(GmMetaModel.class) {
	    @Override public ProtoGmMetaModel createPseudo() { return new ProtoGmMetaModelImpl(); }
	    @Override public EntityType<GmMetaModel> entityType() { return GmMetaModel.T; }
	};

	public static final EagerType<ProtoGmBaseType, GmBaseType> eagerProtoGmBaseType = new EagerTypeBase<ProtoGmBaseType, GmBaseType>(GmBaseType.class) {
	    @Override public ProtoGmBaseType createPseudo() { return new ProtoGmBaseTypeImpl(); }
	    @Override public EntityType<GmBaseType> entityType() { return GmBaseType.T; }
	};

	public static final EagerProtoGmEntityType eagerProtoGmEntityType = new EagerProtoGmEntityType(GmEntityType.class);
	public static class EagerProtoGmEntityType extends EagerTypeBase<ProtoGmEntityType, GmEntityType> {
		protected EagerProtoGmEntityType(Class<GmEntityType> clazz) {super(clazz);}
		@Override public ProtoGmEntityType createPseudo() { return new ProtoGmEntityTypeImpl(); }
		@Override public EntityType<GmEntityType> entityType() { return GmEntityType.T; }
		
		public void setSuperTypes(ProtoGmEntityType twEntityType, List<? extends ProtoGmEntityType> superTypes, boolean proto) {
			if (proto)
				((ProtoGmEntityTypeImpl) twEntityType).setSuperTypes((List<ProtoGmEntityType>) superTypes);
			else
				((GmEntityType) twEntityType).setSuperTypes((List<GmEntityType>) superTypes);
		}

		public void setProperties(ProtoGmEntityType twEntityType, List<? extends ProtoGmProperty> properties, boolean proto) {
			if (proto)
				((ProtoGmEntityTypeImpl) twEntityType).setProperties((List<ProtoGmProperty>) properties);
			else
				((GmEntityType) twEntityType).setProperties((List<GmProperty>) properties);
		}

		public void setPropertyOverrides(ProtoGmEntityType twEntityType, List<? extends ProtoGmPropertyOverride> propertyOverrides, boolean proto) {
			if (proto)
				((ProtoGmEntityTypeImpl) twEntityType).setPropertyOverrides((List<ProtoGmPropertyOverride>) propertyOverrides);
			else
				((GmEntityType) twEntityType).setPropertyOverrides((List<GmPropertyOverride>) propertyOverrides);
		}

		public void setEvaluatesTo(ProtoGmEntityType entityType, ProtoGmType evaluatesTo, boolean proto) {
			if (proto)
				((ProtoGmEntityTypeImpl) entityType).setEvaluatesTo(evaluatesTo);
			else
				((GmEntityType) entityType).setEvaluatesTo((GmType) evaluatesTo);
		}
	}

	public static final EagerProtoGmProperty eagerProtoGmProperty = new EagerProtoGmProperty(GmProperty.class);
	public static class EagerProtoGmProperty extends EagerTypeBase<ProtoGmProperty, GmProperty> {
		protected EagerProtoGmProperty(Class<GmProperty> clazz) { super(clazz); }
		@Override public ProtoGmProperty createPseudo() { return new ProtoGmPropertyImpl(); }
	    @Override public EntityType<GmProperty> entityType() { return GmProperty.T; }
	    
		public void setDeclaringType(ProtoGmProperty twProperty, ProtoGmEntityType declaringType, boolean proto) {
			if (proto)
				((ProtoGmPropertyImpl) twProperty).setDeclaringType(declaringType);
			else
				((GmProperty) twProperty).setDeclaringType((GmEntityType) declaringType);
		}

		public void setType(ProtoGmProperty twProperty, ProtoGmType type, boolean proto) {
			if (proto)
				((ProtoGmPropertyImpl) twProperty).setType(type);
			else
				((GmProperty) twProperty).setType((GmType) type);
		}

		public void setTypeRestriction(ProtoGmProperty twProperty, ProtoGmTypeRestriction typeRestriction, boolean proto) {
			if (proto)
				((ProtoGmPropertyImpl) twProperty).setTypeRestriction(typeRestriction);
			else
				((GmProperty) twProperty).setTypeRestriction((GmTypeRestriction) typeRestriction);
		}
	}

	public static final EagerProtoGmPropertyOverride eagerProtoGmPropertyOverride = new EagerProtoGmPropertyOverride(GmPropertyOverride.class);
	public static class EagerProtoGmPropertyOverride extends EagerTypeBase<ProtoGmPropertyOverride, GmPropertyOverride> {
		protected EagerProtoGmPropertyOverride(Class<GmPropertyOverride> clazz) { super(clazz); }
		@Override public ProtoGmPropertyOverride createPseudo() {return new ProtoGmPropertyOverrideImpl(); }
		@Override public EntityType<GmPropertyOverride> entityType() { return GmPropertyOverride.T; }

		public void setDeclaringTypeInfo(ProtoGmPropertyOverride twPropertyOverride, ProtoGmEntityType declaringTypeInfo, boolean proto) {
			if (proto)
				((ProtoGmPropertyOverrideImpl) twPropertyOverride).setDeclaringTypeInfo(declaringTypeInfo);
			else
				((GmPropertyOverride) twPropertyOverride).setDeclaringTypeInfo((GmEntityTypeInfo) declaringTypeInfo);
		}

		public void setProperty(ProtoGmPropertyOverride twPropertyOverride, ProtoGmProperty property, boolean proto) {
			if (proto)
				((ProtoGmPropertyOverrideImpl) twPropertyOverride).setProperty(property);
			else
				((GmPropertyOverride) twPropertyOverride).setProperty((GmProperty) property);
		}
	}

	public static final EagerProtoGmTypeRestriction eagerProtoGmTypeRestriction = new EagerProtoGmTypeRestriction(GmTypeRestriction.class);
	public static class EagerProtoGmTypeRestriction extends EagerTypeBase<ProtoGmTypeRestriction, GmTypeRestriction> {
		protected EagerProtoGmTypeRestriction(Class<GmTypeRestriction> clazz) { super(clazz); }
		@Override public ProtoGmTypeRestriction createPseudo() { return new ProtoGmTypeRestrictionImpl(); }
		@Override public EntityType<GmTypeRestriction> entityType() { return GmTypeRestriction.T; }
		
		public void setTypes(ProtoGmTypeRestriction twTypeRestriction, List<? extends ProtoGmType> types, boolean proto) {
			if (proto)
				((ProtoGmTypeRestrictionImpl) twTypeRestriction).setTypes((List<ProtoGmType>) types);
			else
				((GmTypeRestriction) twTypeRestriction).setTypes((List<GmType>) types);
		}

		public void setKeyTypes(ProtoGmTypeRestriction twTypeRestriction, List<? extends ProtoGmType> types, boolean proto) {
			if (proto)
				((ProtoGmTypeRestrictionImpl) twTypeRestriction).setKeyTypes((List<ProtoGmType>) types);
			else
				((GmTypeRestriction) twTypeRestriction).setKeyTypes((List<GmType>) types);
		}
	}

	public static final EagerProtoGmEnumType eagerProtoGmEnumType = new EagerProtoGmEnumType(GmEnumType.class);
	public static class EagerProtoGmEnumType extends EagerTypeBase<ProtoGmEnumType, GmEnumType> {
	    protected EagerProtoGmEnumType(Class<GmEnumType> clazz) { super(clazz); }
		@Override public ProtoGmEnumType createPseudo() { return new ProtoGmEnumTypeImpl(); }
	    @Override public EntityType<GmEnumType> entityType() { return GmEnumType.T; }
	    
		public void setConstatns(ProtoGmEnumType twEnumType, List<? extends ProtoGmEnumConstant> constants, boolean proto) {
			if (proto)
				((ProtoGmEnumTypeImpl) twEnumType).setConstants((List<ProtoGmEnumConstant>) constants);
			else
				((GmEnumType) twEnumType).setConstants((List<GmEnumConstant>) constants);
		}
	}

	public static final EagerProtoGmEnumConstant eagerProtoGmEnumConstant = new EagerProtoGmEnumConstant(GmEnumConstant.class);
	public static class EagerProtoGmEnumConstant extends EagerTypeBase<ProtoGmEnumConstant, GmEnumConstant> {
	    protected EagerProtoGmEnumConstant(Class<GmEnumConstant> clazz) { super(clazz); }
		@Override public ProtoGmEnumConstant createPseudo() { return new ProtoGmEnumConstantImpl(); }
	    @Override public EntityType<GmEnumConstant> entityType() { return GmEnumConstant.T; }
	    
		public void setDeclaringType(ProtoGmEnumConstant twConstant, ProtoGmEnumType declaringType, boolean proto) {
			if (proto)
				((ProtoGmEnumConstantImpl) twConstant).setDeclaringType(declaringType);
			else
				((GmEnumConstant) twConstant).setDeclaringType((GmEnumType) declaringType);
		}
	}

	public static final EagerType<ProtoGmBooleanType, GmBooleanType> eagerProtoGmBooleanType = new EagerTypeBase<ProtoGmBooleanType, GmBooleanType>(GmBooleanType.class) {
	    @Override public ProtoGmBooleanType createPseudo() { return new ProtoGmBooleanTypeImpl(); }
	    @Override public EntityType<GmBooleanType> entityType() { return GmBooleanType.T; }
	};

	public static final EagerType<ProtoGmIntegerType, GmIntegerType> eagerProtoGmIntegerType = new EagerTypeBase<ProtoGmIntegerType, GmIntegerType>(GmIntegerType.class) {
	    @Override public ProtoGmIntegerType createPseudo() { return new ProtoGmIntegerTypeImpl(); }
	    @Override public EntityType<GmIntegerType> entityType() { return GmIntegerType.T; }
	};

	public static final EagerType<ProtoGmLongType, GmLongType> eagerProtoGmLongType = new EagerTypeBase<ProtoGmLongType, GmLongType>(GmLongType.class) {
	    @Override public ProtoGmLongType createPseudo() { return new ProtoGmLongTypeImpl(); }
	    @Override public EntityType<GmLongType> entityType() { return GmLongType.T; }
	};

	public static final EagerType<ProtoGmFloatType, GmFloatType> eagerProtoGmFloatType = new EagerTypeBase<ProtoGmFloatType, GmFloatType>(GmFloatType.class) {
	    @Override public ProtoGmFloatType createPseudo() { return new ProtoGmFloatTypeImpl(); }
	    @Override public EntityType<GmFloatType> entityType() { return GmFloatType.T; }
	};

	public static final EagerType<ProtoGmDoubleType, GmDoubleType> eagerProtoGmDoubleType = new EagerTypeBase<ProtoGmDoubleType, GmDoubleType>(GmDoubleType.class) {
	    @Override public ProtoGmDoubleType createPseudo() { return new ProtoGmDoubleTypeImpl(); }
	    @Override public EntityType<GmDoubleType> entityType() { return GmDoubleType.T; }
	};

	public static final EagerType<ProtoGmDecimalType, GmDecimalType> eagerProtoGmDecimalType = new EagerTypeBase<ProtoGmDecimalType, GmDecimalType>(GmDecimalType.class) {
	    @Override public ProtoGmDecimalType createPseudo() { return new ProtoGmDecimalTypeImpl(); }
	    @Override public EntityType<GmDecimalType> entityType() { return GmDecimalType.T; }
	};

	public static final EagerType<ProtoGmDateType, GmDateType> eagerProtoGmDateType = new EagerTypeBase<ProtoGmDateType, GmDateType>(GmDateType.class) {
	    @Override public ProtoGmDateType createPseudo() { return new ProtoGmDateTypeImpl(); }
	    @Override public EntityType<GmDateType> entityType() { return GmDateType.T; }
	};

	public static final EagerType<ProtoGmStringType, GmStringType> eagerProtoGmStringType = new EagerTypeBase<ProtoGmStringType, GmStringType>(GmStringType.class) {
	    @Override public ProtoGmStringType createPseudo() { return new ProtoGmStringTypeImpl(); }
	    @Override public EntityType<GmStringType> entityType() { return GmStringType.T; }
	};

	public static final EagerProtoGmListType eagerProtoGmListType = new EagerProtoGmListType(GmListType.class);
	public static class EagerProtoGmListType extends EagerTypeBase<ProtoGmListType, GmListType> {
	    protected EagerProtoGmListType(Class<GmListType> clazz) { super(clazz); }
	    @Override public ProtoGmListType createPseudo() { return new ProtoGmListTypeImpl(); }
	    @Override public EntityType<GmListType> entityType() { return GmListType.T; }

		public void setElementType(ProtoGmListType twListType, ProtoGmType elementType, boolean proto) {
			if (proto)
				((ProtoGmListTypeImpl) twListType).setElementType(elementType);
			else
				((GmListType) twListType).setElementType((GmType) elementType);
		}
	}

	public static final EagerProtoGmMapType eagerProtoGmMapType = new EagerProtoGmMapType(GmMapType.class);
	public static class EagerProtoGmMapType extends EagerTypeBase<ProtoGmMapType, GmMapType> {
	    protected EagerProtoGmMapType(Class<GmMapType> clazz) { super(clazz); }
		@Override public ProtoGmMapType createPseudo() { return new ProtoGmMapTypeImpl(); }
	    @Override public EntityType<GmMapType> entityType() { return GmMapType.T; }
	    
		public void setKeyType(ProtoGmMapType twMapType, ProtoGmType keyType, boolean proto) {
			if (proto)
				((ProtoGmMapTypeImpl) twMapType).setKeyType(keyType);
			else
				((GmMapType) twMapType).setKeyType((GmType) keyType);
		}

		public void setValueType(ProtoGmMapType twMapType, ProtoGmType valueType, boolean proto) {
			if (proto)
				((ProtoGmMapTypeImpl) twMapType).setValueType(valueType);
			else
				((GmMapType) twMapType).setValueType((GmType) valueType);
		}
	}

	public static final EagerProtoGmSetType eagerProtoGmSetType = new EagerProtoGmSetType(GmSetType.class);
	public static class EagerProtoGmSetType extends EagerTypeBase<ProtoGmSetType, GmSetType> {
	    protected EagerProtoGmSetType(Class<GmSetType> clazz) { super(clazz); }
		@Override public ProtoGmSetType createPseudo() { return new ProtoGmSetTypeImpl(); }
	    @Override public EntityType<GmSetType> entityType() { return GmSetType.T; }
	    
	    public void setElementType(ProtoGmSetType twSetType, ProtoGmType elementType, boolean proto) {
			if (proto)
				((ProtoGmSetTypeImpl) twSetType).setElementType(elementType);
			else
				((GmSetType) twSetType).setElementType((GmType) elementType);
		}
	}
	// @formatter:on

	/**
	 * To avoid cycles, we want to eagerly instantiate all the types which are needed during JTA (Java Type Analysis). This list contains those
	 * entities with remarks as to where/why they are needed.
	 */
	static {
		for (MdaHandler<?, ?> mdaHandler : MetaDataAnnotations.registry().annoToHandler().values())
			if (!(mdaHandler instanceof RepeatableAggregatorMdaHandler<?, ?>))
				list.add(mdaHandler.metaDataClass());

		// initializers
		list.add(Now.class);
		list.add(EnumReference.class);
		list.add(NullDescriptor.class);

		// accessed from PAIs
		list.add(AbsenceInformation.class);
	}

	public static List<Class<? extends GenericEntity>> instantiableJavaTypes() {
		return list;
	}

	public static abstract class EagerTypeBase<T extends ProtoGmModelElement, E extends GenericEntity> implements EagerType<T, E> {

		protected EagerTypeBase(Class<E> clazz) {
			list.add(clazz);
		}

		@Override
		public abstract T createPseudo();

		@Override
		public abstract EntityType<E> entityType();

	}

}
