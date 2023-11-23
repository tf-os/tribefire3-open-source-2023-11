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
package com.braintribe.codec.jseval.genericmodel;


import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;


public class GenericModelJsEvalCodec<T> implements Codec<T, String> {
	private boolean hostedMode = false;
	private JavaScriptPrototypes prototypes;
	
	@Required
	public void setPrototypes(JavaScriptPrototypes prototypes) {
		this.prototypes = prototypes;
	}
	
	public void setHostedMode(boolean hostedMode) {
		this.hostedMode = hostedMode;
	}
	
	@Override
	public T decode(String encodedValue) throws CodecException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String encode(T value) throws CodecException {
		EncodingContext encodingContext = new EncodingContext(hostedMode, prototypes);
		return encodingContext.assemble(value);
	}
	
	@Override
	public Class<T> getValueClass() {
		return null;
	}
	
	private static class EntityListNode {
		public GenericEntity entity;
		public EntityListNode successor;
	}
	
	private static class EncodingContext {
		private Map<GenericModelType, String> types = new HashMap<GenericModelType, String>();
		private Map<String, String> properties = new HashMap<String, String>();
		private Map<GenericEntity, String> entities = new HashMap<GenericEntity, String>();
		private EntityListNode lastEntityNode;
		private EntityListNode firstEntityNode;
		private String typeReflectionVarName = null;
		private boolean hostedMode;
		private JavaScriptPrototypes prototypes;
		private ReferenceGenerator varNameGenerator;
		
		
		public EncodingContext(boolean hostedMode, JavaScriptPrototypes prototypes) {
			super();
			this.hostedMode = hostedMode;
			this.prototypes = prototypes;
			varNameGenerator = new ArrayFieldReferenceGenerator();
			
			typeNamesSection.append("//BEGIN_TYPES\n");
		}

		private StringBuilder typeNamesSection = new StringBuilder();
		private StringBuilder typesSection = new StringBuilder();
		private StringBuilder propertiesSection = new StringBuilder();
		private StringBuilder instantiationSection = new StringBuilder();
		private StringBuilder assemblySection = new StringBuilder();
		
		public boolean isHostedMode() {
			return hostedMode;
		}
		
		public String aquireTypeReflectionVarName() throws CodecException {
			if (typeReflectionVarName == null) {
				typeReflectionVarName = varNameGenerator.nextVarName();
				String typeReflectionRef = prototypes.typeReflection().evaluate();
				typesSection.append(typeReflectionVarName);
				typesSection.append("=");
				typesSection.append(typeReflectionRef);
				typesSection.append(";\n");
			}
			
			return typeReflectionVarName;
		}
		
		public String aquireTypeVarName(GenericModelType type) throws CodecException {
			String typeVarName = types.get(type);
			if (typeVarName == null) {
				String typeNameVarName = varNameGenerator.nextVarName();
				
				typeNamesSection.append(typeNameVarName);
				typeNamesSection.append("=");
				typeNamesSection.append("\"" + type.getTypeSignature() + "\"");
				typeNamesSection.append(";\n");
				
				String aquireTypeReflectionVarName = aquireTypeReflectionVarName();
				
				typeVarName = varNameGenerator.nextVarName();
				types.put(type, typeVarName);
				
				String typeRef = prototypes.resolveType().evaluate(aquireTypeReflectionVarName, typeNameVarName);
				typesSection.append(typeVarName);
				typesSection.append("=");
				typesSection.append(typeRef);
				typesSection.append(";\n");
			}
			
			return typeVarName;
		}
		
		public String aquireEntityVarName(GenericEntity entity) throws CodecException {
			String entityVarName = entities.get(entity);
			if (entityVarName == null) {
				String typeVarName = aquireTypeVarName(entity.entityType());
				entityVarName = varNameGenerator.nextVarName();
				String instantiation = prototypes.create().evaluate(typeVarName);
				
				instantiationSection.append(entityVarName);
					
				instantiationSection.append("=");
				instantiationSection.append(instantiation);
				instantiationSection.append(";\n");

				registerEntity(entity, entityVarName);
			}
			
			return entityVarName;
		}
		
