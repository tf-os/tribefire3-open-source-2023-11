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
package tribefire.extension.drools.integration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.drools.core.io.impl.ByteArrayResource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.paths.UniversalPath;

import tribefire.extension.drools.integration.api.DroolsContainer;
import tribefire.extension.drools.integration.api.DroolsContainerFactory;

public class BasicDroolsContainerFactory implements DroolsContainerFactory {
	private static final Logger logger = Logger.getLogger(BasicDroolsContainerFactory.class);
	
	private static final UniversalPath RESOURCES_PATH = UniversalPath.empty().push("src").push("main").push("resources");
	private static final String DYNAMIC_BASE_PACKAGE = "tribefire.extension.drools.rule";
	private KieServices kieServices = KieServices.get();
	
	static {
		// Installing a property handler to make GenericModel's interface based Java beans accessible to drools/mvel2
		GmMvel2PropertyHandler.install();
	}
	
	@Override
	public DroolsContainer open(InputStreamProvider ruleStreamProvider, String identifier) {
		return new BasicDroolsContainer(ruleStreamProvider, identifier);
	}
	
	private class BasicDroolsContainer implements DroolsContainer {
		private ReleaseId releaseId;
		private KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		private InputStreamProvider ruleStreamProvider;
		private LazyInitialized<KieContainer> kieContainer = new LazyInitialized<>(this::openKieContainer, KieContainer::dispose);
		private String identifier;
		
		public BasicDroolsContainer(InputStreamProvider ruleStreamProvider, String identifier) {
			this.ruleStreamProvider = ruleStreamProvider;
			this.identifier = identifier;
			// sanitizedIdentifier used as artifactId and dynamic sub package
			String sanitizedIdentifier = sanitizeIdentifier(identifier);
			
			releaseId = kieServices.newReleaseId(DYNAMIC_BASE_PACKAGE, sanitizedIdentifier, "1.0");
			
			buildModule();

		}
		
		private KieContainer openKieContainer() {
			return kieServices.newKieContainer(releaseId);
		}
		
		private void buildModule() {
			logger.debug("Building KieModule for [" + identifier + "]");
			
			StopWatch stopWatch = new StopWatch();
			
			buildModuleDeclaration();
			transferRule();
			kieFileSystem.generateAndWritePomXML(releaseId);
			
			KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
			kb.buildAll();
			
			if (kb.getResults().hasMessages(Level.ERROR)) {
				throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
			}
			
			logger.debug("Built KieModule for [" + identifier + "] in " + stopWatch.getLastElapsedTime() + "ms");
		}
		
		private void buildModuleDeclaration() {
			KieModuleModel moduleModel = kieServices.newKieModuleModel();

			KieBaseModel kieBaseModel = moduleModel.newKieBaseModel("defaultBase");

			kieBaseModel.setDefault(true);
			kieBaseModel.setEqualsBehavior(EqualityBehaviorOption.EQUALITY);

			KieSessionModel sessionModel = kieBaseModel.newKieSessionModel("defaultSession");
			sessionModel.setDefault(true);
			sessionModel.setType(KieSessionType.STATELESS);
			
			String kmodule = moduleModel.toXML();
			kieFileSystem.writeKModuleXML(kmodule);
		}
		
		@Override
		public StatelessKieSession openSession() {
			return kieContainer.get().newStatelessKieSession();
		}
		
		private void transferRule() {
			// transfer the whole content from the pipe to a resource under the correct path
			try (InputStream in = ruleStreamProvider.openInputStream()) {
				
				String resourcePath = RESOURCES_PATH.push("rules.drl").toSlashPath();
				
				byte[] bytes = IOTools.slurpBytes(in);
				ByteArrayResource resource = new ByteArrayResource(bytes, "UTF-8");
				kieFileSystem.write(resourcePath, resource);
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		
		private String sanitizeIdentifier(String identifier) {
			int length = identifier.length();
			StringBuilder builder = new StringBuilder(length);
			
			for (int i = 0; i < length; i++) {
				char c = identifier.charAt(i);
				
				if (i == 0) {
					if (!Character.isJavaIdentifierStart(c)) {
						builder.append('_');
					}
				}

				if (Character.isJavaIdentifierPart(c)) {
					builder.append(c);
				}
				else {
					builder.append("_");
				}
			}
			
			return builder.toString();
		}

		@Override
		public void close() {
			logger.debug("Closing KieModule for [" + identifier + "]");
			
			if (kieContainer.isInitialized())
				kieContainer.close();
			
			kieServices.getRepository().removeKieModule(releaseId);
		}

	}
}
