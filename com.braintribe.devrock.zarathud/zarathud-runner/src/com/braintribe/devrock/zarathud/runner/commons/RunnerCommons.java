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
package com.braintribe.devrock.zarathud.runner.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.JarProcessingRunnerContext;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerprintOverrideContainer;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

public class RunnerCommons {
	/**
	 * @param condensedName
	 * @return
	 */
	public static Artifact toArtifact( String condensedName) {
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
	
	/**
	 * @param solution
	 * @return
	 */
	public static Artifact toArtifact( AnalysisArtifact solution) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( solution.getVersion());		
		return artifact;
	}
	
	public static CoreContext coreContextFromJarProcessingContext(JarProcessingRunnerContext in) {
		CoreContext out = new CoreContext();
		out.setClasspath( in.getClasspath());
		out.setDependencies( in.getDependencies());
		out.setTerminalArtifact( RunnerCommons.toArtifact( in.getTerminal()));
		out.setRespectBraintribeSpecifica( in.getRespectBraintribeSpecifica());
		com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity consoleOutputVerbosity = in.getConsoleOutputVerbosity();
		if (consoleOutputVerbosity != null) {
			out.setConsoleOutputVerbosity(ConsoleOutputVerbosity.valueOf( consoleOutputVerbosity.toString()));
		}		
		
		return out;
	}
	
	public static CoreContext coreContextFromClassesProcessingContext(ClassesProcessingRunnerContext in) {
		CoreContext out = new CoreContext();
		out.setClasspath( in.getClasspath());
		out.setDependencies( in.getDependencies());
		out.setClassesDirectories( in.getTerminalClassesDirectoryNames());
		out.setTerminalArtifact( RunnerCommons.toArtifact( in.getTerminal()));
		out.setRespectBraintribeSpecifica( in.getRespectBraintribeSpecifica());
		com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity consoleOutputVerbosity = in.getConsoleOutputVerbosity();
		if (consoleOutputVerbosity != null) {
			out.setConsoleOutputVerbosity(ConsoleOutputVerbosity.valueOf( consoleOutputVerbosity.toString()));
		}		
		
		return out;
	}
	
	/**
	 * dumps fingerprints 
	 * @param output - {@link File} pointing to the directory
	 * @param terminalName - the name of the terminal 
	 * @param fingerPrints - the fingerprint data to store 
	 * @return - the {@link File} written
	 */
	public static File writeFingerPrints( Marshaller marshaller, File output, String terminalName, Map<FingerPrint,ForensicsRating> fingerPrints) {
		FingerprintOverrideContainer fpovrc = new FingerprintOverrideContainer();
		fpovrc.setFingerprintOverrideRatings(fingerPrints);		
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + ".fpr.txt";
		File target = new File( output, name);
		try (OutputStream out = new FileOutputStream( target)) {
			marshaller.marshall(out, fpovrc);
			return target;
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot write ", e);
		}
	}
	
	/**
	 * dump data 
	 * @param output - {@link File} pointing to the directory 
	 * @param terminalName - the name of the terminal
	 * @param code - the code (actually a prefix to the suffix of the file)
	 * @param payload - the payload as {@link GenericEntity}
	 * @return - the {@link File} written 
	 */
	public static File writeAssemblies( Marshaller marshaller, File output, String terminalName, String code, GenericEntity payload) {
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + "." + code + ".yaml";
		File target = new File( output, name);
		try (OutputStream out = new FileOutputStream( target)) {
			marshaller.marshall(out, payload);
			return target;
		}
		catch (Exception e) {
			throw new IllegalStateException("boink", e);
		}
		
	}
	
	
}
