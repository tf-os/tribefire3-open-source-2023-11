// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.legacy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.braintribe.exception.Exceptions;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.meta.Artifact;
import com.braintribe.model.version.Version;

/**
 * @deprecated Review if needed, see {@link MavenInstallAssetTransfer}
 */
@Deprecated
public class MavenMetaDataTools {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss").withZone(ZoneId.systemDefault());

	public static String formatMavenMetaDataDate(Date date) {
		return formatter.format(ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault()));
	}
	
	public static Date parseMavenMetaDataDate(String date) {
		return Date.from(ZonedDateTime.parse(date, formatter).toInstant());
	}
	
	public static void addSolution(MavenMetaData mavenMetaData, Artifact artifact) {
		addSolution(mavenMetaData, artifact.getVersion());
	}
	
	public static void addSolution(MavenMetaData mavenMetaData, String version) {
		addSolution(mavenMetaData, Version.parse(version));
	}
	
	public static void addSolution(MavenMetaData mavenMetaData, Version version) {
		Versioning versioning = mavenMetaData.getVersioning();
		if (versioning == null) {
			versioning = Versioning.T.create();
			mavenMetaData.setVersioning(versioning);			
		}
		
		List<Version> versions = versioning.getVersions();
		Set<Version> sortedVersions = new TreeSet<>(versions);
		
		if (!sortedVersions.add(version))
			return;
		
		versions.clear();
		versions.addAll(sortedVersions);		
		
		Date lastUpdated = new Date();
		String lastUpdatedStr = formatMavenMetaDataDate(lastUpdated);
		versioning.setLastUpdated(lastUpdatedStr);
		versioning.setLatest(version);
	}
	

	public static void updateMetaData(PlatformAsset asset, MavenMetaData mavenMetadata, File artifactMetaDataFile) {
		try {
			String version = asset.getVersion() + '.' + asset.getResolvedRevision();
			
			addSolution(mavenMetadata, version);
			
			writeMetaData(mavenMetadata, artifactMetaDataFile);
			
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while updating maven meta data for " + asset.getGroupId() + ":" + asset.getName() + "#"
					+ asset.getVersion() + '.' + asset.getResolvedRevision());
		}
		
	}
	
	public static MavenMetaData acquireMetadata(PlatformAsset asset, File artifactMetaDataFile) {
		MavenMetaData mavenMetaData = null;
		if (artifactMetaDataFile.exists()) {
			try {
				return readMavenMetaData(artifactMetaDataFile);
			} catch (Exception e) {
				throw new RuntimeException("error while loading metadata file: " + artifactMetaDataFile, e);
			}
		}
		else {
			try {
				mavenMetaData = MavenMetaData.T.create();
				mavenMetaData.setArtifactId(asset.getName());
				mavenMetaData.setGroupId(asset.getGroupId());
				mavenMetaData.setVersioning(Versioning.T.create());
				
			} catch (Exception e) {
				throw Exceptions.unchecked(e,"error while creating metadata");
			}
		}
		
		return mavenMetaData;
	}
	
	public static MavenMetaData readMavenMetaData(File file) throws FileNotFoundException, IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			return (MavenMetaData) DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);
		}
	}
	
	public static void writeVersionMetadata(PlatformAsset asset, File versionMetaDataFile) {
		try {
			Versioning versioning = Versioning.T.create();
			versioning.setLastUpdated(formatMavenMetaDataDate(new Date()));
			
			MavenMetaData mavenMetaData = MavenMetaData.T.create();
			mavenMetaData.setArtifactId(asset.getName());
			mavenMetaData.setGroupId(asset.getGroupId());
			mavenMetaData.setVersion(Version.parse(asset.getVersion()+ '.' + asset.getResolvedRevision()));
			mavenMetaData.setVersioning(versioning);

			// TODO what now?

		} catch (Exception e) {
			throw new RuntimeException("error while creating metadata", e);
		}
	}
	
	public static void writeMetaData(MavenMetaData mavenMetaData, File file) throws FileNotFoundException, IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, mavenMetaData);
		}
	}
	
}
