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
package tribefire.cortex.remotes.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.csa.DynamicInitializer;
import com.braintribe.model.deployment.HttpServer;
import com.braintribe.model.deployment.remote.GmWebRpcRemoteServiceProcessor;
import com.braintribe.model.deployment.remote.RemoteDomainIdMapping;
import com.braintribe.model.deployment.remote.RemotifyingInterceptor;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.FileTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.tools.SimpleAssemblyImporter;
import tribefire.cortex.initializer.tools.ServiceDomainSupport;
import tribefire.cortex.remotes.model.RemoteServiceDomain;
import tribefire.cortex.remotes.model.Remotes;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.MarshallingContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

/**
 * This module binds an implementation for a {@link DynamicInitializer} which configures a single {@link GmWebRpcRemoteServiceProcessor} with a single
 * {@link RemotifyingInterceptor} and multiple remote domains. By remote domains we mean domains where all the request are handled by the mentioned
 * service processor and interceptor.
 * <p>
 * The input for this initializer is expected to be a single file called "remotes.yml", who's content describes a single {@link Remotes} instance.
 */
@Managed
public class RemotesConfiguringModuleSpace implements TribefireModuleContract {

	private static final String REMOTES_YML = "remotes.yml";

	@Import
	private TribefirePlatformContract tfPlatform;

