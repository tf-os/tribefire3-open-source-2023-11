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
package com.braintribe.devrock.zarathud.test.extraction.resolving;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.devrock.zarathud.test.utils.TestUtils;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerPrintDumper;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;


@Category(KnownIssue.class)
public class GroupZedRunner {
	private static Logger log = Logger.getLogger(GroupZedRunner.class);
	private File repo = new File("f:/repository");
	private File contents = new File( "res");
	private File grpDump = new File( contents, "group-dump");
	private WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
	private DeclaredMavenMetaDataMarshaller marshaller = new DeclaredMavenMetaDataMarshaller();
	
	private List<String> processGroup( String group) {
		String grp = group.replace( '.', '/');
		File grpDir = new File( repo, grp); 
		File [] dirs = grpDir.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		List<String> names = new ArrayList<>();
		for (File artifactDir : dirs) {
			String version = processArtifactDirectory( artifactDir);
			if (version == null)
				continue;
			String name = group + ":" + artifactDir.getName() + "#" + version;
			names.add( name);
		}
		
		return names;
				
	}
	
	private String processArtifactDirectory(File artifactDir) {
		File [] mds = artifactDir.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File dir) {
				String name = dir.getName();
				return name.startsWith( "maven-metadata-") && name.endsWith(".xml");
			}
		});
		if (mds.length == 0)
			return null;
		List<Version> versions = new ArrayList<>();
		for (File fmd : mds) {
			try (InputStream in = new FileInputStream(fmd)){
				MavenMetaData metaData = (MavenMetaData) marshaller.unmarshall(in);
				Versioning versioning = metaData.getVersioning();
				if (versioning != null) {
					versions.addAll( versioning.getVersions());					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		versions.sort( new Comparator<Version>() {

			@Override
			public int compare(Version o1, Version o2) {			
				return o1.compareTo(o2);
			}			
		});
		if (versions.size() == 0) {
			return null;
		}
		
		Version hv = versions.get( versions.size() - 1);
									
		return hv.asString();
	}

	@SuppressWarnings("unused")
	private ForensicsRating runArtifacts( List<String> condensedNames) {
		ForensicsRating overallRating = ForensicsRating.OK;
		for (String name : condensedNames) {
			Artifact artifact = Artifact.parse(name);		
			ForensicsRating rating = runArtifact( artifact).get().first;
			if (rating.ordinal() > overallRating.ordinal()) {
				overallRating = rating;
			}
		}
		return overallRating;
		 
	}
	
	private Maybe<Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>>> runArtifact( Artifact artifact) {
		ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
		String terminalName = artifact.toVersionedStringRepresentation();
		rrc.setTerminal( terminalName);
		rrc.setConsoleOutputVerbosity( ConsoleOutputVerbosity.taciturn);
		System.out.println("processing terminal [" + terminalName + "]");
		ZedWireRunner zedWireRunner = wireContext.contract().resolvingRunner( rrc);
		
		return zedWireRunner.run();
	}
	
	
	private void run(String grp, String ... badBoys) {
		
		File targetDir = new File( grpDump, grp);
		TestUtils.ensure( targetDir);
		
		List<String> blacklist = badBoys != null ? Arrays.asList(badBoys) : null; 
		List<String> list = processGroup(grp);
		System.out.println(list.stream().collect( Collectors.joining(",")));
		BlackListFilter blackListFilter = new BlackListFilter(blacklist);
		
		Map<String, ForensicsRating> result = new HashMap<>();
		for (String name : list) {
			if (blackListFilter.test(name)) {
				result.put( name, null);
				continue;
			}
			ForensicsRating rating;
			try {
				Artifact artifact = Artifact.parse(name);
				Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>> pair = runArtifact( artifact).get();
				rating = pair.first;
				if (rating != null) {
					result.put( name, rating);
					if (rating.ordinal() > ForensicsRating.WARN.ordinal()) {				
						log.error("Zed reports a rating of [" + rating + "] for [" + name + "]");
					}
					// dump 					
					FingerPrintDumper.dump( targetDir, artifact, pair.second);
				}
				else {
					log.warn("Zed reports that [" + name + "] has no jar to analyse");
				}
			} catch (Exception e) {
				log.error("Zed crashes with [" + e.getMessage() + "] while processing [" + name + "]");
				result.put( name, ForensicsRating.FATAL);
			}
		}
		

		StringBuilder sb = new StringBuilder();
		for (Entry<String,ForensicsRating> entry : result.entrySet()) {
			if (sb.length() > 0)
				sb.append( "\n");
			ForensicsRating rating = entry.getValue();
			String name = entry.getKey();
			if (rating != null) {
				sb.append( rating.toString() + ":\t\t" + name);
				if (rating == ForensicsRating.ERROR) {
					// find the offending error in the actual finger print file 
					sb.append("\n" + extractErrorsFrom( targetDir, name));
				}
			} else {
				sb.append( "N/A:\t\t" + name);
			}
		}
		File target = new File( grpDump, grp + ".dmp.txt");
		System.out.println( sb.toString());
		try {
			IOTools.spit( target, sb.toString(), "UTF-8", false);
		} catch (IOException e) {
			log.error("cannot dump result to [" + target.getAbsolutePath() + "]");
		}		
	}
	
	
	private String extractErrorsFrom(File targetDir, String name) {
		String offset ="\t\t\t";
		StringBuilder sb = new StringBuilder();
		Artifact artifact = Artifact.parse(name);
		Map<FingerPrint, ForensicsRating> ratedFingerprints = FingerPrintDumper.load(targetDir, artifact);
		for (Entry<FingerPrint, ForensicsRating> entry : ratedFingerprints.entrySet()) {
			if (entry.getValue() == ForensicsRating.ERROR) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append( offset + FingerPrintExpert.toString(entry.getKey()));
			}
		}
		return sb.toString();
	}


	private class BlackListFilter implements Predicate<String> {
		private List<String> blackListed;
		
		public BlackListFilter(List<String> l) {
			blackListed = l;
		}
		
		@Override
		public boolean test(String t) {
			if (blackListed == null)
				return false;
			return blackListed.contains(t);
		}
		
	}

	
	
	@Test
	public void test__com_braintribe_devrock() {
		run( "com.braintribe.devrock");
	}
	
	@Test
	public void test__com_braintribe_common() {
		run( "com.braintribe.common");
	}
	
	@Test
	public void test__com_braintribe_devrock_zarathud() {
		run("com.braintribe.devrock.zarathud");	
	}
	
	
	@Test
	public void test__com_braintribe_schemedxml() {
		run("com.braintribe.gm.schemedxml");	
	}
	
	@Test
	public void test__tribefire_extension_schemedxml() {
		run("tribefire.extension.schemed-xml");	
	}
	
	@Test
	public void test__tribefire_extension_setup() {
		run("tribefire.extension.setup");	
	}
	
	@Test
	public void test__tribefire_extension_artifact() {
		run("tribefire.extension.artifact");	
	}
	
	@Test
	public void test__com_braintribe_gm() {
		run("com.braintribe.gm");
		
	}
	
	@Test
	public void test__adx_phoenix() {
		run("tribefire.adx.phoenix");	
	}
	
	@Test
	public void test__tribefire_cortex() {
		run("tribefire.cortex");	
	}

}