		protected void registerEntity(GenericEntity entity, String entityVarName) {
			entities.put(entity, entityVarName);
			
			EntityListNode node = new EntityListNode();
			node.entity = entity;
			
			if (firstEntityNode == null) {
				firstEntityNode = lastEntityNode = node; 
			}
			else {
				lastEntityNode.successor = node;
				lastEntityNode = node;
			}
			
		}
		
		public String aquirePropertyVarName(EntityType<?> type, String propertyName) throws CodecException {
			String property = type.getTypeSignature() + ":" + propertyName;
			String propertyVarName = properties.get(property);
			if (propertyVarName == null) {
				String typeVarName = aquireTypeVarName(type);
				propertyVarName = varNameGenerator.nextVarName();
				properties.put(property, propertyVarName);
				
				String propertyRef = prototypes.resolveProperty().evaluate(typeVarName, "\"" + propertyName + "\"");
				propertiesSection.append(propertyVarName);
				propertiesSection.append("=");
				propertiesSection.append(propertyRef);
				propertiesSection.append(";\n");
			}
			
			return propertyVarName;
		}
		
		public void encodeEntityWiring(StringBuilder builder, GenericEntity entity) throws CodecException {
			final String entityVarName = aquireEntityVarName(entity);

			EntityType<GenericEntity> entityType = entity.entityType();
			
			for (Property property: entityType.getProperties()) {
				final AbsenceInformation ai = property.getAbsenceInformation(entity);
				
				if (ai != null) {
					final String absenceInformationVarName = aquireEntityVarName(ai);
					final String propertyVarName = aquirePropertyVarName(entityType, property.getName());
					prototypes.setAbsence().evaluate(builder, propertyVarName, entityVarName, absenceInformationVarName);
					builder.append(";\n");
				}
				else {
					final Object value = property.get(entity);
					
					if (value != null) {
						final String propertyVarName = aquirePropertyVarName(entityType, property.getName());

						prototypes.setValue().evaluate(builder,
								propertyVarName, entityVarName, new CodeWriter() {
							@Override
							public void write(StringBuilder builder) throws CodecException {
								encodeValue(builder, value);
							}
						});
						builder.append(";\n");
					}
				}
				
			}
		}
		
		
		public void encodeValue(StringBuilder builder, Object value) throws CodecException {
			ValueEncoder<?> encoder;
			if (value == null)
				encoder = NullEncoder.instance;
			else if (value instanceof GenericEntity) {
				encoder = EntityEncoder.instance;
			}
			else if (value instanceof Enum) {
				encoder = EnumEncoder.instance;
			}
			else if (value instanceof List) {
				encoder = ListEncoder.instance;
			}
			else if (value instanceof Set) {
				encoder = SetEncoder.instance;
			}
			else if (value instanceof Map) {
				encoder = MapEncoder.instance;
			}
			else if (value instanceof Date) {
				encoder = dateEncoder;
			}
			else {
				encoder = encodersByClass.get(value.getClass());
			}

			if (encoder != null) {
				@SuppressWarnings("unchecked")
				ValueEncoder<Object> castedEncoder = (ValueEncoder<Object>)encoder;
				castedEncoder.encode(builder, value, prototypes, this);
			}
			else
				throw new CodecException("unkown value found: " + value);
			
		}
		
		public String assemble(Object value) throws CodecException {
			StringBuilder valueSection = new StringBuilder();
			
			// actual value encoding
			valueSection.append("return ");
			encodeValue(valueSection, value);
			valueSection.append(";");
			
			EntityListNode node = firstEntityNode;
			
			while (node != null) {
				GenericEntity entity = node.entity;
				encodeEntityWiring(assemblySection, entity);
				node = node.successor;
			}
			
			
			typeNamesSection.append("//END_TYPES\n");
			
			StringBuilder builders[] = {
				typeNamesSection,
				typesSection,
				propertiesSection,
				instantiationSection,
				assemblySection,
				valueSection
			};
			
			int size = 4096; //Allow for more space as we cannot say the length of createFragmentationComment
			for (StringBuilder builder: builders) {
				size += builder.length();
			}
			
			StringBuilder assembledCode = new StringBuilder(size);
			for (StringBuilder builder: builders) {
				assembledCode.append(builder.toString());
			}
			
			// write sections comment in case the script script should be devidable in portions than can run one after the other
			assembledCode.append("\n");
			assembledCode.append(createFragmentationComment(assembledCode));
			
			return assembledCode.toString();
		}
		
