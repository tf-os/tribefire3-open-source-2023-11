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
package com.braintribe.codec.marshaller.yaml;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.CollectionEndEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import com.braintribe.codec.marshaller.api.EntityFactory;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.PlaceholderSupport;
import com.braintribe.codec.marshaller.api.ScalarEntityParsers;
import com.braintribe.codec.marshaller.impl.EmptyScalarEntityParsers;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.model.bvd.convert.Convert;
import com.braintribe.model.bvd.convert.ToBoolean;
import com.braintribe.model.bvd.convert.ToDate;
import com.braintribe.model.bvd.convert.ToDecimal;
import com.braintribe.model.bvd.convert.ToDouble;
import com.braintribe.model.bvd.convert.ToEnum;
import com.braintribe.model.bvd.convert.ToFloat;
import com.braintribe.model.bvd.convert.ToInteger;
import com.braintribe.model.bvd.convert.ToLong;
import com.braintribe.model.bvd.convert.ToString;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.vde.parser.TemplateStringParser;

class StatefulYamlUnmarshaller {

	private static Map<Class<? extends Event>, BiConsumer<StatefulYamlUnmarshaller, ?>> decoders = new IdentityHashMap<>();

	private static <E extends Event> void registerDecoder(Class<E> type, BiConsumer<StatefulYamlUnmarshaller, ? super E> decoder) {
		decoders.put(type, decoder);
	}
	
	public static final Pattern TIMESTAMP_PATTERN = Pattern
			.compile("^(?:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?(?:[Tt]|[ \t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](?:\\.[0-9]*)?(?:[ \t]*(?:Z|[-+][0-9][0-9]?(?::[0-9][0-9])?))?)$");
    private Tag TIMESTAMP_TAG = new Tag(Tag.PREFIX + "timestamp");

	
	static {
		registerDecoder(StreamStartEvent.class, StatefulYamlUnmarshaller::decodeStreamStart);
		registerDecoder(StreamEndEvent.class, StatefulYamlUnmarshaller::decodeStreamEnd);
		registerDecoder(DocumentStartEvent.class, StatefulYamlUnmarshaller::decodeDocumentStart);
		registerDecoder(DocumentEndEvent.class, StatefulYamlUnmarshaller::decodeDocumentEnd);
		registerDecoder(MappingStartEvent.class, StatefulYamlUnmarshaller::decodeMappingStart);
		registerDecoder(MappingEndEvent.class, StatefulYamlUnmarshaller::decodeComplexEnd);
		registerDecoder(SequenceStartEvent.class, StatefulYamlUnmarshaller::decodeSequenceStart);
		registerDecoder(SequenceEndEvent.class, StatefulYamlUnmarshaller::decodeComplexEnd);
		registerDecoder(ScalarEvent.class, StatefulYamlUnmarshaller::decodeScalar);
		registerDecoder(AliasEvent.class, StatefulYamlUnmarshaller::decodeAlias);
	}
	
	private final Parser parser;
	private final GmDeserializationOptions options;
	private Function<EntityType<?>, GenericEntity> entityFactory;
	private Object rootValue;
	private final Deque<EventResultConsumer> consumerStack = new ArrayDeque<>();
	private final JsonScalarResolver resolver = new JsonScalarResolver();
	private boolean placeholdersEnabled = false;


	private final Consumer<? super GenericEntity> entityVisitor;


	private final Map<String, Anchoring> referencableValues = new HashMap<>();
	private int satisfiedAnchorCount = 0;
	private ScalarEntityParsers scalarEntityParsers;
	
	private interface EventResultConsumer extends BiConsumer<Event, Object> {
		GenericModelType getInferredType();
		void defer(Anchoring anchoring);
	}

