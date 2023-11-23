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
package com.braintribe.model.processing.core.commons.hashing;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.commons.datasink.DataSink;
import com.braintribe.utils.StringTools;

/**
 * This class is able to generate MD5 hashes for single entities or list of entities or set of entities.
 * 
 * It uses the property names and values of filtered scalar typed properties of an entity to build the hash
 * with a {@link MessageDigest}
 * 
 * @author Dirk Scheffler
 *
 * @param <T> the actual GenericEntity type
 */
public class EntityHashing<T extends GenericEntity> {
	EntityType<T> entityType;
	List<PropertyWriter> writers;
	Comparator<T> entityComparator;
	
	public Comparator<T> getComparator() {
		return entityComparator;
	}
	
	/**
	 * Hashes one entity with MD5 and returns the hash as {@link String}
	 * @param entity the entity whose hash should be calculated
	 */
	public String hashEntity(T entity) {
		return hash(dataOut -> {
			for (PropertyWriter writer: writers) {
				writer.write(dataOut, entity);
			}
		});
	}

	/**
	 * Calculates the MD5 hash of each entity with MD5 and then a super hash from these hashes using the original list order 
	 * @param entities the list of entities whose hash should be calculated
	 */
	public String hashEntities(List<? extends T> entities) {
		return hashStrings(entities.stream().map(this::hashEntity));
	}
	
	/**
	 * Calculates the MD5 hash of each entity with MD5 and then sorts the hashes alphabetically using this order to calculate a super hash from these hashes
	 * @param entities the list of entities whose hash should be calculated
	 */
	public String hashEntitiesSorted(Collection<? extends T> entities) {
		return hashStrings(entities.stream().map(this::hashEntity).sorted());
	}
	
	private String hashStrings(Stream<String> texts) {
		return hash(sink -> texts.forEach(s -> sink.writeString(s)));
	}
	
	public static <T extends GenericEntity> EntityHashing<T> hashGenerator(EntityType<T> entityType) {
		return hashGenerator(entityType, p -> true);
	}

	public static <T extends GenericEntity> EntityHashing<T> hashGenerator(EntityType<T> entityType, Predicate<? super Property> propertyFilter) {
		
		List<Property> orderedProperties = entityType.getProperties().stream() //
		  .filter(p -> p.getType().isScalar()) //
		  .filter(propertyFilter) //
		  .sorted(Comparator.comparing(Property::getName))
		  .collect(Collectors.toList());
		
		List<PropertyWriter> writers = orderedProperties.stream() //
		  .map(p -> new PropertyWriter(p)) //
		  .collect(Collectors.toList());
		
		EntityHashing<T> hashing = new EntityHashing<>();
		hashing.writers = writers;
		hashing.entityType = entityType;
		hashing.entityComparator = createCascadedComparator(orderedProperties);
		
		return hashing;
	}

	private static <T extends GenericEntity> Comparator<T> createCascadedComparator(List<Property> orderedProperties) {
		Comparator<T> comp = null;
		
		for (Property property: orderedProperties) {
			
			PropertyComparator<T> propertyComp = new PropertyComparator<>(property);
		
			if (comp == null)
				comp = propertyComp;
			else
				comp = comp.thenComparing(propertyComp);
		}
		
		return comp;
	}

	private static String hash(CheckedConsumer<DataSink, IOException> dataProducer) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			DataSink sink = DataSink.from(digest);
			dataProducer.accept(sink);
			return StringTools.toHex(digest.digest());
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}
	
	/**
	 * This class handles writing of a name and value of a certain property of a given entity according to the property type
	 * @author Dirk Scheffler
	 */
	private static class PropertyWriter {
		private final Property property;
		private BiConsumer<DataSink, Object> valueWriter;
		
		public PropertyWriter(Property property) {
			super();
			this.property = property;
			
			switch (property.getType().getTypeCode()) {
				case booleanType: init(DataSink::writeBoolean); break;
				case integerType: init(DataSink::writeInt); break;
				case longType: init(DataSink::writeLong); break;
				case floatType: init(DataSink::writeFloat); break;
				case doubleType: init(DataSink::writeDouble); break;
				case decimalType: init(DataSink::writeDecimal); break;
				case stringType: init(DataSink::writeString); break;
				case dateType: init(DataSink::writeDate); break;
				case enumType: init(DataSink::writeEnum); break;
				default:
					throw new IllegalStateException("Unsupported property type for hashing: " + property.toString());
			}
		}
		
		private <T> void init(BiConsumer<DataSink, T> valueWriter) {
			this.valueWriter = (BiConsumer<DataSink, Object>) valueWriter;
		}
		
		public void write(DataSink sink, GenericEntity entity) {
			Object value = property.get(entity);
			
			if (value == null)
				return;
			
			sink.writeString(property.getName());
			valueWriter.accept(sink, value);
		}
	}
	
	private static class PropertyComparator<T extends GenericEntity> implements Comparator<T> {
		private final Property property;
		
		
		public PropertyComparator(Property property) {
			super();
			this.property = property;
		}


		@Override
		public int compare(T e1, T e2) {
			Comparable<Object> o1 = property.get(e1);
			Comparable<Object> o2 = property.get(e2);
			
			if (o1 == o2)
				return 0;
			
			if (o1 == null)
				return -1;
			
			if (o2 == null)
				return 1;
			
			return o1.compareTo(o2);
		}
	}

}