		protected String createFragmentationComment(CharSequence cs) {
			int len = cs.length();
			int statementGranularity = 500;
			int s = 0;
			int statementCount = 0;
			StringBuilder comment = new StringBuilder(256);
			comment.append("/*fragments:");
			comment.append(varNameGenerator.getGeneratedReferencesCount());
			int i = s;
			for (; i < len; i++) {
				char c = cs.charAt(i);
				
				if (c == '\n') {
					if (++statementCount > statementGranularity) {
						comment.append(',');
						comment.append(s);
						comment.append('-');
						comment.append(i);
						statementCount = 0;
						s = i + 1;
					}
					
				}
			}
			
			if (i > s) {
				comment.append(',');
				comment.append(s);
				comment.append('-');
				comment.append(i);
			}
			
			comment.append("*/");
			
			return comment.toString();
		}
	}
	
	
	protected interface ValueEncoder<V> {
		public void encode(StringBuilder builder, V value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException;
	}

	protected static class NullEncoder implements ValueEncoder<Object> {
		public final static NullEncoder instance = new NullEncoder();
		@Override
		public void encode(StringBuilder builder, Object value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append("null");
		}
	}
	
	protected static class StringEncoder implements ValueEncoder<String> {
		@Override
		public void encode(StringBuilder builder, String value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append("\"" + org.json.simple.JSONObject.escape(value) + "\"");
		}
	}
	
	protected static class IntegerEncoder implements ValueEncoder<Integer> {
		@Override
		public void encode(StringBuilder builder, Integer value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(prototypes.boxInteger().evaluate(value.toString()));
		}
	}
	
	protected static class LongEncoder implements ValueEncoder<Long> {
		@Override
		public void encode(StringBuilder builder, Long value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			if (encodingContext.isHostedMode()) {
				String strValue = value.toString();
				builder.append(prototypes.parseLongBox().evaluate("\"" + strValue + "\""));
			}
			else {
				int parts[] = LongLib.getAsIntArray(value);
				String longLiteral = "{l:" + parts[0] + ",m:" + parts[1] + ",h:" + parts[2]+"}";
				builder.append(prototypes.boxLong().evaluate(longLiteral));
			}
		}
	}
	
	protected static class DateEncoder implements ValueEncoder<Date> {
		@Override
		public void encode(StringBuilder builder, Date date, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			long time = date.getTime();
			
			CallPrototype prototype = prototypes.dateFromLong();

			String longLiteral = null;
			
			if (encodingContext.isHostedMode()) {
				longLiteral = prototypes.parseLong().evaluate("\"" + Long.toString(time) + "\"");
			}
			else {
				int parts[] = LongLib.getAsIntArray(time);
				longLiteral = "{l:" + parts[0] + ",m:" + parts[1] + ",h:" + parts[2]+"}";
			}
			
			
			builder.append(prototype.evaluate(longLiteral));
		}
	}
	
	protected static class BooleanEncoder implements ValueEncoder<Boolean> {
		@Override
		public void encode(StringBuilder builder, Boolean value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(prototypes.boxBoolean().evaluate(value.toString()));
		}
	}
	
	protected static class DoubleEncoder implements ValueEncoder<Double> {
		@Override
		public void encode(StringBuilder builder, Double value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(prototypes.boxDouble().evaluate(value.toString()));
		}
	}
	
	protected static class FloatEncoder implements ValueEncoder<Float> {
		@Override
		public void encode(StringBuilder builder, Float value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(prototypes.boxFloat().evaluate(value.toString()));
		}
	}
	
	protected static class DecimalEncoder implements ValueEncoder<BigDecimal> {
		@Override
		public void encode(StringBuilder builder, BigDecimal value, JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(prototypes.parseDecimal().evaluate("\"" + value.toString() + "\""));
		}
	}
	
