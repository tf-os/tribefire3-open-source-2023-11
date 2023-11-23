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
package tribefire.extension.js.core.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

import tribefire.extension.js.core.impl.JsResolvingProcessor;
import tribefire.extension.js.core.test.utils.TestUtils;

/**
 * tests the JsResolver :
 * - support for local artifacts/projects (using parent of working folder as base) or only remote artifacts
 * - support to switch between the 'pretty' (unpacked standard zip) or 'min' (unpacked min:zip)
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class JsResolverTest_COREDR19 implements LauncherTrait{
	private File res = new File("res/COREDR-19");
	private File output = new File( res, "output");
	
	private File input = new File( res, "input");
	private File initial = new File( res, "initial");

	private File initialWorking = new File( initial, "working");
	private File initialJsRepository = new File( initial, "js-repository");
	
	private File working = new File( output, "working");
	private File repo = new File( output, "repo");
	private File jsRepository = new File( repo, "js-repository");
	private File settings = new File( res, "settings");
	
	private File settingsFile = new File( settings, "basic-settings.xml");
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private Launcher launcher = Launcher.build()
			.repolet()
				.name("archive")				
				.filesystem()
					.filesystem( input)
				.close()
			.close()			
		.done();	
	
	@Before 
	public void before() {		
		TestUtils.ensure(working);
		TestUtils.ensure(jsRepository);
		TestUtils.ensure( repo);
		
		TestUtils.copy(initialWorking, working);
		if (initialJsRepository.exists()) {
			TestUtils.copy(initialJsRepository, jsRepository);
		}
		runBefore(launcher);
	}
	
	@After
	public void after() {
		runAfter( launcher);
	}
	
	// local mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> jsRepositoryExpectations;	
	{
		jsRepositoryExpectations = new HashMap<>();
		jsRepositoryExpectations.put( "test.js.js-1.0", Pair.of( true, true));
		jsRepositoryExpectations.put( "test.js.jsa-1.0", Pair.of( true, true));
		jsRepositoryExpectations.put( "test.js.jsb-1.0", Pair.of( true, true));
		jsRepositoryExpectations.put( "test.js.jsc-1.0", Pair.of( true, true));		
	}
	// remote mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> remoteJsRepositoryExpectations;	
		{
			remoteJsRepositoryExpectations = new HashMap<>();
			remoteJsRepositoryExpectations.put( "test.js.js-1.0", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.jsa-1.0", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.jsb-1.0", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.jsc-1.0", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.projectA-1.0.1", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.projectB-1.0.1", Pair.of( true, true));
		}

	// local: terminal's lib entries set to use 'min' 
	Map<String,Pair<String,String>> localLibEntryExpectations;
	{
		localLibEntryExpectations = new HashMap<>();
		localLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		localLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));
		localLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
		localLibEntryExpectations.put( "test.js.terminal-1.0", Pair.of( "terminal", "src"));		
	}
	// local : terminal's lib entries set to use 'pretty'
	Map<String,Pair<String,String>> localPrettyLibEntryExpectations;
	{
		localPrettyLibEntryExpectations = new HashMap<>();
		localPrettyLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));
		localPrettyLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
		localPrettyLibEntryExpectations.put( "test.js.terminal-1.0", Pair.of( "terminal", "src"));		
	}
	// remote : terminal's lib entries set to use min 	
	Map<String,Pair<String,String>> remoteLibEntryExpectations;
	{
		remoteLibEntryExpectations = new HashMap<>();
		remoteLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "test.js.projectA-1.0.1", "min"));
		remoteLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "test.js.projectB-1.0.1", "min"));
		remoteLibEntryExpectations.put( "test.js.terminal-1.0", Pair.of( "terminal", "src"));		
	}
	
	

	/**
	 * @param names - a {@link List} of {@link String} to collate to a single string 
	 * @return
	 */
	private String collate( List<String> names) {
		return names.stream().collect(Collectors.joining(","));
	}

	/**
	 * @param jsRepository - the {@link File} pointing to the js-repository folder 
	 * @param expectations - a {@link Map} containing the expectations to verify the js-repository folder against
	 */
	private void validateJsRepositoryFolder( File jsRepository, Map<String, Pair<Boolean,Boolean>> expectations) {
		File [] files = jsRepository.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		for (File file : files) {
			String name = file.getName();
			Pair<Boolean,Boolean> found = expectations.get(name);
			if (found == null) {
				excess.add( name);
				continue;
			}
			matching.add( name);
			if (found.first) { // min
				File minFolder = new File( file, "min");
				Assert.assertTrue( "expected folder [" + minFolder.getAbsolutePath() + "], but it doesn't", minFolder.exists());
			}
			if (found.second) { // pretty
				File minFolder = new File( file, "pretty");
				Assert.assertTrue( "expected folder [" + minFolder.getAbsolutePath() + "], but it doesn't", minFolder.exists());
			}			
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		
		Assert.assertTrue("missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess [" + collate( excess) + "]", excess.size() == 0);
		
	}
	
	/**
	 * @param working - the {@link File} that points to the working folder
	 * @param jsRepository - the {@link File} that points to the js-repository
	 * @param terminal - the {@link String} that contains the name of the terminal
	 * @param expectations - a {@link Map} containing the expectations to verify the working folder with 
	 */
	private void validateWorkingFolder( File working, File jsRepository, String terminal, Map<String,Pair<String,String>> expectations) {
		File terminalFolder = new File( working, terminal);
		File libFolder = new File( terminalFolder, "lib");
		File [] files = libFolder.listFiles();
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();

		for (File file : files) {
			String name = file.getName();
			Pair<String,String> linkTarget = expectations.get( name); 
			if (linkTarget == null) {
				excess.add( name);
				continue;
			}			
			matching.add( name);			

			try {
				Path link = Files.readSymbolicLink( file.toPath());

				if (linkTarget.second.equalsIgnoreCase("src")) { 
					File found = new File( libFolder, link.toString());
					Path foundX = found.toPath();
					Path foundy = foundX.toRealPath();
					String foundPath = foundy.toString();
					File expected = new File( working,linkTarget.first + "/" + linkTarget.second);
					String expectedPath = expected.getAbsolutePath();
					Assert.assertTrue("expected [" + expectedPath + "] but found [" + foundPath +"]",  expectedPath.equals( foundPath));					
				}
				else {
					Path real = link.toRealPath();
					File found = real.toFile();
					String foundPath = found.getAbsolutePath();
					File expected = new File( jsRepository,linkTarget.first + "/" + linkTarget.second);
					String expectedPath = expected.getAbsolutePath();
					Assert.assertTrue("expected [" + expectedPath + "] but found [" + foundPath +"]",  expectedPath.equals( foundPath));
				}
				
			} catch (IOException e) {
				//e.printStackTrace();
				Assert.fail( name + ": cannot read symbolic link [" + file.getAbsolutePath() + "] as " + e.getMessage());
			}			
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		Assert.assertTrue("missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess [" + collate( excess) + "]", excess.size() == 0);
		
		
	}
	
	/**
	 * @param projectDirectory - the directory of the project to process
	 */
	private void test(File workingDirectory, String terminal, boolean preferMinOverPretty, boolean localProjectSupport, Map<String, Pair<Boolean,Boolean>> jsRepositoryExpectations, Map<String,Pair<String,String>> libEntryExpectations) {
		JsResolvingProcessor jrp = new JsResolvingProcessor();		
		jrp.setPreferMinOverPretty(preferMinOverPretty);
		jrp.setSupportLocalProjects(localProjectSupport);		
		File projectDirectory = new File( workingDirectory, terminal);
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv( "ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settingsFile.getAbsolutePath());
		ove.setEnv( "port", "" + launcher.getAssignedPort());
		ove.setEnv("m2_repo", repo.getAbsolutePath());
		ove.setEnv( JsResolvingProcessor.JS_LIBRARIES, jsRepository.getAbsolutePath());
		
		jrp.resolve( projectDirectory, ove);
		validateJsRepositoryFolder(jsRepository, jsRepositoryExpectations);
		validateWorkingFolder(working, jsRepository, terminal, libEntryExpectations);
	}

	/**
	 * tests 'local mode' with preferences set to 'min' 
	 */
	@Test
	public void testLocal() {
		test( working, "terminal", true, true, jsRepositoryExpectations, localLibEntryExpectations);		
	}
	
	/**
	 * tests 'local mode' with preferences set to 'pretty'
	 */
	@Test
	public void testPrettyLocal() {
		test( working, "terminal", false, true, jsRepositoryExpectations, localPrettyLibEntryExpectations);		
	}
	/**
	 * tests 'remote mode' with preferences set to 'min'
	 */
	@Test
	public void testRepoOnly() {
		test( working, "terminal", true, false, remoteJsRepositoryExpectations, remoteLibEntryExpectations);		
	}
	
	Map<Pair<String, String>, Pair<String, Boolean>> touchedExpectations = new HashMap<>();
	{
		touchedExpectations.put( Pair.of("test.js:jsc#1.0", "test.js.jsc-1.0"), Pair.of( "jsc-1.0.js.zip", true));
		touchedExpectations.put( Pair.of("test.js:jsa#1.0", "test.js.jsa-1.0"), Pair.of( "jsa-1.0.js.zip", false));
	}
		
	/**
	 * tests 'remote mode' with preferences set to 'min'
	 */
	@Test
	public void testRepoInstalled() {
		// initial run 
		test( working, "terminal", true, false, remoteJsRepositoryExpectations, remoteLibEntryExpectations);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// store date from artifact
		Map<String, Date> touchDates = new HashMap<>();
		for (Map.Entry<Pair<String,String>,Pair<String, Boolean>> entry : touchedExpectations.entrySet()) {
			
			Pair<String, String> artifactIdentificationPair = entry.getKey();
			File folder = new File( jsRepository, artifactIdentificationPair.second + "/pretty");		
			Pair<String, Boolean> touchedDataPair = entry.getValue();
			
			String touchFileName = touchedDataPair.first.endsWith( "min.js.zip") ? "min" : "pretty";
			File file = new File( folder.getParentFile(), touchFileName + ".touched");
			
			try (InputStream in = new FileInputStream( file)) {
				Date date = (Date) marshaller.unmarshall(in);
				touchDates.put(touchedDataPair.first, date);
				if (touchedDataPair.second) {
					// copy file 
					Solution s = NameParser.parseCondensedSolutionName( artifactIdentificationPair.first);					
					File sourceFile = UniversalPath.from(input).push( s.getGroupId(), ".").push( s.getArtifactId()).push(VersionProcessor.toString( s.getVersion())).push( touchedDataPair.first).toFile();
					File targetFile = UniversalPath.from(repo).push( s.getGroupId(), ".").push( s.getArtifactId()).push(VersionProcessor.toString( s.getVersion())).push( touchedDataPair.first).toFile();
					targetFile.delete();
					Files.copy( sourceFile.toPath(), targetFile.toPath());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("cannot read / touch [" +  file.getAbsolutePath() + "]");
			}
		}				
		// second run 
		test( working, "terminal", true, false, remoteJsRepositoryExpectations, remoteLibEntryExpectations);
		
		// compare date from artifact
		for (Map.Entry<Pair<String,String>,Pair<String, Boolean>> entry : touchedExpectations.entrySet()) {
			
			Pair<String, String> artifactIdentificationPair = entry.getKey();
			File folder = new File( jsRepository, artifactIdentificationPair.second + "/pretty");		
			Pair<String, Boolean> touchedDataPair = entry.getValue();
			String touchFileName = touchedDataPair.first.endsWith( "min.zip") ? "min" : "pretty";
			File file = new File( folder.getParentFile(), touchFileName + ".touched");
			try (InputStream in = new FileInputStream( file)) {
				Date date = (Date) marshaller.unmarshall(in);
				Date storedDate = touchDates.get( touchedDataPair.first);
				
				if (touchedDataPair.second) {
					Assert.assertTrue("expected [" + file.getAbsolutePath() + "] to have changed", date.compareTo(storedDate) != 0);												
				}
				else {
					Assert.assertTrue("expected [" + file.getAbsolutePath() + "] not to have changed", date.compareTo(storedDate) == 0);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("cannot read / touch [" +  file.getAbsolutePath() + "]");
			}
		}				
		
	}
}