	@Import
	private MarshallingContract marshalling;

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bindDynamicInitializerFactory(this::newRemotesInitializer);
	}

	private DataInitializer newRemotesInitializer(File inputFolder) {
		return ctx -> new RemotesInitialization(ctx, inputFolder).run();
	}

	private Marshaller ymlMarshaller() {
		return marshalling.registry().getMarshaller("text/yaml");
	}

	private class RemotesInitialization {

		private final PersistenceInitializationContext context;
		private final ManagedGmSession session;
		private final File inputFolder;

		private final String cfgIdentifier; // groupId_artidactId
		private final String cfgDetails; // Remotes configuration: ${cfIdentifier}
		private String domainCfgDetails;

		private final Remotes remoteServices;

		private String domainId;
		private String domainName;
		private String remoteDomainId;
		private List<Model> models;

		// cortex entities
		private GmWebRpcRemoteServiceProcessor sp;
		private RemotifyingInterceptor rai;

		public RemotesInitialization(PersistenceInitializationContext context, File inputFolder) {
			this.context = context;
			this.session = context.getSession();
			this.inputFolder = inputFolder;
			this.cfgIdentifier = removeVersion(inputFolder.getName());
			this.cfgDetails = " Retmotes configuration: " + inputFolder.getName();
			this.remoteServices = parseRemoteServices();
		}

		/** The folder name is in the form groupId:artifactId#version, so we get rid of the #version part. */
		private String removeVersion(String configFolderName) {
			int i = configFolderName.indexOf("#");
			return i < 0 ? configFolderName : configFolderName.substring(0, i);
		}

		private Remotes parseRemoteServices() {
			File remotesYmlFile = new File(inputFolder, REMOTES_YML);
			return (Remotes) FileTools.read(remotesYmlFile).fromInputStream(ymlMarshaller()::unmarshall);
		}

		public void run() {
			createRemoteServiceProcessor();
			createRemotifyingInterceptor();
			createRemoteDomains();
		}

		private void createRemoteServiceProcessor() {
			String serverUrl = requireNonNull(remoteServices.getServerUrl(), () -> "ServerUrl cannot be null" + cfgDetails);
			String serverUri = requireNonNull(remoteServices.getServerUri(), () -> "ServerUri cannot be null" + cfgDetails);

			HttpServer server = session.create(HttpServer.T, "server:HttpServer/" + cfgIdentifier);
			server.setBaseUrl(serverUrl);

			sp = session.create(GmWebRpcRemoteServiceProcessor.T, "serviceProcessor:GmWebRpcRemoteServiceProcessor/" + cfgIdentifier);
			sp.setExternalId("serviceProcessor:GmWebRpcRemoteServiceProcessor/" + cfgIdentifier);
			sp.setName("Remote Processor for '" + domainName + "'");
			sp.setUri(serverUri);
			sp.setServer(server);
		}

		private void createRemotifyingInterceptor() {
			rai = session.create(RemotifyingInterceptor.T, "aroundProcessor:RemotifyingInterceptor/" + cfgIdentifier);
			rai.setExternalId("aroundProcessor:RemotifyingInterceptor/" + cfgIdentifier);
			rai.setName("Remotifying Interceptor for '" + domainName + "'");
			rai.setDecryptCredentials(remoteServices.getDecryptCredentials());
			rai.setCredentials(importCredentials());

		}

		private Credentials importCredentials() {
			Credentials credentials = remoteServices.getServerCredentials();
			String globalIdPrefix = "aroundProcessor:RemotifyingInterceptor:Credentials/" + cfgIdentifier;

			return SimpleAssemblyImporter.importAssembly(credentials, session, globalIdPrefix);
		}

		private void createRemoteDomains() {
			for (RemoteServiceDomain rsd : nullSafe(remoteServices.getDomains()))
				createRemoteDomain(rsd);
		}

		private void createRemoteDomain(RemoteServiceDomain rsd) {
			initDomain(rsd);

			ServiceDomainSupport.domainInitializer(configAssetIdentifier(), domainId) //
					.withServiceProcessor(ServiceRequest.T, sp) // configures the ProcessWith MD
					.withModels(OpenUserSession.T.getModel()) //
					.withModels(models) //
					.withMdConfigurer((mdEditor, _session) -> configureRemoteMds(mdEditor, rai)) //
					.forNewDomain(domainName) //
					.initialize(context);
		}

		private void initDomain(RemoteServiceDomain rsd) {
			domainId = requireNonNull(rsd.getExternalId(), () -> "Domain's externalId cannot be null." + cfgDetails);
			domainCfgDetails = " DomainId: " + domainId + "," + cfgDetails;
			domainName = requireNonNull(rsd.getName(), () -> "Domain's name cannot be null." + domainCfgDetails);

			remoteDomainId = requireNonNull(rsd.getRemoteDomainId(), () -> "RemoteDomainId cannot be null" + domainCfgDetails);

			models = resolveModels(rsd);
		}

		private List<Model> resolveModels(RemoteServiceDomain rsd) {
			List<String> modelNames = rsd.getModels();

			if (isEmpty(modelNames))
				throw new NoSuchElementException("No models configured." + domainCfgDetails);

			return modelNames.stream() //
					.map(GMF.getTypeReflection()::getModel) //
					.collect(Collectors.toList());
		}

		private void configureRemoteMds(ModelMetaDataEditor mdEditor, RemotifyingInterceptor rai) {
			configureAroundProcessWthRemotifyingInterceptor(mdEditor, rai);

			configureMapping(mdEditor, ServiceRequest.T, remoteDomainId);
		}

		private void configureAroundProcessWthRemotifyingInterceptor(ModelMetaDataEditor mdEditor, RemotifyingInterceptor rai) {
			AroundProcessWith apw = session.create(AroundProcessWith.T, "aroundProcessWith:RemotifyingInterceptor/" + cfgIdentifier);
			apw.setProcessor(rai);

			mdEditor.onEntityType(ServiceRequest.T).addMetaData(apw);
		}

		private void configureMapping(ModelMetaDataEditor mdEditor, EntityType<?> et, String remoteDomain) {
			RemoteDomainIdMapping mapToGreenDomainA = session.create(RemoteDomainIdMapping.T,
					"remoteServerMapping/" + cfgIdentifier + ":" + et.getTypeSignature());
			mapToGreenDomainA.setDomainId(remoteDomain);

			mdEditor.onEntityType(et).addMetaData(mapToGreenDomainA);
		}

		private String configAssetIdentifier() {
			return "remotes-configuring-module (processing:" + cfgIdentifier + ")";
		}

	}
}