	protected static class EnumEncoder implements ValueEncoder<Enum<?>> {
		public final static EnumEncoder instance = new EnumEncoder();
		@Override
		public void encode(StringBuilder builder, Enum<?> value,
				JavaScriptPrototypes prototypes,
				EncodingContext encodingContext) throws CodecException {
			String enumValueName = value.name();
			EnumType enumType = GMF.getTypeReflection().getEnumType(value.getDeclaringClass());
			String enumTypeVarName = encodingContext.aquireTypeVarName(enumType);
			builder.append(prototypes.parseEnum().evaluate(enumTypeVarName, "\"" + enumValueName + "\""));
		}
	}
	
	protected static class EntityEncoder implements ValueEncoder<GenericEntity> {
		public final static EntityEncoder instance = new EntityEncoder();
		@Override
		public void encode(StringBuilder builder, GenericEntity entity,
				JavaScriptPrototypes prototypes, EncodingContext encodingContext) throws CodecException {
			builder.append(encodingContext.aquireEntityVarName(entity));
		}
	}
	
	protected static abstract class CollectionEncoder<T extends Collection<?>> implements ValueEncoder<T> {
		protected void encode(StringBuilder builder, final T collection,
				CallPrototype buildCollectionPrototype,
				final EncodingContext encodingContext) throws CodecException {
			buildCollectionPrototype.evaluate(builder,
				new CodeWriter() {
					@Override
					public void write(StringBuilder builder) throws CodecException {
						builder.append("[");
						
						int i = 0;
						for (Object element: collection) {
							if (i++ > 0) builder.append(",");
							encodingContext.encodeValue(builder, element);
							
						}
						builder.append("]");
					}
				}
			);
		}
	}
	
	protected static class ListEncoder extends CollectionEncoder<List<?>> {
		public static final ValueEncoder<?> instance = new ListEncoder();

		@Override
		public void encode(StringBuilder builder, List<?> list,
				JavaScriptPrototypes prototypes,
				EncodingContext encodingContext) throws CodecException {
			encode(builder, list, prototypes.list(), encodingContext);
		}
	}
	
	protected static class SetEncoder extends CollectionEncoder<Set<?>> {
		public static final ValueEncoder<?> instance = new SetEncoder();
		@Override
		public void encode(StringBuilder builder, Set<?> set,
				JavaScriptPrototypes prototypes,
				EncodingContext encodingContext) throws CodecException {
			encode(builder, set, prototypes.set(), encodingContext);
		}
	}
	
	protected static class MapEncoder implements ValueEncoder<Map<?,?>> {
		public static final ValueEncoder<?> instance = new MapEncoder();
		@Override
		public void encode(StringBuilder builder, final Map<?,?> value,
				JavaScriptPrototypes prototypes,
				final EncodingContext encodingContext) throws CodecException {
			prototypes.map().evaluate(builder,
					new CodeWriter() {
						@Override
						public void write(StringBuilder builder) throws CodecException {
							builder.append("[");
							
							int i = 0;
							for (Map.Entry<?, ?> entry: value.entrySet()) {
								if (i++ > 0) builder.append(",");
								
								encodingContext.encodeValue(builder, entry.getKey());
								builder.append(",");
								encodingContext.encodeValue(builder, entry.getValue());
							}
							builder.append("]");
						}
					}
				);
		}
	}
	
	protected static Map<Class<?>, ValueEncoder<?>> encodersByClass = new HashMap<Class<?>, ValueEncoder<?>>(); 
	protected static DateEncoder dateEncoder = new DateEncoder(); 
	
	static {
		encodersByClass.put(String.class, new StringEncoder());
		encodersByClass.put(Integer.class, new IntegerEncoder());
		encodersByClass.put(Long.class, new LongEncoder());
		encodersByClass.put(Float.class, new FloatEncoder());
		encodersByClass.put(Double.class, new DoubleEncoder());
		encodersByClass.put(BigDecimal.class, new DecimalEncoder());
		encodersByClass.put(Boolean.class, new BooleanEncoder());
	}
	
}
