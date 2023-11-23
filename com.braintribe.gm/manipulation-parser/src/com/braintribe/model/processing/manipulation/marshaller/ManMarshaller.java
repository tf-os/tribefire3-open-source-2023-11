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
package com.braintribe.model.processing.manipulation.marshaller;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.processing.core.commons.comparison.AssemblyComparison;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.provider.Holder;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.braintribe.utils.format.lcd.FormatTool;

public class ManMarshaller implements CharacterMarshaller {
	
	private final static CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
	private final static AssemblyComparison stabilizationComparision = new AssemblyComparison(false, true);

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.defaultOptions);
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, value, options);
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling " + value, e);
		}
	}

	
	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		ManMarshallingContext context = new ManMarshallingContext();
		context.options = options;
		context.writer = writer;
		context.rootValue = value;
		context.entityVisitor = options.findOrNull(EntityVisitorOption.class);
		context.resultNaming = options.findOrDefault(ResultNaming.class, "$");
		context.instantiationClassifier = options.findOrDefault(ManInstantiationClassifier.class, e -> ManInstanceKind.create);

		try {
			context.marshall(value);
		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling " + value, e);
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.defaultOptions);
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
		return unmarshall(reader, options);
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		
		Consumer<? super GenericEntity> entityVisitor = options.findOrNull(EntityVisitorOption.class);
		
		EntityManager manager = new EntityManager() {
			
			@Override
			public <T extends GenericEntity> T findEntityByGlobalId(String globalId) {
				return null;
			}
			
			@Override
			public void deleteEntity(GenericEntity entity, DeleteMode deleteMode) {
				// noop
			}
			
			@Override
			public <T extends GenericEntity> T createRaw(EntityType<T> entityType) {
				T entity = entityType.createRaw();
				
				if (entityVisitor != null)
					entityVisitor.accept(entity);
				
				return entity;
			}
		};
		
		ParseResponse parseResponse = ManipulatorParser.parse(reader, manager, Gmml.manipulatorConfiguration());
		return parseResponse.lastAssignment;
	}
	
	private static char[] digits = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	
	private static String asString(int i) {
        char[] buf = new char[32];
        int charPos = 0;
        
        int divisor = digits.length;
           
        int digitIndex;
        do {
            digitIndex = i % divisor;
            digitIndex = Math.abs(digitIndex);
			buf[charPos++] = digits[digitIndex];
            i = i / divisor;
        } while (i != 0);
        
        return new String(buf, 0, charPos);
    }

	private static class EntityInfo {
		public String varName;
		public ManInstanceKind instanceKind;
		
		public EntityInfo(String varName, ManInstanceKind instanceKind) {
			super();
			this.varName = varName;
			this.instanceKind = instanceKind;
		}
		
	}
	
	private static class ManMarshallingContext {
		public Consumer<? super GenericEntity> entityVisitor;
		public Object rootValue;
		private String resultNaming;
		private GmSerializationOptions options;
		private Writer writer;
		private final Map<CustomType, String> typeVariables = new HashMap<>();
		private final Map<GenericEntity, EntityInfo> entityVariables = new HashMap<>();
		private final Set<String> vars = new HashSet<>();
		private int entityVarSeq = 0;
		private Function<GenericEntity, ManInstanceKind> instantiationClassifier;
		
		public String nextVar(CustomType type) {
			String shortName = type.getShortName();
			
			String var = shortName;
			
			if (!vars.add(var)) {
				int i = 1;
				
				while (!vars.add(var = shortName + i))
					i++;
			}
			
			return var;
		}
		
		public String nextVar(GenericEntity entity) {
			String candidate = null;
			
			if (entity == rootValue) {
				candidate = resultNaming;
			}
			else if (options.stabilizeOrder()) {
				String globalId = entity.getGlobalId();
				if (globalId == null) {
					globalId = "";
				}
					
				int hash = FNVHash.hash32(globalId);
				String name = "$" + asString(hash);

				candidate = name;
				
			}
			else {
				candidate = "$" + (entityVarSeq++);	
			}
			
			String var = candidate;
			
			if (!vars.add(var)) {
				int i = 1;
				
				while (!vars.add(var = candidate + '_' + i))
					i++;
			}
			
			return var;

		}
		
		public void marshall(Object value) throws IOException {
			indexValue(EssentialTypes.TYPE_OBJECT, value);

			writeTypes();
			
			Collection<Entry<GenericEntity, EntityInfo>> entrySet = entityVariables.entrySet();
			if (options.stabilizeOrder()) {
				entrySet = entrySet.stream().sorted(this::compareEntityEntries).collect(Collectors.toList());
			}
			
			writeInstantiations(entrySet);
			writeAssignments(entrySet);
			writeResult(value);
		}
		
		private void writeResult(Object value) throws IOException {
			if (!vars.contains(resultNaming)) {
				writer.write('\n');
				writer.write(resultNaming);
				writer.write(" = ");
				writeValue(BaseType.INSTANCE, value);
			}
		}

		private void writeInstantiations(Collection<Entry<GenericEntity, EntityInfo>> entrySet) throws IOException {
			ManInstanceKind lastKind = null;
			for (Map.Entry<GenericEntity, EntityInfo> entry: entrySet) {
				EntityInfo info = entry.getValue();
				
				if (lastKind != info.instanceKind) {
					writer.write('\n');
					lastKind = info.instanceKind;
				}
				
				GenericEntity entity = entry.getKey();
				EntityType<?> entityType = entity.entityType();
				
				String typeVariable = typeVariables.get(entityType);
				
				String varName = info.varName;
				writer.write(varName);
				
				int d = 7 - varName.length();
				for (int n = 0; n <  d; n++)
					writer.write(' ');
				
				writer.write(" = ");
				writer.write(typeVariable);
				
				switch (info.instanceKind) {
				case create:
					writer.write("()\n");
					break;
				case acquire:
					writer.write('[');
					writeString(entity.getGlobalId());
					writer.write("]\n");
					break;
				case lookup:
					writer.write('(');
					writeString(entity.getGlobalId());
					writer.write(")\n");
					break;
				default:
					throw new IllegalStateException("unsupported enum value: " + info.instanceKind);
				}
			}
		}
		
		private int compareCustomTypes(CustomType t1, CustomType t2) {
			int res = t1.getShortName().compareTo(t2.getShortName());
			
			if (res != 0)
				return res;
			
			return t1.getTypeSignature().compareTo(t2.getTypeSignature());
		}
		
		private int compareEntityEntries(Map.Entry<GenericEntity, EntityInfo> e1, Map.Entry<GenericEntity, EntityInfo> e2) {
			ManInstanceKind k1 = e1.getValue().instanceKind;
			ManInstanceKind k2 = e2.getValue().instanceKind;
			
			int res = k1.compareTo(k2);
			
			if (res != 0)
				return res;
			
			GenericEntity entity1 = e1.getKey();
			GenericEntity entity2 = e2.getKey();

			if (entity1 == entity2)
				return 0;
			
			if (entity1 == rootValue)
				return -1;
			
			if (entity2 == rootValue)
				return 1;
			
			EntityType<?> t1 = entity1.entityType();
			EntityType<?> t2 = entity2.entityType();
			
			res = compareCustomTypes(t1, t2);
			
			if (res != 0)
				return res;
			
			return e1.getValue().varName.compareTo(e2.getValue().varName);
		}

		private void writeAssignments(Collection<Entry<GenericEntity, EntityInfo>> entrySet) throws IOException {
			for (Map.Entry<GenericEntity, EntityInfo> entry: entrySet) {
				EntityInfo info = entry.getValue();
				
				if (info.instanceKind == ManInstanceKind.lookup)
					continue;
				
				GenericEntity entity = entry.getKey();
				EntityType<?> entityType = entity.entityType();
				
				boolean first = true;
				for (Property property: entityType.getProperties()) {
					
					Object propertyValue = property.get(entity);
					GenericModelType propertyType = property.getType();
					
					if (propertyValue == null || propertyType.isEmpty(propertyValue))
						continue;
					
					if (first) {
						writer.append('\n');
						writer.append(entityVariables.get(entity).varName);
						writer.append('\n');
						first = false;
					}
					
					writer.append('.');
					
					writer.append(property.getName());
					writer.append(" = ");
					writeValue(propertyType, propertyValue);
					writer.append('\n');
				}
				
			}
		}
		
		private void writeValue(GenericModelType type, Object value) throws IOException {
			if (value == null) {
				writer.append("null");
				return;
			}
			
			switch(type.getTypeCode()) {
			case objectType:
				writeValue(type.getActualType(value), value);
				return;
				
			case booleanType:
			case integerType:
				writer.append(value.toString());
				return;
				
			case stringType:
				writeString(value.toString());
				return;
				
			case doubleType:
				writer.append(value.toString());
				writer.append('D');
				return;
				
			case floatType:
				writer.append(value.toString());
				writer.append('F');
				return;
				
			case longType:
				writer.append(value.toString());
				writer.append('L');
				return;
				
			case decimalType:
				writer.append(value.toString());
				writer.append('B');
				return;
				
			case dateType:
				writeDate((Date)value);
				return;
				
			case entityType:
				writer.write(entityVariables.get(value).varName);
				return;
				
			case enumType:
				writeEnum((Enum<?>)value);
				return;
				
				
			case listType:
				writeList((ListType)type, (List<?>)value);
				return;
			case setType:
				writeSet((SetType)type, (Set<?>)value);
				return;
			case mapType:
				writeMap((MapType)type, (Map<?, ?>)value);
				return;
				
			default:
				return;		
			}
		}
		

		private void writeLinearCollection(LinearCollectionType type, Collection<?> collection) throws IOException {
			if (collection.isEmpty())
				return;

			GenericModelType elementType = type.getCollectionElementType();
			if (collection.size() == 1) {
				writeValue(elementType, first(collection));
				return;
			}

			boolean first = true;
			for (Object value : collection) {
				if (first)
					first = false;
				else
					writer.append(',');

				writer.write("\n ");
				writeValue(elementType, value);
			}

			writer.write("\n ");
		}

		public void writeList(ListType type, List<?> list) throws IOException {
			writer.append('[');
			writeLinearCollection(type, list);
			writer.append(']');
		}

		private void writeSet(SetType type, Collection<?> collection) throws IOException {
			if (options.stabilizeOrder()) {
				List<Object> sortedList = new ArrayList<>(collection);
				sortedList.sort(stabilizationComparision.getInternalComparator(type.getCollectionElementType()));
				collection = sortedList;
			}

			writer.append('(');
			writeLinearCollection(type, collection);
			writer.append(')');
		}

		private void writeMap(MapType type, Map<?, ?> map) throws IOException {
			Collection<? extends Map.Entry<?, ?>> entries = map.entrySet();
			
			if (options.stabilizeOrder()) {
				List<Map.Entry<?, ?>> sortedEntries = new ArrayList<>(entries);
				sortedEntries.sort(Comparator.
						<Map.Entry<?, ?>, Object>comparing(
								Map.Entry::getKey, 
								stabilizationComparision.getInternalComparator(type.getKeyType()
						)));
				
				entries = sortedEntries; 
			}
			
			writer.append('{');
			writeMapEntries(type, entries);
			writer.append("}");
		}

		private void writeMapEntries(MapType type, Collection<? extends Map.Entry<?, ?>> entries) throws IOException {
			if (entries.isEmpty())
				return;

			GenericModelType keyType = type.getKeyType();
			GenericModelType valueType = type.getValueType();

			if (entries.size() == 1) {
				Map.Entry<?, ?> e = first(entries);
				writeMapEntry(keyType, e.getKey(), valueType, e.getValue());
				return;
			}

			boolean first = true;
			for (Map.Entry<?, ?> entry : entries) {
				if (first)
					first = false;
				else
					writer.append(',');

				Object key = entry.getKey();
				Object value = entry.getValue();

				writer.write("\n ");
				writeMapEntry(keyType, key, valueType, value);
			}
			writer.write("\n");
		}
		
		private void writeMapEntry(GenericModelType keyType, Object key, GenericModelType valueType, Object value) throws IOException {
			writeValue(keyType, key);
			writer.append(':');
			writeValue(valueType, value);
		}

		private void writeEnum(Enum<?> enumConstant) throws IOException {
			EnumType enumType = GMF.getTypeReflection().getType(enumConstant);
			writer.write(typeVariables.get(enumType));
			writer.append("::");
			writer.append(enumConstant.name());
		}

		private void writeDate(Date date) throws IOException {
			// Format string to the parser date-string
			int year = dateFormat.getYear(date);
			int month = dateFormat.getMonth(date);
			int day = dateFormat.getDay(date);
			int hour = dateFormat.getHour(date);
			int minute = dateFormat.getMinute(date);
			int second = dateFormat.getSecond(date);
			int ms = dateFormat.getMilliSecond(date);
			int tz = Calendar.getInstance().getTimeZone().getOffset(date.getTime()) / (1000 * 60);
			
			writer.append("date(");
			writer.append(Integer.toString(year));
			writer.append("Y,");
			writer.append(Integer.toString(month));
			writer.append("M,");
			writer.append(Integer.toString(day));
			writer.append("D,");
			writer.append(Integer.toString(hour));
			writer.append("H,");
			writer.append(Integer.toString(minute));
			writer.append("m,");
			writer.append(Integer.toString(second));
			writer.append("S,");
			writer.append(Integer.toString(ms));
			writer.append("s,");
			
			
			boolean negativeTz = tz < 0;
			
			if (negativeTz) {
				tz *= -1;
			}
		
			int tzh = tz / 60;
			int tzm = tz % 60;
			
			if (negativeTz) {
				writer.append('-');
			}
			else {
				writer.append('+');
			}
			
			if (tzh < 10)
				writer.append('0');
			
			writer.append(Integer.toString(tzh));
			
			if (tzm < 10)
				writer.append('0');
			
			writer.append(Integer.toString(tzm));

			
			writer.append("Z)");
		}

		
		private void writeString(String string) throws IOException {
			writer.append('\'');
			ManipulationStringifier.writeEscaped(writer, string);
			writer.append('\'');
		}


		private void writeTypes() throws IOException {
			Collection<Map.Entry<CustomType, String>> types = typeVariables.entrySet();
			
			if (options.stabilizeOrder()) {
				types = types.stream().sorted((e1, e2) -> compareCustomTypes(e1.getKey(), e2.getKey())).collect(Collectors.toList());
			}
			
			for (Map.Entry<CustomType, String> entry: types) {
				writer.write(entry.getValue());
				writer.write(" = ");
				writer.write(entry.getKey().getTypeSignature());
				writer.write('\n');
			}
		}
		
		private void indexValue(GenericModelType type, Object value) {
			if (value == null)
				return;
			
			switch (type.getTypeCode()) {
			case objectType: indexValue(GMF.getTypeReflection().getType(value), value); return;
			case entityType: indexEntity((GenericEntity)value); return;
			case enumType: indexEnum((EnumType)type, (Enum<?>)value); return;
			case listType:
			case setType: indexCollection((LinearCollectionType)type, (Collection<?>)value); return;
			case mapType: indexMap((MapType)type, (Map<?, ?>)value); return;
			default: return;
			}
		}
		
		private void indexMap(MapType mapType, Map<?, ?> map) {
			GenericModelType keyType = mapType.getKeyType();
			GenericModelType valueType = mapType.getValueType();
			for (Map.Entry<?, ?> entry: map.entrySet()) {
				indexValue(keyType, entry.getKey());
				indexValue(valueType, entry.getValue());
			}
		}
		
		private void indexCollection(LinearCollectionType collectionType, Collection<?> collection) {
			GenericModelType elementType = collectionType.getCollectionElementType();
			for (Object element: collection) {
				indexValue(elementType, element);
			}
		}
		
		private void indexEnum(EnumType enumType, @SuppressWarnings("unused") Enum<?> enumConstant) {
			typeVariables.computeIfAbsent(enumType, this::nextVar);
		}
		
		private void indexEntity(GenericEntity entity) {
			EntityType<GenericEntity> entityType = entity.entityType();
			
			Holder<EntityInfo> holder = new Holder<>(); 
			
			entityVariables.computeIfAbsent(entity, e -> {
				if (entityVisitor != null)
					entityVisitor.accept(e);
				
				EntityInfo info = new EntityInfo(nextVar(e), instantiationClassifier.apply(e));
				holder.accept(info);
				return info;	
			});
			
			EntityInfo info = holder.get();
			if (info != null) {
				
				typeVariables.computeIfAbsent(entityType, this::nextVar);
				
				if (info.instanceKind == ManInstanceKind.lookup)
					return;
				
				for (Property property: entityType.getCustomTypeProperties()) {
					GenericModelType propertyType = property.getType();
					Object propertyValue = property.get(entity);
					
					indexValue(propertyType, propertyValue);
				}
			}
		}
	}
}

