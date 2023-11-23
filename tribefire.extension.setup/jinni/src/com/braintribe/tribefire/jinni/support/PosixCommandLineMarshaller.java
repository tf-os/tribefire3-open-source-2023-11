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
package com.braintribe.tribefire.jinni.support;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.jinni.api.JinniOptions;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.utils.DateTools;

public class PosixCommandLineMarshaller {
	private static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder().optionalStart()
			.appendPattern("yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]").optionalEnd().optionalStart().appendPattern("yyyyMMdd['T'HH[mm[ss[SSS]]]][Z]")
			.optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
			.parseDefaulting(ChronoField.OFFSET_SECONDS, 0).toFormatter();

	private class StatefulPosixCommandLineMarshaller {
		Deque<Pair<String, GenericEntity>> enqueued = new ArrayDeque<>();
		Map<GenericEntity, String> references = new HashMap<>();
		List<String> args = new ArrayList<>();
		int nameSeq = 0;

		String createReference(GenericEntity entity) {
			return references.computeIfAbsent(entity, k -> {
				String referenceName = "e" + nameSeq++;
				enqueued.addLast(Pair.of(referenceName, entity));
				return "@" + referenceName;
			});
		}

		void marshallToArgs(List<GenericEntity> entities) {

			for (GenericEntity entity : entities)
				enqueued.addLast(Pair.of(null, entity));

			boolean first = true;

			while (!enqueued.isEmpty()) {
				Pair<String, GenericEntity> entry = enqueued.removeFirst();

				if (first) {
					first = false;
				} else {
					String name = entry.first();
					if (name != null)
						args.add(":" + name);
					else
						args.add(":");
				}

				GenericEntity entity = entry.getSecond();
				marshallValue(entity.type(), entity, null, true);
			}
		}

		void marshallToArgs(GenericEntity entity) {
			EntityType<GenericEntity> entityType = entity.entityType();

			args.add(entityType.getTypeSignature());

			for (Property property : entityType.getProperties()) {
				GenericModelType propertyType = property.getType();
				Object value = property.get(entity);
				if (value != null)
					marshallValue(propertyType, value, property.getName(), false);
			}
		}

		void marshallValue(GenericModelType type, Object value, String valueName, boolean topLevel) {
			switch (type.getTypeCode()) {
				case dateType:
					if (value != null) {
						if (valueName != null)
							args.add("--" + valueName);

						args.add(DateTools.encode((Date) value, DATETIME_FORMATTER));
					}
					break;

				case booleanType:
				case decimalType:
				case doubleType:
				case floatType:
				case integerType:
				case enumType:
				case longType:
				case stringType:
					if (value != null) {
						if (valueName != null)
							args.add("--" + valueName);

						args.add(value.toString());
					}

					break;

				case objectType:
					if (value != null) {
						if (valueName != null)
							args.add("--" + valueName);

						GenericModelType actualType = type.getActualType(value);

						if (actualType.isScalar())

							args.add(actualType.getTypeSignature());
						marshallValue(actualType, value, null, topLevel);
					}
					break;

				case entityType:
					GenericEntity entity = (GenericEntity) value;
					if (topLevel) {
						marshallToArgs(entity);
					} else {
						args.add(createReference(entity));
					}
					break;

				case mapType:
					MapType mapType = (MapType) type;
					GenericModelType keyType = mapType.getKeyType();
					GenericModelType valueType = mapType.getValueType();

					Map<?, ?> map = (Map<?, ?>) value;

					if (map == null || map.isEmpty())
						break;

					if (valueName != null)
						args.add("--" + valueName);

					for (Map.Entry<?, ?> entry : map.entrySet()) {
						marshallValue(keyType, entry.getKey(), null, false);
						marshallValue(valueType, entry.getValue(), null, false);
					}

					break;

				case listType:
				case setType:
					LinearCollectionType collectionType = (LinearCollectionType) type;
					GenericModelType elementType = collectionType.getCollectionElementType();

					Collection<?> collection = (Collection<?>) value;

					if (collection == null || collection.isEmpty())
						break;

					if (valueName != null)
						args.add("--" + valueName);

					for (Object element : collection) {
						marshallValue(elementType, element, null, false);
					}

					break;

				default:
					break;

			}

		}
	}

	public List<String> marshall(List<GenericEntity> entities) {
		StatefulPosixCommandLineMarshaller statefullMarshaller = new StatefulPosixCommandLineMarshaller();
		statefullMarshaller.marshallToArgs(entities);
		return statefullMarshaller.args;
	}

	public static void main(String[] args) {

		JinniOptions options = JinniOptions.T.create();
		options.setAlias("foo");

		GmMetaModel model1 = GmMetaModel.T.create();
		model1.setName("ModelOne");
		GmMetaModel model2 = GmMetaModel.T.create();
		model2.setName("ModelTwo");

		GmMetaModel model = GmMetaModel.T.create();
		model.setName("MyModel");
		model.getDependencies().add(model1);
		model.getDependencies().add(model2);

		List<GenericEntity> rootValues = new ArrayList<>();
		rootValues.add(options);
		rootValues.add(model);

		System.out.println(new PosixCommandLineMarshaller().marshall(rootValues).stream().collect(Collectors.joining(" ")));
	}
}