	public StatefulYamlUnmarshaller(Reader reader, GmDeserializationOptions options) {
		LoadSettings loadSettings = LoadSettings.builder().build();
		this.parser = new ParserImpl(new StreamReader(reader, loadSettings), loadSettings);
		this.options = options;
		this.scalarEntityParsers = options.findOrDefault(ScalarEntityParsers.class, EmptyScalarEntityParsers.INSTANCE);
		this.placeholdersEnabled = options.findOrDefault(PlaceholderSupport.class, false);
		
		this.entityFactory = options.findOrNull(EntityFactory.class);
		
		if (entityFactory == null) {
			GmSession session = options.getSession();
			
			if (session != null) {
				entityFactory = session::createRaw;
			} else {
				entityFactory = EntityType::createRaw;
			}
			
		}

		if (options.getAbsentifyMissingProperties()) {
			entityFactory = entityFactory.andThen(StatefulYamlUnmarshaller::absentifyAllProperties); 
		}
		
		this.entityVisitor = options.findOrNull(EntityVisitorOption.class);
		this.resolver.addImplicitResolver(TIMESTAMP_TAG, TIMESTAMP_PATTERN, "0123456789");
	}
	
	private Anchoring acquireAnchoring(String name) {
		return referencableValues.computeIfAbsent(name, Anchoring::new);
	}
	
	private void satisfyAnchor(Event event, String name, Object value) {
		acquireAnchoring(name).setValue(event, value);
		satisfiedAnchorCount++;
	}
	
	private static GenericEntity absentifyAllProperties(GenericEntity e) {
		for (Property p : e.entityType().getProperties()) {
			p.setAbsenceInformation(e, GMF.absenceInformation());
		}
		return e;
	}
	
	private static String isCustomTag(String value) {
		if (value != null && value.length() >= 2 && value.charAt(0) == '!' && value.charAt(1) != '!')
			return value.substring(1);
		else
			return null;
	}

	protected GenericEntity createEntity(EntityType<?> entityType) {
		GenericEntity entity = entityFactory.apply(entityType);

		if (entityVisitor != null)
			entityVisitor.accept(entity);
		
		return entity;
	}

	abstract class AbstractDecoder implements EventResultConsumer {
		abstract Object getValue();
		abstract void notifyComplexEnd();
	}
	
	static class BufferMapEntry {
		Object key;
		Object value;
		;
	}
	
	abstract class MappingDecoder<K> extends AbstractDecoder {
		private List<BufferMapEntry> buffer = new ArrayList<>();
		private BufferMapEntry curEntry;
		private int openOperations = 1;

		public MappingDecoder() {

		}
		
		abstract K processKey(Event event, Object key);
		
		abstract void putValue(K key, Object value);

		@Override
		public void accept(Event event, Object value) {
			if (curEntry == null) {
				curEntry = new BufferMapEntry();
				curEntry.key = processKey(event, value);
				buffer.add(curEntry);
			}
			else {
				curEntry.value = value;
				curEntry = null;
			}
		}
		
		@Override
		public void defer(Anchoring anchoring) {
			if (curEntry == null) {
				BufferMapEntry entry = new BufferMapEntry();
				buffer.add(entry);
				curEntry = entry;
				openOperations++;
				
				anchoring.addValueConsumer(k -> {
					entry.key = k;
					notifyOperation();
				});
				
			} else {
				BufferMapEntry entry = curEntry;
				curEntry = null;
				openOperations++;
				
				anchoring.addValueConsumer(k -> {
					entry.value = k;
					notifyOperation();
				});
			}
		}
		
		@Override
		void notifyComplexEnd() {
			notifyOperation();
		}
		
		private void notifyOperation() {
			if (--openOperations > 0)
				return;
			
			for (BufferMapEntry entry: buffer) {
				putValue((K)entry.key, entry.value);
			}
		}

		abstract GenericModelType getKeyType();
		abstract GenericModelType getValueType();
		
		@Override
		public GenericModelType getInferredType() {
			return curEntry != null? 
					getValueType(): 
					getKeyType();
		}
	}

	enum EnvelopeField {
		pool, value 
	}
	
	class PooledDecoder extends MappingDecoder<EnvelopeField> {
		private GenericModelType inferredType;
		private Object value;
		private List<GenericEntity> pool;
		private EnvelopeField curKey;
		
		public PooledDecoder(GenericModelType inferredType) {
			super();
			this.inferredType = inferredType;
		}
		
