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
package com.braintribe.devrock.templates.support.compose;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.templates.model.Dependency;
import com.braintribe.devrock.templates.model.artifact.CreateArtifact;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

/**
 * Base class for a processor of a complex request that consists of multiple {@link CreateArtifact} requests.
 * <p>
 * Such a higher-level request is useful e.g. when creating an extension consisting of a module, deployment/api/data models, processing artifact and
 * tests... A matching processor would create all these artifacts including the expected dependencies, such as the module would depend on all three
 * models from the previous example.
 * <p>
 * To get an idea see CreateArtifactsProcessor (tribefire.extension.setup) or HydruxSetupProcessor (tribefire.extension.hydrux).
 * 
 * @author peter.gazdik
 */
public abstract class CompositeProcessor {

	protected final File groupFolder;
	protected final ServiceRequestContext context;
	protected final String baseName;
	protected final boolean overwrite;

	protected final List<PartHandler<?>> handlers = newList();

	// TODO use CompositeRequest (doesn't exit yet), rather than baseName and overwrite - this makes it more extensible.
	public CompositeProcessor(ServiceRequestContext context, File groupFolder, String baseName, boolean overwrite) {
		this.context = context;
		this.groupFolder = groupFolder;
		this.baseName = baseName;
		this.overwrite = overwrite;
	}

	public final void run() {
		initHandlers();

		if (!overwrite)
			if (warnIfAnyDirExists())
				return;

		letHandlersHandleBro();
	}

	protected abstract void initHandlers();

	protected boolean warnIfAnyDirExists() {
		List<File> existingDirs = handlers.stream() //
				.map(handler -> handler.dir) //
				.filter(File::exists) //
				.collect(Collectors.toList());

		if (existingDirs.isEmpty())
			return false;

		println(//
				existingDirs.stream() //
						.map(file -> yellow(file.getAbsolutePath())) //
						.collect(ConsoleOutputs.joiningCollector(text("\n"), red("Following files already exist:\n"), null)) //
		);

		return true;
	}

	protected void letHandlersHandleBro() {
		for (PartHandler<?> handler : handlers)
			handler.handle();
	}

	protected InputStream loadResourceRelativeToThisClass(String name) {
		return loadResourceRelativeToClass(name, getClass());
	}

	protected InputStream loadResourceRelativeToClass(String name, Class<?> clazz) {
		String urlBase = clazz.getPackage().getName().replace(".", "/");
		return clazz.getClassLoader().getResourceAsStream(urlBase + "/" + name);
	}

	/**
	 * Handles one part of the composite request, i.e. one of the several {@link CreateArtifact} requests.
	 */
	public abstract class PartHandler<T extends CreateArtifact> {

		public final File dir;
		public final T request;

		public PartHandler(String fileName, EntityType<T> requestType) {
			this.dir = new File(groupFolder, fileName);
			this.request = newRequest(requestType);
		}

		public void handle() {
			println(sequence(text("Creating: "), yellow(dir.getName())));
			println();

			configureRequest();
			request.eval(context.getEvaluator()).get();
		}

		protected abstract void configureRequest();

		protected T newRequest(EntityType<T> entityType) {
			T request = entityType.create();
			request.setArtifactId(dir.getName());
			request.setInstallationPath(groupFolder.getAbsolutePath());
			request.setOverwrite(overwrite);
			return request;
		}

		protected void addDeps(Dependency... deps) {
			request.getDependencies().addAll(Arrays.asList(deps));
		}

		protected Dependency locJsDep(String artifactId) {
			String groupId = groupId();
			return jsDep(groupId, artifactId);
		}

		protected Dependency jsDep(String groupId, String artifactId) {
			return dep(groupId, artifactId, DependencyTag.js);
		}

		protected Dependency locAssDep(String artifactId) {
			String groupId = groupId();
			return assDep(groupId, artifactId);
		}

		protected Dependency assDep(ArtifactReflection ar) {
			return dep(ar, DependencyTag.asset);
		}

		protected Dependency assDep(String groupId, String artifactId) {
			return dep(groupId, artifactId, DependencyTag.asset);
		}

		protected Dependency locLibDep(String artifactId) {
			String groupId = groupId();
			return libDep(groupId, artifactId);
		}

		protected Dependency libDep(ArtifactReflection ar) {
			return dep(ar, null);
		}

		protected Dependency libDep(String groupId, String artifactId) {
			return dep(groupId, artifactId, null);
		}

		protected Dependency dep(ArtifactReflection ar, DependencyTag tag) {
			return dep(ar.groupId(), ar.artifactId(), tag);
		}

		protected Dependency dep(String groupId, String artifactId, DependencyTag tag) {
			Dependency result = Dependency.T.create();
			result.setGroupId(groupId);
			result.setArtifactId(artifactId);
			result.setVersion("${V." + groupId + "}");

			if (tag != null)
				result.getTags().add(tag.name());

			return result;
		}

		protected Dependency assOnly(Dependency dep) {
			dep.setType("man");
			dep.setClassifier("asset");

			return dep;
		}

		protected String groupId() {
			return groupFolder.getName();
		}

	}

}
