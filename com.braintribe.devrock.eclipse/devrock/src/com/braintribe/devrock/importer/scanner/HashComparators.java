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
package com.braintribe.devrock.importer.scanner;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.version.Version;

public interface HashComparators {
	/**
	 * a {@link EntityHashingComparator} for {@link EnhancedCompiledArtifactIdentification} that <b>INCLUDES</b> the archetype and the origin 
	 */
	static final HashingComparator<EnhancedCompiledArtifactIdentification> enhancedCompiledArtifactIdentification = EntityHashingComparator
			.build( EnhancedCompiledArtifactIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.major)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.minor)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.revision)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.qualifier)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.buildNumber)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.nonConform)
			.addField( EnhancedCompiledArtifactIdentification.archetype)
			.addField( EnhancedCompiledArtifactIdentification.origin)
			.done();
}