		@Override
		EnvelopeField processKey(Event event, Object key) {
			try {
				return curKey = EnvelopeField.valueOf((String)key);
			}
			catch (NullPointerException e) {
				throw new ParserException("Envelope does not support null keys", event.getStartMark());
			}
			catch (IllegalArgumentException e) {
				throw new ParserException("Envelope has no property " + (String)key, event.getStartMark());
			}
			catch (ClassCastException e) {
				throw new ParserException("Envelope does support the key type " + key.getClass(), event.getStartMark());
			}
		}
		
		@Override
		GenericModelType getKeyType() {
			return EssentialTypes.TYPE_STRING;
		}
		
		@Override
		GenericModelType getValueType() {
			switch (curKey) {
			case value: return inferredType;
			case pool: return GMF.getTypeReflection().getListType(GenericEntity.T);
			default:
				// this is an actual exception which should really not happen and is therefore no ParserException used for reasoning
				throw new IllegalStateException("unexpected key " + curKey + " for pooled envelope");
			}
		}
		
		@Override
		void putValue(EnvelopeField key, Object value) {
			switch (key) {
			case value:
				this.value = value;
				break;
			case pool:
				this.pool = (List<GenericEntity>) value;
				break;
			default:
				// this is an actual exception which should really not happen and is therefore no ParserException used for reasoning
				throw new IllegalStateException("unexpected key " + key + " for pooled envelope");
			}
		}
		
		@Override
		Object getValue() {
			return value;
		}
	}

	class MapDecoder extends MappingDecoder<Object> {
		private final Map<Object, Object> map;
		private final MapType mapType;

		public MapDecoder(MapType mapType) {
			super();
			this.mapType = mapType;
			this.map = mapType.createPlain();
		}
		
		@Override
		Object processKey(Event event, Object key) {
			return key;
		}

		@Override
		void putValue(Object key, Object value) {
			if (key instanceof Collection || key instanceof Map) {
				throw new IllegalArgumentException("Can't add an element of type Collection or Map to a Map" + key);
			}
			map.put(key, value);
		}
		
		@Override
		Object getValue() {
			return map;
		}

		@Override
		GenericModelType getKeyType() {
			return mapType.getKeyType();
		}

		@Override
		GenericModelType getValueType() {
			return mapType.getValueType();
		}
	}

	class SetDecoder extends MappingDecoder<Object> {
		private final Set<Object> set;
		private final SetType setType;

		public SetDecoder(SetType setType) {
			super();
			this.setType = setType;
			this.set = setType.createPlain();
		}
		
		@Override
		Object processKey(Event event, Object key) {
			return key;
		}

		@Override
		void putValue(Object key, Object value) {
			if (key instanceof Collection || key instanceof Map) {
				throw new IllegalArgumentException("Can't add an element of type Collection or Map to a Set: " + key);
			}
			set.add(key);
		}

		@Override
		Object getValue() {
			return set;
		}

		@Override
		GenericModelType getKeyType() {
			return setType.getCollectionElementType();
		}

		@Override
		GenericModelType getValueType() {
			return BaseType.INSTANCE;
		}
	}
	
	static class PropertyDecodingInfo {
		Property property;
		boolean absent;
		Optional<Mark> mark;
		
		public PropertyDecodingInfo(Optional<Mark> mark, Property property, boolean absent) {
			super();
			this.mark = mark;
			this.property = property;
			this.absent = absent;
		}
	}

	
	class EntityDecoder extends MappingDecoder<PropertyDecodingInfo> {
		private final EntityType<?> entityType;
		private final GenericEntity entity;
		private PropertyDecodingInfo curPropertyDecodingInfo;

		public EntityDecoder(EntityType<?> entityType) {
			super();
			this.entityType = entityType;
			this.entity = createEntity(entityType);
		}
		
		PropertyDecodingInfo processKey(Event event, Object key) {
			String propertyName = (String) key;
			
			boolean absent = false;
			
			if (propertyName.endsWith("?")) {
				propertyName = propertyName.substring(0, propertyName.length() - 1);
				absent = true;
			}

			Property property = entityType.findProperty(propertyName);
			
			if(property == null && !isPropertyLenient()) {
				throw new ParserException("Property '" + propertyName + "' does not exist in entity type " + entityType.getTypeSignature(), event.getStartMark());
			}
			
			return curPropertyDecodingInfo = new PropertyDecodingInfo(event.getStartMark(), property, absent);
		}
		
