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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public class PooledStatefulYamlMarshaller extends AbstractStatefulYamlMarshaller {
	private final Map<GenericEntity, String> anchors = new IdentityHashMap<>();
	private final Queue<PooledEntity> poolQueue = new LinkedBlockingQueue<>();
	
	private static class PooledEntity {
		GenericEntity entity;
		String anchor;
		public PooledEntity(GenericEntity entity, String anchor) {
			super();
			this.entity = entity;
			this.anchor = anchor;
		}
	}

	public PooledStatefulYamlMarshaller(GmSerializationOptions options, Writer writer, Object rootValue) {
		super(options, writer, rootValue);
	}

	public void write() {
		try {
			
			writer.write("!pooled\n\n");
			writer.write("value:");
			write(options.getInferredRootType(), BaseType.INSTANCE, rootValue, true);
			
			if (poolQueue.isEmpty()) {
				writer.write('\n');
				return;
			}
			
			writer.write("\n\npool:\n");
			PooledEntity pooledEntity = null;
			while ((pooledEntity = poolQueue.poll()) != null) {
				writePoolEntity(pooledEntity);
			}
			
		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling: " + rootValue, e);
		}
	}
	
	private String anchorOf(GenericEntity entity) {
		return anchors.computeIfAbsent(entity, this::newAnchor);
	}
	
	private String newAnchor(GenericEntity e) {
		String anchor = String.valueOf(anchorSequence++);
		poolQueue.offer(new PooledEntity(e, anchor));
		return anchor;
	}
	
	@Override
	protected void writeEntity(GenericModelType inferredType, GenericEntity entity, boolean isComplexPropertyValue) throws IOException {
		String anchor = anchorOf(entity);
		
		int tokenCounter = isComplexPropertyValue ? 1 : 0;

		writeSpacer(tokenCounter++, writer);
		writer.write('*');
		writer.write(anchor);
		return;
	}

	private void writePoolEntity(PooledEntity pooledEntity) throws IOException {
		String anchor = pooledEntity.anchor;
		GenericEntity entity = pooledEntity.entity;

		writer.write("\n- &");
		writer.write(anchor);

		EntityType<?> entityType = entity.entityType();

		writer.write(" !");
		writer.write(entityType.getTypeSignature());

		boolean propertiesWritten = false;

		for (Property property : entityType.getProperties()) {

			
			if (property.isAbsent(entity)) {
				if (options.writeAbsenceInformation()) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					writer.write("\n ");
					indent.write(writer);

					if (absenceInformation == GMF.absenceInformation()) {
						writer.write(property.getName());
						writer.write("?: absent");
					} else {
						writer.write(property.getName());
						writer.write(":");
						write(AbsenceInformation.T, AbsenceInformation.T, absenceInformation, true);
					}
					propertiesWritten = true;
				}
			} else {
				Object value = property.get(entity);
				if (property.isEmptyValue(value) && !options.writeEmptyProperties())
					continue;

				writer.write("\n ");
				indent.write(writer);

				propertiesWritten = true;
				writer.write(property.getName());
				writer.write(':');

				GenericModelType type = property.getType();
				indent.pushIndent();
				write(type, type, value, true);
				indent.popIndent();
			}
		}

		if (!propertiesWritten) {
			writer.write(" {}\n");
		}
		else {
			writer.write('\n');
		}
	}
}