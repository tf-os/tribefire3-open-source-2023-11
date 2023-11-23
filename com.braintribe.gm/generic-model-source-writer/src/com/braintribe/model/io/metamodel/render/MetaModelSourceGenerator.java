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
package com.braintribe.model.io.metamodel.render;

import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.io.metamodel.MetaModelSourceDescriptor;
import com.braintribe.model.io.metamodel.render.serializer.SourceSerializer;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;

/**
 * Class that manages rendering and serializing of source files for given GmMetaModel. See method
 * {@link #generateSources}.
 */
public class MetaModelSourceGenerator {

	private final SourceWriterContext context;
	private final MetaModelRenderer metaModelRenderer;
	private SourceSerializer sourceSerializer;

	private static final Logger log = Logger.getLogger(MetaModelSourceGenerator.class);

	public MetaModelSourceGenerator(SourceWriterContext context) {
		this.context = context;
		this.metaModelRenderer = new MetaModelRenderer(context);
	}

	/**
	 * Generates sources for given MetaModel and stores them in provided outputDirectory. In case it tries to create a
	 * file with name, that is already used by an existing file, this existing file is copied into a backup directory
	 * inside the outputDirectory.
	 */
	public Map<String, String> generateSources(SourceSerializer sourceSerializer) {
		this.sourceSerializer = sourceSerializer;

		generateEntityTypes();
		generateEnums();

		return sourceSerializer.getSourceMap();
	}
	
	private void generateEntityTypes() {
		context.modelOracle.getTypes().onlyDeclared().onlyEntities().<GmEntityType> asGmTypes().forEach(gmEntityType -> {
			log.info("Processing type [" + gmEntityType.getTypeSignature() + "]");
			
			try {
				MetaModelSourceDescriptor sourceDescriptor = metaModelRenderer.renderEntityType(gmEntityType);
				store(sourceDescriptor);

			} catch (Exception e) {
				log.error("Failed to render entity type " + gmEntityType.getTypeSignature(), e);
			}
		});
	}

	private void generateEnums() {
		context.modelOracle.getTypes().onlyDeclared().onlyEnums().<GmEnumType> asGmTypes().forEach(gmEnumType -> {
			try {
				MetaModelSourceDescriptor sourceDescriptor = metaModelRenderer.renderEnumType(gmEnumType);
				store(sourceDescriptor);

			} catch (Exception e) {
				log.error("Failed to render enum type " + gmEnumType.getTypeSignature(), e);
			}
		});
	}

	private void store(MetaModelSourceDescriptor sourceDescriptor) {
		if (sourceDescriptor != null) {
			sourceSerializer.writeSourceFile(sourceDescriptor);
		}
	}

}