		@Override
		void putValue(PropertyDecodingInfo key, Object value) {
			Property property = key.property;
			boolean absent = key.absent;
			
			if (property != null) {
				if (absent) {
					if (value == null)
						property.setAbsenceInformation(entity, GMF.absenceInformation());
					else
						property.setAbsenceInformation(entity, (AbsenceInformation)value);
				}
				else {
					if (placeholdersEnabled && VdHolder.isVdHolder(value)) {
						property.setVd(entity, ((VdHolder)value).vd);
					}
					else {
						property.set(entity, value);
					}
				}
			} 
		}

		@Override
		Object getValue() {
			return entity;
		}

		@Override
		GenericModelType getKeyType() {
			return EssentialTypes.TYPE_STRING;
		}

		@Override
		GenericModelType getValueType() {
			boolean absent = curPropertyDecodingInfo.absent;
			Property property = curPropertyDecodingInfo.property;
			if (absent)
				return AbsenceInformation.T;
			else
				return property != null ? property.getType() : BaseType.INSTANCE;
		}
	}
	
	private class RootConsumer implements EventResultConsumer {
		@Override
		public void accept(Event event, Object t) {
			rootValue = t;
		}
		
		@Override
		public GenericModelType getInferredType() {
			GenericModelType inferredRootType = options.getInferredRootType();
			if (inferredRootType == null)
				inferredRootType = BaseType.INSTANCE;
			return inferredRootType;
		}
		
		@Override
		public void defer(Anchoring anchoring) {
			// noop
		}
	}

	private void decodeStreamStart(StreamStartEvent event) {
		GenericModelType inferredRootType = options.getInferredRootType();
		if (inferredRootType == null)
			inferredRootType = BaseType.INSTANCE;

		consumerStack.push(new RootConsumer());
	}

	private void decodeStreamEnd(StreamEndEvent event) {
		consumerStack.pop();
	}

	private void decodeDocumentStart(DocumentStartEvent event) {
		// noop
	}
	private void decodeDocumentEnd(DocumentEndEvent event) {
		// noop
	}

	private void decodeMappingStart(MappingStartEvent event) {
		String tagStr = event.getTag().orElse(null);
		
		if ("!pooled".equals(tagStr)) {
			GenericModelType inferredType = consumerStack.peek().getInferredType();
			consumerStack.push(new PooledDecoder(inferredType));
			return;
		}
			
		GenericModelType inferredType = getInferredType(tagStr);

		if (inferredType == BaseType.INSTANCE) {
			if (Tag.SET.getValue().equals(tagStr))
				inferredType = EssentialCollectionTypes.TYPE_SET;
			else
				inferredType = EssentialCollectionTypes.TYPE_MAP;
		}

		MappingDecoder<?> mapDecoder = null;

		switch (inferredType.getTypeCode()) {
			case mapType:
				mapDecoder = new MapDecoder((MapType) inferredType);
				break;

			case entityType:
				mapDecoder = new EntityDecoder((EntityType<?>) inferredType);
				break;

			case setType:
				mapDecoder = new SetDecoder((SetType) inferredType);
				break;

			default:
				throw new ParserException("invalid type for map:" + inferredType, event.getStartMark());
		}

		Optional<Anchor> anchorOptional = event.getAnchor();
		if (anchorOptional.isPresent()) {
			Object value = mapDecoder.getValue();
			satisfyAnchor(event, anchorOptional.get().getValue(), value);
		}

		consumerStack.push(mapDecoder);
	}

	static class SequenceBufferEntry {
		Object value;

		public SequenceBufferEntry(Object value) {
			super();
			this.value = value;
		}

		public SequenceBufferEntry() {
			super();
		}
	}
	
	private class SequenceDecoder extends AbstractDecoder {
		private final Collection<Object> collection;
		private final LinearCollectionType collectionType;
		private List<SequenceBufferEntry> buffer = new ArrayList<>();
		private int openOperations = 1;
		
		public SequenceDecoder(LinearCollectionType collectionType, Collection<Object> collection) {
			super();
			this.collectionType = collectionType;
			this.collection = collection;
		}
		
