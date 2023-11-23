// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.cmd.assets.impl.modules;

import static com.braintribe.utils.lcd.CollectionTools.getSortedList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import com.braintribe.build.cmd.assets.impl.modules.model.ComponentSetup;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.hash.md5.MD5HashGenerator;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.packaging.Artifact;
import com.braintribe.model.packaging.Dependency;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;


/**
 * creates a list of special packaging artifacts used to show what the terminal artifact or resolved setup consists of
 * 
 * @author pit
 *
 */
class ArtifactListProducer {
	
	public static void writePackagingToFile(ComponentSetup platformSetup, File outputFile) {
		writePackagingToFile(platformSetup.descriptor.assetSolution, platformSetup.classpath, outputFile);
	}
	
	private static void writePackagingToFile(AnalysisArtifact platformSetup, List<AnalysisArtifact> solutions, File outputFile) {
		Packaging packaging = createPackaging(platformSetup, solutions);
		writeToFile(packaging, outputFile);
	}

	private static Packaging createPackaging(AnalysisArtifact projectArtifact, List<AnalysisArtifact> classPath) {
		List<AnalysisArtifact> sortedSolutions = sortByArtifactId(classPath);
	
		StringJoiner hashBuilder = new StringJoiner(",");
		List<Dependency> packagedArtifacts = newList();

		for (AnalysisArtifact solution : sortedSolutions) {
			packagedArtifacts.add(transferIdentification(Dependency.T.create(), solution));
			hashBuilder.add(solution.getVersion());						
		}

		Packaging packaging = Packaging.T.create();
		packaging.setTerminalArtifact(transferIdentification(Artifact.T.create(), projectArtifact));
		packaging.setTimestamp( new Date());
		packaging.setDependencies(packagedArtifacts);
		packaging.setMD5(toMd5Unchecked(hashBuilder.toString()));
		// set version (i.e. release/snapshot name)
		packaging.setVersion(getReleaseVersionString(packaging.getTimestamp()));
		// get revision info 
		packaging.setRevision("-1");

		return packaging;
	}

	private static List<AnalysisArtifact> sortByArtifactId(List<AnalysisArtifact> classPath) {
		return getSortedList(classPath, Comparator.comparing(AnalysisArtifact::getArtifactId));
	}

	private static String toMd5Unchecked(String hash) {
		try {
			return MD5HashGenerator.MD5(hash, "UTF-8");
		} catch (CryptoServiceException e1) {
			throw new IllegalStateException("cannot generated MD5 hash from solutions", e1);
		}
	}

	private static void writeToFile(Packaging packaging, File outputFile) {
		FileTools.write(outputFile).usingOutputStream(out -> staxMarshall(packaging, out));
	}

	private static void staxMarshall(Packaging packaging, OutputStream out) {
		new StaxMarshaller().marshall(out, packaging,
				GmSerializationOptions.deriveDefaults().setOutputPrettiness(OutputPrettiness.high).build());
	}
	
	/**
	 * Creates a version string to be used as a name/identification for the build/release (see
	 * {@link Packaging#getVersion()}. This method by default returns a string consisting of a prefix, a timestamp and
	 * the user name: <code>localsnapshot+[timestamp]_[user]</code>. This clearly marks the build/release as local
	 * (snapshot) build. Such snapshots should not be used in any customer/partner environments! Alternatively the CI
	 * may set environment variable "BTANTTASKS__PACKAGING_VERSION" to force a certain version.
	 */
	private static String getReleaseVersionString(Date timestamp) {
		// TODO WTF? BTANTTASKS?
		String version = System.getenv("BTANTTASKS__PACKAGING_VERSION");

		if (version == null) {
			String timestampString = DateTools.encode(timestamp, DateTools.TERSE_DATETIME_FORMAT);
			String user = System.getProperty("user.name");
			version = "localsnapshot+" + timestampString + "_" + user;
		}

		return version;
	}
	
	private static <A extends com.braintribe.model.packaging.Artifact> A transferIdentification(A artifact, AnalysisArtifact identification) {
		artifact.setGroupId(identification.getGroupId());
		artifact.setArtifactId(identification.getArtifactId());
		artifact.setVersion(identification.getVersion());
		
		return artifact;
	}
	
}
