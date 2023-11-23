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
package com.braintribe.codec.dom.genericmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.w3c.dom.Element;

import com.braintribe.codec.Codec;
import com.braintribe.codec.dom.plain.DomListCodec;
import com.braintribe.codec.dom.plain.DomMapCodec;
import com.braintribe.codec.dom.plain.DomSetCodec;
import com.braintribe.codec.string.BigDecimalCodec;
import com.braintribe.codec.string.BooleanCodec;
import com.braintribe.codec.string.DoubleCodec;
import com.braintribe.codec.string.FloatCodec;
import com.braintribe.codec.string.IntegerCodec;
import com.braintribe.codec.string.LongCodec;
import com.braintribe.codec.string.StringCodec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleType;


public class GenericModelDomCodecRegistry {
	private static GenericModelDomCodecRegistry defaultInstance;

	public synchronized static GenericModelDomCodecRegistry getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new GenericModelDomCodecRegistry();
		}

		return defaultInstance;
	}

	
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private final Map<GenericModelType, Codec<?, Element>> codecs = new HashMap<GenericModelType, Codec<?, Element>>();
	private ReentrantReadWriteLock codecsLock = new ReentrantReadWriteLock();
	private final Map<String, Codec<?, Element>> codecByTagName = new HashMap<>();
	private final GenericModelEntityDomCodec<GenericEntity> entityCodec;
	private final NullWrapperCodec<Object> objectCodec;
	
	public GenericModelDomCodecRegistry() {
		
		// register simple type codecs
		registerSimpleCodec(GenericModelTypeReflection.TYPE_BOOLEAN, new BooleanCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_STRING, new StringCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_INTEGER, new IntegerCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_LONG, new LongCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_FLOAT, new FloatCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_DOUBLE, new DoubleCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_DATE, new GenericModelDateCodec());
		registerSimpleCodec(GenericModelTypeReflection.TYPE_DECIMAL, new BigDecimalCodec());
		
		// register object codec (fully generic codec)
		GenericModelObjectDomCodec plainObjectCodec = new GenericModelObjectDomCodec();
		plainObjectCodec.setCodecRegistry(this);
		
		objectCodec = new NullWrapperCodec<>();
		objectCodec.setValueCodec(plainObjectCodec);
		
		BaseType baseType = BaseType.INSTANCE;
		
		codecs.put(baseType, objectCodec);
		
		// register entity codec (generic codec for entities)
		EntityType<GenericEntity> entityType = GenericEntity.T;
		entityCodec = new GenericModelEntityDomCodec<>();
		entityCodec.setCodecRegistry(this);
		entityCodec.setType(entityType);
		
		codecByTagName.put("entity", entityCodec);
		
		// register null codec
		codecByTagName.put("null", new NullCodec<>());
		
		// register enum codec
		@SuppressWarnings("rawtypes")
		EnumCodec<?> enumCodec = new EnumCodec();
		codecByTagName.put("enum", enumCodec);
		
		// register generic collection codec
		Codec<List<Object>, Element> listCodec = getCodec(typeReflection.getListType(baseType));
		Codec<Set<Object>, Element> setCodec = getCodec(typeReflection.getSetType(baseType));
		Codec<Map<Object, Object>, Element> mapCodec = getCodec(typeReflection.getMapType(baseType, baseType));
		
		codecByTagName.put("list", listCodec);
		codecByTagName.put("set", setCodec);
		codecByTagName.put("map", mapCodec);
		
		// To support older versions of encoded xmls we add a LegacAnyTypeDomCodec.
		LegacyAnyTypeDomCodec anyCodec = new LegacyAnyTypeDomCodec();
		anyCodec.setCodecRegistry(this);
		
		codecByTagName.put("any", anyCodec);
		
	}

	protected <T> void registerSimpleCodec(SimpleType simpleType, Codec<T, String> delegateCodec) {
		registerSimpleCodec(simpleType, delegateCodec, simpleType.getTypeName());
	}
	
	protected <T> void registerSimpleCodec(SimpleType simpleType, Codec<T, String> delegateCodec, String tagName) {
		ValueDomCodec<T> codec = new ValueDomCodec<>();
		codec.setElementName(tagName);
		codec.setDelegate(delegateCodec);

		NullWrapperCodec<T> nullWrapperCodec = new NullWrapperCodec<>();
		nullWrapperCodec.setValueCodec(codec);
		//Note: no synchronized needed here as this is only called from the Constructor
		codecs.put(simpleType, nullWrapperCodec);
		codecByTagName.put(tagName, codec);
	}
	
	public <T> Codec<T, Element> getCodecByTagName(String tagName) {
		@SuppressWarnings("unchecked")
		Codec<T, Element> codec = (Codec<T, Element>)codecByTagName.get(tagName);
		return codec;
	}

	@SuppressWarnings("unchecked")
	public <T> Codec<T, Element> getCodec(GenericModelType type) {
		if (type == null)
			throw new IllegalArgumentException("null is not allowed as GenericModelType argument here");
		
		if (type instanceof EntityType<?>) {
			/*
			EntityType<GenericEntity> entityType = (EntityType<GenericEntity>) type;
			if (entityType.getTypeSignature().equals("com.braintribe.model.generic.value.ValueRepresentation")) {
				return this.legacyValueRepresentationCodec;
			}*/
			
			return (Codec<T, Element>)entityCodec;
		}
		
		Codec<?, Element> codec = null;
		
		codecsLock.readLock().lock();
		try {
			codec = codecs.get(type);
		} finally {
			codecsLock.readLock().unlock();
		}

		if (codec == null) {
			if (type instanceof EnumType) {
				@SuppressWarnings("rawtypes")
				EnumCodec<?> enumCodec = new EnumCodec();
				enumCodec.setEnumType((EnumType)type);
				codec = enumCodec;
			}
			else if (type instanceof CollectionType) {
				CollectionType collectionType = (CollectionType)type;

				switch (collectionType.getCollectionKind()) {
				case list:
					DomListCodec<Object> domListCodec = new DomListCodec<>();
					Codec<Object, Element> listElementCodec = getCodec(collectionType.getCollectionElementType());
					domListCodec.setDomCodec(listElementCodec);
					codec = domListCodec;
					break;
				case map:
					GenericModelType parameterization[] = collectionType.getParameterization();
					DomMapCodec<Object, Object> domMapCodec = new DomMapCodec<>();

					Codec<Object, Element> keyCodec = getCodec(parameterization[0]);
					Codec<Object, Element> valueCodec = getCodec(parameterization[1]);

					domMapCodec.setDomKeyCodec(keyCodec);
					domMapCodec.setDomValueCodec(valueCodec);
					codec = domMapCodec;
					break;
				case set:
					DomSetCodec<Object> domSetCodec = new DomSetCodec<>();
					Codec<Object, Element> setElementCodec = getCodec(collectionType.getCollectionElementType());
					domSetCodec.setDomCodec(setElementCodec);
					codec = domSetCodec;
					break;
				}
			}

			if (codec == null) {
				throw new IllegalStateException("could not create codec for GenericModelType " + type.getTypeSignature());
			}
			
			NullWrapperCodec<T> nullWrapperCodec = new NullWrapperCodec<>();
			nullWrapperCodec.setValueCodec((Codec<T, Element>)codec);

			codecsLock.writeLock().lock();
			try {
				codecs.put(type, nullWrapperCodec);
			} finally {
				codecsLock.writeLock().unlock();
			}
			codec = nullWrapperCodec;
		}

		return (Codec<T, Element>)codec;
	}

}