		@Override
		public void accept(Event event, Object value) {
			buffer.add(new SequenceBufferEntry(value));
		}
		
		@Override
		public void defer(Anchoring anchoring) {
			openOperations++;
			SequenceBufferEntry entry = new SequenceBufferEntry();
			buffer.add(entry);
			
			anchoring.addValueConsumer(v -> {
				entry.value = v;
				notifyComplexEnd();
			});
		}
		
		void notifyComplexEnd() {
			notifyOperation();
		}
		
		private void notifyOperation() {
			if (--openOperations > 0)
				return;
			
			for (SequenceBufferEntry entry: buffer) {
				collection.add(entry.value);
			}
		}
		
		@Override
		Object getValue() {
			return collection;
		}
		
		@Override
		public GenericModelType getInferredType() {
			return collectionType.getCollectionElementType();
		}
		
	}
	
	private void decodeSequenceStart(SequenceStartEvent event) {
		String tagStr = event.getTag().orElse(null);
		GenericModelType inferredType = getInferredType(tagStr);

		if (inferredType == EssentialTypes.TYPE_OBJECT) {
			if (Tag.SET.getValue().equals(tagStr)) {
				inferredType = EssentialCollectionTypes.TYPE_SET;
			} else {
				inferredType = EssentialCollectionTypes.TYPE_LIST;
			}
		}

		LinearCollectionType collectionType = null;

		switch (inferredType.getTypeCode()) {
			case listType:
			case setType:
				collectionType = (LinearCollectionType) inferredType;
				break;

			default:
				throw new ParserException("cannot infer sequence type from: " + inferredType, event.getStartMark());
		}

		Collection<Object> collection = collectionType.createPlain();

		Optional<Anchor> anchorOptional = event.getAnchor();
		if (anchorOptional.isPresent()) {
			satisfyAnchor(event, anchorOptional.get().getValue(), collection);
		}

		consumerStack.push(new SequenceDecoder(collectionType, collection));
	}

	private void decodeComplexEnd(CollectionEndEvent event) {
		AbstractDecoder decoder = (AbstractDecoder) consumerStack.pop();
		decoder.notifyComplexEnd();
		EventResultConsumer consumer = consumerStack.peek();
		consumer.accept(event, decoder.getValue());
	}

	private void decodeAlias(AliasEvent event) {
		Anchoring anchoring = acquireAnchoring(event.getAnchor().get().getValue());
		if (anchoring.resolved)
			consumerStack.peek().accept(event, anchoring.value);
		else {
			consumerStack.peek().defer(anchoring);
		}
			
	}

	private void decodeScalar(ScalarEvent event) {
		String tagStr = event.getTag().orElse(null);
		GenericModelType inferredType = getInferredType(tagStr);
		String valueAsString = event.getValue();
		
		ImplicitTuple implicitTuple = event.getImplicit();

		if (inferredType.isBase()) {
			Tag tag = tagStr != null?
					new Tag(tagStr):
					resolver.resolve(valueAsString, event.getImplicit().canOmitTagInPlainScalar());
			
			inferredType = tagToType.getOrDefault(tag.getValue(), EssentialTypes.TYPE_STRING);
		}

		Object value = null;
		
		if (!(implicitTuple.canOmitTagInPlainScalar() && valueAsString.equals("null"))) {
			try {
				value = parseScalar(event, inferredType, valueAsString);
			} catch (Exception e) {
				throw new ParserException("Can't parse value '" + valueAsString + "' to type " + inferredType.getTypeName(), event.getStartMark());
			}
		}

		Optional<Anchor> anchorOptional = event.getAnchor();
		if (anchorOptional.isPresent()) {
			satisfyAnchor(event, anchorOptional.get().getValue(), value);
		}
		
		consumerStack.peek().accept(event, value);
	}

