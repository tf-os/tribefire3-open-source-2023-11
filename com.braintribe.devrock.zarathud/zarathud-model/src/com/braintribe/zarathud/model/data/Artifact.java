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
package com.braintribe.zarathud.model.data;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * zarathud's representation of an artifact
 * @author pit
 *
 */
public interface Artifact extends GenericEntity, Comparable<Artifact>{
	
	final EntityType<Artifact> T = EntityTypes.T(Artifact.class);
	
	String groupId = "groupId";
	String artifactId = "artifactId";
	String version = "version";
	String entries = "entries";
	String gwtModule = "gwtModule";
	String declaredDependencies = "declaredDependencies";
	String actualDependencies = "actualDependencies";
	String isIncomplete = "isIncomplete";

	/**
	 * @return - the group id
	 */
	String getGroupId();
	void setGroupId(String id);
	
	/**
	 * @return - the artifact id
	 */
	String getArtifactId();
	void setArtifactId(String id);
	
	/**
	 * @return - the version
	 */
	String getVersion();
	void setVersion(String version);
	
	/**
	 * @return - true if the artifact is incomplete, i.e. it was not sufficiently enough instrumented to identify it fully. 
	 * In most cases (the only one now) this is due to a forward-declaration which has no corresponding dependency.  
	 */
	boolean getIsIncomplete();
	void setIsIncomplete(boolean value);

	
	/**
	 * @return - everything found within this Artifact as a {@link Set} of {@link ZedEntity}
	 */
	Set<ZedEntity> getEntries();
	void setEntries( Set<ZedEntity> entries);
	
	/**
	 * @return
	 */
	String getGwtModule();
	void setGwtModule( String name);
	
	/**
	 * @return - a list of {@link Artifact} as they were declared in the pom
	 */
	List<Artifact> getDeclaredDependencies();
	void setDeclaredDependencies( List<Artifact> artifacts);
	
	/**
	 * @return - a list of {@link Artifact} as they were determined by Z
	 */
	List<Artifact> getActualDependencies();
	void setActualDependencies( List<Artifact> artifacts);
	
	
	@Override
	default int compareTo( Artifact o2) {	
		if (o2 == null)
			return 1;
		
		
		String g1 = getGroupId();
		String g2 = o2.getGroupId();	
		if (g1 == null) {
			if (g2 != null) {
				return -1;
			}
			// both null, this is fine
		}
		else {
			if (g2 == null) {
				return 1;
			}
			int r = g1.compareTo(g2);
			if (r != 0)
				return r;
		}
		 
		String a1 = getArtifactId();
		String a2 = o2.getArtifactId();
		if (a1 == null) {
			if (a2 != null) {
				return -1;
			}
			// both null, fine 
		}
		else {
			if (a2 == null) {
				return 1;
			}
			int r = a1.compareTo(a2);
			if (r != 0)
				return r;
		}
		String v1 = getVersion();
		String v2 = o2.getVersion();
		if (v1 == null) {
			if (v2 != null) {
				return -1;
			}
			else {
				return 0;
			}			
		}
		if (v2 == null)
			return 1;
		
		int r = v1.compareTo(v2);
		if (r != 0) {
			return r;
		}
		
		return 0;
	}	
	
	/**
	 * @return - {@code <groupId>:<artifactId>#<version>} string concatenation
	 */
	default String toVersionedStringRepresentation() {
		if (getGroupId() != null && getVersion() != null) {
			return getGroupId() + ":" + getArtifactId() + "#" + getVersion();
		}
		return getArtifactId();
	}
	
	default String asString() {
		return toVersionedStringRepresentation();
	}
	/**
	 * @return - see {@link Artifact#toStringRepresentation()}
	 */
	default String toUnversionedStringRepresentation() {
		return toStringRepresentation();		
	}
	/**
	 * @return - a {@code <groupId>:<artifactId>} string concatentation
	 */
	default String toStringRepresentation() {
		if (getGroupId() != null) {
			return getGroupId() + ":" + getArtifactId();
		}
		return getArtifactId();
	}
	
	
	/**
	 * @return - a {@code <artifactId>-<version>.jar} string representation
	 */
	default String toJarRepresentation() {
		if (getArtifactId() != null && getVersion() != null) {
			return getArtifactId() + "-" + getVersion() + ".jar";
		}
		throw new IllegalStateException("artifactid and version both must be set");
	}
	
	/**
	 * @param condensedName
	 * @return
	 */
	 static Artifact parse( String condensedName) {
		String [] values = condensedName.split( "[:#]");
		if (values.length < 3) {
			throw new IllegalArgumentException("passed value [" + condensedName + "] is not a valid solution name");
		}		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( values[0]);
		artifact.setArtifactId( values[1]);
		artifact.setVersion( values[2]);
		
		return artifact;
	}
	 
}
