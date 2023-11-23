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
package com.braintribe.devrock.zarathud.test.extraction.comparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.commons.ClasspathResolvingUtil;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.devrock.zed.api.comparison.ZedComparison;
import com.braintribe.devrock.zed.core.comparison.BasicComparator;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * tests requires a 'environment sensitive surrounding 
 */
@Category(KnownIssue.class)
public abstract class AbstractComparisonTest {
	private static Logger log = Logger.getLogger(AbstractComparisonTest.class);
	private YamlMarshaller marshaller = new YamlMarshaller();

	/**
	 * resolve and extract an artifact 
	 * @param terminal
	 * @return
	 */
	protected Maybe<Artifact> extract(String terminal)  {
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminal);
		
		Maybe<CompiledArtifactIdentification> caiMaybe = ClasspathResolvingUtil.resolve(cdi, null);
		
		if ( caiMaybe.isUnsatisfied()) {
			Reason whyUnsatisfied = caiMaybe.whyUnsatisfied();
			String msg = "cannot resolve [" + terminal + "]: " + whyUnsatisfied.stringify();
			log.error( msg);
			return Maybe.empty( whyUnsatisfied);
		}
		CompiledArtifactIdentification cai = caiMaybe.get();
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
		
		ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
		rrc.setTerminal( cai.asString());
		//rrc.getEnvironmentOverrides().put("ENV_DEVROCK_REPOSITORY_CONFIGURATION", "f:/sde/env/devrock/artifacts/repository-configuration.yaml");
		rrc.setConsoleOutputVerbosity( com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity.verbose);
		
		ZedWireRunner zedWireRunner = wireContext.contract().resolvingRunner( rrc);
		
		Maybe<Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>>> maybe = zedWireRunner.run();
		if (maybe.hasValue()) {
			Artifact artifact = zedWireRunner.analyzedArtifact();
			return Maybe.complete( artifact); 
		}
		else  {
			return Maybe.empty( maybe.whyUnsatisfied());
		}
	}
	
	/**
	 * load a stored {@link Artifact} extraction  
	 * @param filename
	 * @return
	 */
	protected Maybe<Artifact> load( File file) {	
		if (!file.exists()) {
			return Maybe.empty( Reasons.build(NotFound.T).text("cannot find file: " + file.getAbsolutePath()).toReason());
		}	
		
		Artifact artifact;
		try (InputStream in = new FileInputStream( file)) {
			artifact = (Artifact) marshaller.unmarshall(in);
			return Maybe.complete(artifact);
		} catch (FileNotFoundException e) {
			return Maybe.empty( Reasons.build(NotFound.T).text("cannot find file: " + file.getAbsolutePath()).toReason());
		} catch (IOException e) {
			return Maybe.empty( Reasons.build(IoError.T).text("cannot unmarshall file: " + file.getAbsolutePath()).toReason());
		}		
	}
	
	/**
	 * saves a {@link Artifact} extraction
	 * @param artifact
	 * @param filename
	 * @return
	 */
	protected Maybe<Boolean> save( Artifact artifact, File file) {		
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.marshall(out, artifact);
		} catch (Exception e) {
			return Maybe.empty( Reasons.build(IoError.T).text("cannot marshall file: " + file.getAbsolutePath()).toReason());
		}
		return Maybe.complete(true);
	}
	
	protected Maybe<Boolean> save( List<FingerPrint> fps, File file) {
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.marshall(out, fps);
		} catch (Exception e) {
			return Maybe.empty( Reasons.build(IoError.T).text("cannot marshall file: " + file.getAbsolutePath()).toReason());
		}
		return Maybe.complete(true);
	}
	
	
	/**
	 * runs the actual comparison
	 * @param base
	 * @param other
	 * @return
	 */
	protected List<FingerPrint> compare( Artifact base, Artifact other) {
		ZedComparison comparator = new BasicComparator();
		comparator.compare(base, other);
		return comparator.getComparisonContext().getFingerPrints();
	}
	
	protected List<FingerPrint> compare( File base, File other, File output) {
		Maybe<Artifact> maybeA = load(base);
		Maybe<Artifact> maybeB = load(other);
		
		if (maybeA.isUnsatisfied() || maybeB.isUnsatisfied()) {
			Assert.fail("cannot load at least one of the files");
		}
		
		Artifact artifactA = maybeA.get();
		Artifact artifactB = maybeB.get();
		
		return compare(artifactA, artifactB, output);
	}
	
	protected String getName( FingerPrint fp) {
		String packageName = fp.getSlots().get( HasFingerPrintTokens.PACKAGE);
		String entityName = fp.getSlots().get( HasFingerPrintTokens.TYPE);
		
		String methodName = fp.getSlots().get( HasFingerPrintTokens.METHOD);
		if ( methodName != null) {
			return packageName + "." + entityName + "->" + methodName;
		}
		String fieldName = fp.getSlots().get( HasFingerPrintTokens.FIELD);
		if ( fieldName != null) {
			return packageName + "." + entityName + "." + fieldName;
		}
		
		return packageName + "." + entityName;
	}
	
	protected List<FingerPrint> compare( Artifact artifactA, Artifact artifactB, File output) {
		
		List<FingerPrint> fingerPrints = compare(artifactA, artifactB);
		
		if (fingerPrints != null && !fingerPrints.isEmpty()) {
			System.out.println( "found [" + fingerPrints.size() + "] issues");
			save(fingerPrints, output);
			
			for (FingerPrint fp : fingerPrints) {
				GenericEntity bge = fp.getEntitySource();
				//GenericEntity oge = fp.getEntityComparisonTarget();
				
				String baseName ="unknown";
				String prefix = bge.getClass().getName();
				
				baseName = getName( fp);
				
				String id = fp.getIssueData().stream().collect( Collectors.joining(","));
				if (id.length() > 0) {
					id = " -> " + id;
				}
				System.out.println( "entity (type:" + prefix + ") " + baseName  + " has issue : " + fp.getSlots().get( HasFingerPrintTokens.ISSUE) + id);
			}
			
			Assert.fail("differences were detected");
		}
		return fingerPrints;
	}

}