	private Object parseScalar(ScalarEvent event, GenericModelType inferredType, String valueAsString) {
		if (placeholdersEnabled) {
			Object parsedValue = TemplateStringParser.parse(valueAsString);
			
			if (parsedValue.getClass() != String.class) {
				ValueDescriptor vd = buildConversion(event, inferredType, parsedValue);
				return VdHolder.newInstance(vd);
			}
			else {
				valueAsString = (String)parsedValue;
			}
		}
		
		TypeCode typeCode = inferredType.getTypeCode();
		switch (typeCode) {
			case stringType:
				return valueAsString;
			case dateType:
				return parseDate(event, valueAsString);
			case enumType:
			case booleanType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
				return ScalarParsers.parse((ScalarType) inferredType, valueAsString);
			case entityType:
				if (valueAsString != null && !valueAsString.isEmpty()) {
					if (inferredType == AbsenceInformation.T && valueAsString.equals("absent")) {
						return GMF.absenceInformation();
					}
					else {
						Optional<? extends Function<String, ?>> optionalParser = scalarEntityParsers.findEntityScalarParser((EntityType<?>)inferredType);
						if (optionalParser.isPresent()) {
							return optionalParser.get().apply(valueAsString);
						}
						else
							throw new ParserException("type " + inferredType + " cannot be decoded from a scalar value", event.getStartMark());
					}
				}
				else { 
					return createEntity((EntityType<?>)inferredType);
				}
			default:
				throw new ParserException("type " + inferredType + " cannot be decoded from a scalar value", event.getStartMark());
		}
	}

	private ValueDescriptor buildConversion(ScalarEvent event, GenericModelType inferredType, Object parsedValue) {
		TypeCode typeCode = inferredType.getTypeCode();
		
		final Convert convert;
		
		switch (typeCode) {
			case stringType: convert = ToString.T.create(); break;
			case dateType: convert = ToDate.T.create(); break;
			case enumType: convert = ToEnum.T.create(); break;
			case booleanType: convert = ToBoolean.T.create(); break;
			case decimalType: convert = ToDecimal.T.create(); break;
			case doubleType: convert = ToDouble.T.create(); break;
			case floatType: convert = ToFloat.T.create(); break;
			case integerType: convert = ToInteger.T.create(); break;
			case longType: convert = ToLong.T.create(); break;
			default:
				throw new ParserException("type " + inferredType + " cannot be decoded from a scalar value with placeholders", event.getStartMark());
		}
		
		convert.setOperand(parsedValue);
		return convert;
	}

	public Object decode() {
		Event event = null;
		while (parser.hasNext()) {
			event = parser.next();
			
			BiConsumer<StatefulYamlUnmarshaller, Event> decoder = (BiConsumer<StatefulYamlUnmarshaller, Event>) decoders.get(event.getClass());
			decoder.accept(this, event);
		}
		
		if (satisfiedAnchorCount != referencableValues.size()) {
			String anchors = referencableValues.values().stream().filter(a -> !a.resolved).map(a -> a.name).collect(Collectors.joining(", "));
			throw new ParserException("The following anchors where referenced but never defined: " + anchors, event.getEndMark());
		}

		return rootValue;
	}
	
	public Maybe<Object> decodeReasoned() {
		try {
			while (parser.hasNext()) {
				Event event = parser.next();
				
				BiConsumer<StatefulYamlUnmarshaller, Event> decoder = (BiConsumer<StatefulYamlUnmarshaller, Event>) decoders.get(event.getClass());
				decoder.accept(this, event);
			}
		} catch (ParserException e) {
			return Reasons.build(ParseError.T).text(e.getMessage()).toMaybe();
		}
		
		if (satisfiedAnchorCount != referencableValues.size()) {
			String anchors = referencableValues.values().stream().filter(a -> !a.resolved).map(a -> a.name).collect(Collectors.joining(", "));
			return Reasons.build(ParseError.T).text("The following anchors where referenced but never defined: " + anchors).toMaybe();
		}
		
		return Maybe.complete(rootValue);
	}

	private GenericModelType getInferredType(String tag) {
		String typeSignature = isCustomTag(tag);
		
		if (typeSignature != null)
			return GMF.getTypeReflection().getType(typeSignature);
		else
			return consumerStack.peek().getInferredType();
	}
	

	private boolean isPropertyLenient() {
		return options.getDecodingLenience() != null && options.getDecodingLenience().isPropertyLenient();
	}

	private static Map<String, GenericModelType> tagToType = new HashMap<>();


