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

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traverse.ConfigurableEntityVisiting;

public class StatefulYamlMarshaller extends AbstractStatefulYamlMarshaller {
	private static class EntityAnchoring {
		public int refCount;
		public String anchor;
		public boolean visited;
	}

	private final Map<GenericEntity, StatefulYamlMarshaller.EntityAnchoring> anchors = new IdentityHashMap<>();

	public StatefulYamlMarshaller(GmSerializationOptions options, Writer writer, Object rootValue) {
		super(options, writer, rootValue);
	}

	public void write() {
		new ConfigurableEntityVisiting(this::indexEntity).visit(rootValue);
		try {
			write(options.getInferredRootType(), BaseType.INSTANCE, rootValue);
			writer.write('\n');
		} catch (IOException e) {
			throw new UncheckedIOException("Error while marshalling: " + rootValue, e);
		} 
	}

	private boolean indexEntity(EntityType<?> entityType, GenericEntity entity) {
		StatefulYamlMarshaller.EntityAnchoring anchoring = anchors.computeIfAbsent(entity, e -> new EntityAnchoring());
		return ++anchoring.refCount == 1;
	}

	@Override
	protected void writeEntity(GenericModelType inferredType, GenericEntity entity, boolean isComplexPropertyValue) throws IOException {
		StatefulYamlMarshaller.EntityAnchoring anchoring = anchors.get(entity);

		int tokenCounter = isComplexPropertyValue ? 1 : 0;

		if (anchoring.visited && identityManagementMode != IdentityManagementMode.off) {
			writeSpacer(tokenCounter++, writer);
			writer.write('*');
			writer.write(anchoring.anchor);

			return;
		}

		if (entityVisitor != null && !anchoring.visited)
			entityVisitor.accept(entity);

		anchoring.visited = true;
		boolean entityIntroductionWritten = false;
		
		if (anchoring.refCount > 1 && identityManagementMode != IdentityManagementMode.off) {

			String anchor = String.valueOf(anchorSequence++);
			anchoring.anchor = anchor;
			writeSpacer(tokenCounter++, writer);
			writer.write('&');
			writer.write(anchor);
			entityIntroductionWritten = true;
		}

		EntityType<?> entityType = entity.entityType();

		if (typeExplicitness != TypeExplicitness.never && (typeExplicitness != TypeExplicitness.polymorphic || entity.entityType() != inferredType)) {
			writeSpacer(tokenCounter++, writer);
			writer.write('!');
			writer.write(entityType.getTypeSignature());
			entityIntroductionWritten = true;
		}

		boolean propertiesWritten = false;

		for (Property property : entityType.getProperties()) {

			boolean startWithNewline = isComplexPropertyValue || propertiesWritten || entityIntroductionWritten;

			if (property.isAbsent(entity)) {
				if (options.writeAbsenceInformation()) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					if (startWithNewline) {
						writer.write('\n');
						indent.write(writer);
					}

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

				if (startWithNewline) {
					writer.write('\n');
					indent.write(writer);
				}
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
			writeSpacer(tokenCounter++, writer);
			writer.write("{}");
		}
	}
}