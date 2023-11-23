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
package tribefire.platform.wire.space;

import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_CACHE_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_CONFIGURATION_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_DATA_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_REPO_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_SETUP_INFO_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_STORAGE_DIR;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_TMP_DIR;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.module.wire.contract.WebPlatformResourcesContract;
import tribefire.platform.api.resource.ResourcesBuilder;
import tribefire.platform.wire.space.common.ResourcesBaseSpace;

@Managed
public class MasterResourcesSpace extends ResourcesBaseSpace implements WebPlatformResourcesContract {

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		super.onLoaded();
		builders.put("storage", this::storage);
		builders.put("tmp", this::tmp);
		builders.put("cache", this::cache);
		builders.put("repo", this::repository);
		builders.put("data", this::database);
		builders.put("setup-info", this::setupInfo);
	}

	@Override
	public ResourcesBuilder storage(String url) {
		return PathResourcesBuilder.create(storagePath(), resolve(url));
	}

	@Override
	public ResourcesBuilder tmp(String url) {
		return PathResourcesBuilder.create(tmpPath(), resolve(url));
	}

	@Override
	public ResourcesBuilder cache(String url) {
		return PathResourcesBuilder.create(cachePath(), resolve(url));
	}

	public ResourcesBuilder repository(String url) {
		return PathResourcesBuilder.create(repositoryPath(), resolve(url));
	}

	@Override
	public ResourcesBuilder database(String url) {
		return PathResourcesBuilder.create(databasesPath(), resolve(url));
	}

	public ResourcesBuilder conf(String path) throws UncheckedIOException {
		return PathResourcesBuilder.create(confPath(), resolve(path));
	}

	@Override
	public ResourcesBuilder publicResources(String url) {
		return PathResourcesBuilder.create(publicResourcesPath(), resolve(url));
	}

	public ResourcesBuilder setupInfo(String url) {
		return PathResourcesBuilder.create(setupPath(), resolve(url));
	}

	@Managed
	public Path storagePath() {
		return resolvePath(serverPath(), "../storage", ENVIRONMENT_STORAGE_DIR);
	}

	@Managed
	public Path tmpPath() {
		return resolvePath(storagePath(), "tmp", ENVIRONMENT_TMP_DIR);
	}

	@Managed
	public Path cachePath() {
		return resolvePath(storagePath(), "cache", ENVIRONMENT_CACHE_DIR);
	}

	@Managed
	public Path repositoryPath() {
		return resolvePath(storagePath(), "repository", ENVIRONMENT_REPO_DIR);
	}

	@Managed
	public Path databasesPath() {
		return resolvePath(storagePath(), "databases", ENVIRONMENT_DATA_DIR);
	}

	@Managed
	public Path confPath() {
		return resolvePath(serverPath(), "../conf", ENVIRONMENT_CONFIGURATION_DIR);
	}

	public Path publicResourcesPath() {
		return storagePath().resolve("public-resources");
	}

	@Managed
	public Path setupPath() {
		return resolvePath(storagePath(), "../setup-info", ENVIRONMENT_SETUP_INFO_DIR);
	}

	private Path resolvePath(Path defaultParent, String defaultPath, String propertyName) {
		Path custom = customizedPath(defaultParent, propertyName);
		if (custom != null) {
			return custom;
		}
		return defaultParent.resolve(defaultPath);
	}

	private Path customizedPath(Path parent, String propertyName) {

		String propertyValue = TribefireRuntime.getProperty(propertyName);

		if (propertyValue == null) {
			return null;
		}

		propertyValue = resolve(propertyValue);

		Path customPath = Paths.get(propertyValue);

		if (customPath.isAbsolute()) {
			return customPath;
		}

		return parent.resolve(customPath);

	}

}