	{
		tagToType.put(Tag.INT.getValue(), EssentialTypes.TYPE_INTEGER);
		tagToType.put(Tag.BOOL.getValue(), EssentialTypes.TYPE_BOOLEAN);
		tagToType.put(Tag.STR.getValue(), EssentialTypes.TYPE_STRING);
		tagToType.put(Tag.FLOAT.getValue(), EssentialTypes.TYPE_DOUBLE);
		tagToType.put(TIMESTAMP_TAG.getValue(), EssentialTypes.TYPE_DATE);
	}

	private final static Pattern TIMESTAMP_REGEXP = Pattern.compile(
			"^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)(?:(?:[Tt]|[ \t]+)([0-9][0-9]?):([0-9][0-9]):([0-9][0-9])(?:\\.([0-9]*))?(?:[ \t]*(?:Z|([-+][0-9][0-9]?)(?::([0-9][0-9])?)?))?)?$");
	private final static Pattern YMD_REGEXP = Pattern.compile("^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)$");

	public Date parseDate(ScalarEvent event, String dateAsString) {
		Matcher match = YMD_REGEXP.matcher(dateAsString);
		if (match.matches()) {
			String year_s = match.group(1);
			String month_s = match.group(2);
			String day_s = match.group(3);
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calendar.clear();
			calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
			// Java's months are zero-based...
			calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1); // x
			calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
			return calendar.getTime();
		} else {
			match = TIMESTAMP_REGEXP.matcher(dateAsString);
			if (!match.matches()) {
				throw new ParserException("Unexpected timestamp: " + dateAsString, event.getStartMark());
			}
			String year_s = match.group(1);
			String month_s = match.group(2);
			String day_s = match.group(3);
			String hour_s = match.group(4);
			String min_s = match.group(5);
			// seconds and milliseconds
			String seconds = match.group(6);
			String millis = match.group(7);
			if (millis != null) {
				seconds = seconds + "." + millis;
			}
			double fractions = Double.parseDouble(seconds);
			int sec_s = (int) Math.round(Math.floor(fractions));
			int usec = (int) Math.round((fractions - sec_s) * 1000);
			// timezone
			String timezoneh_s = match.group(8);
			String timezonem_s = match.group(9);
			TimeZone timeZone;
			if (timezoneh_s != null) {
				String time = timezonem_s != null ? ":" + timezonem_s : "00";
				timeZone = TimeZone.getTimeZone("GMT" + timezoneh_s + time);
			} else {
				// no time zone provided
				timeZone = TimeZone.getTimeZone("UTC");
			}
			Calendar calendar = Calendar.getInstance(timeZone);
			calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
			// Java's months are zero-based...
			calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1);
			calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour_s));
			calendar.set(Calendar.MINUTE, Integer.parseInt(min_s));
			calendar.set(Calendar.SECOND, sec_s);
			calendar.set(Calendar.MILLISECOND, usec);
			return calendar.getTime();
		}
	}
	
	private static abstract class AnchorResolutionListener {
		public AnchorResolutionListener predecessor;
		
		public final void onResolve(Object value) {
			AnchorResolutionListener currentListener = this;
			
			do {
				currentListener.satisfy(value);
				currentListener = currentListener.predecessor;
			}
			while (currentListener != null);
		}
		
		protected abstract void satisfy(Object value);
	}
	
	private static class Anchoring {
		public String name;
		public Object value;
		public boolean resolved;
	
		private AnchorResolutionListener resolutionListener;
		
		public Anchoring(String name) {
			this.name = name;
		}
		
		public void setValue(Event event, Object value) {
			if (resolved)
				throw new ParserException("Duplicate anchor definition: " + name, event.getStartMark());
			
			this.value = value;
			this.resolved = true;
			if (resolutionListener != null) {
				resolutionListener.onResolve(value);
				resolutionListener = null;
			}
		}
		
		public void addValueConsumer(Consumer<Object> consumer) {
			addResolutionListener(new AnchorResolutionListener() {
				@Override
				protected void satisfy(Object value) {
					consumer.accept(value);
				}
			});
		}
		
		@Deprecated
		public void addResolutionListener(AnchorResolutionListener resolutionListener) {
			resolutionListener.predecessor = this.resolutionListener;
			this.resolutionListener = resolutionListener;
		}
	}
}