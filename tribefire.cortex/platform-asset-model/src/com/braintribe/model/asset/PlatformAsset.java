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
package com.braintribe.model.asset;

import java.util.List;

import com.braintribe.model.artifact.info.HasRepositoryOrigins;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.HasMetaData;

@SelectiveInformation("${name}")
public interface PlatformAsset extends HasMetaData, HasRepositoryOrigins {
	
	EntityType<PlatformAsset> T = EntityTypes.T(PlatformAsset.class);
	
	public static final String name = "name";
	public static final String version = "version";
	public static final String resolvedRevision = "resolvedRevision";
	public static final String groupId = "groupId";
	public static final String assets = "assets";
	public static final String nature = "nature";
	public static final String natureDefinedAsPart = "natureDefinedAsPart";
	public static final String hasUnsavedChanges = "hasUnsavedChanges";
	public static final String platformProvided = "platformProvided";
	public static final String effective = "effective";
	
	void setName(String name);
	String getName();
	
	@Initializer("'1.0'")
	String getVersion();
	void setVersion(String version);
	
	void setResolvedRevision(String resolvedRevision);
	String getResolvedRevision();
	
	void setGroupId( String groupId);
	String getGroupId();

	List<PlatformAssetDependency> getQualifiedDependencies();
	void setQualifiedDependencies(List<PlatformAssetDependency> qualifiedDependencies);
	
	void setNature( PlatformAssetNature nature);
	PlatformAssetNature getNature();
	
	boolean getNatureDefinedAsPart();
	void setNatureDefinedAsPart(boolean natureDefinedAsPart);
	
	boolean getHasUnsavedChanges();
	void setHasUnsavedChanges(boolean hasUnsavedChanges);
	
	boolean getIsContextualized();
	void setIsContextualized(boolean isContextualized);
	
	boolean getPlatformProvided();
	void setPlatformProvided(boolean platformProvided);
	
	// TODO COREPA-367: Rework this initializer as qualified deps are not effective!
	@Initializer("true")
	boolean getEffective();
	void setEffective(boolean effective);
	
	default String versionlessName() {
		return getGroupId() + ":" + getName();
	}

	default String qualifiedAssetName() {
		return versionlessName() + "#" + getVersion();
	}

	default String qualifiedRevisionedAssetName() {
		String rev = getResolvedRevision();
		return qualifiedAssetName() + (rev == null ? "" : "." + rev);
	}

	default String versionWithRevision() {
		String rev = getResolvedRevision();
		return getVersion() + (rev == null ? "" : "." + rev);
	}
}
