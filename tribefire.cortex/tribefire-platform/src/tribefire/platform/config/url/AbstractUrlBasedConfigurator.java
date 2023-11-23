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
package tribefire.platform.config.url;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;

/**
 * This is a subclass of {@link AbstractExternalConfigurator} that loads entries from a JSON configuration file. The
 * location of the file is provided by an actual subclass of this class. The location could either be a file path or a
 * URL that allows to download the content from a remote server.
 */
public abstract class AbstractUrlBasedConfigurator extends AbstractExternalConfigurator {

	private static final Logger log = Logger.getLogger(AbstractUrlBasedConfigurator.class);

	/**
	 * Returns a File object pointing to the default place where the file would be expected. It internally calls
	 * {@link #buildDefaultFileName()} to get the default file name. If this file name is a valid path itself to an
	 * existing file, it will be used directly. Otherwise, a file path with the prefix returned by
	 * {@link TribefireRuntime#getConfigurationDir()} will be returned (while not checking whether this file really
	 * exists).
	 * 
	 * @return A {@link File} object pointing to a potential location of the configuration file.
	 */
	public File getDefaultConfigFile(Supplier<String> defaultFileNameSupplier) {
		String defaultName = defaultFileNameSupplier.get();
		File candidate = new File(defaultName);

		if (candidate.exists() && candidate.isFile())
			return candidate;
		else
			return new File(TribefireRuntime.getConfigurationDir(), defaultName);
	}

	@Override
	protected List<RegistryEntry> getEntries() throws ConfiguratorException {

		List<RegistryEntry> result = new ArrayList<>();

		String configurationInjectionUrlProperty = buildUrlProperty();
		String configurationUrl = TribefireRuntime.getProperty(configurationInjectionUrlProperty);
		if (configurationUrl == null) {
			// No configuration URL found. Now try to find it in default location.
			File defaultCfgFile = getDefaultConfigFile(this::buildDefaultFileName);
			if (defaultCfgFile.exists()) {
				log.info("No explicit configuration URL defined but found configuration file at default location: "
						+ defaultCfgFile.getAbsolutePath());
				configurationUrl = defaultCfgFile.toURI().toString();
			}
		}

		if (configurationUrl != null) {
			List<RegistryEntry> entries = readConfigurationUrl(configurationUrl);
			if (entries != null && !entries.isEmpty()) {
				result.addAll(entries);
			}
		}

		configurationInjectionUrlProperty = buildSharedUrlProperty();
		configurationUrl = TribefireRuntime.getProperty(configurationInjectionUrlProperty);
		if (configurationUrl == null) {
			// No configuration URL found. Now try to find it in default location.
			File defaultCfgFile = getDefaultConfigFile(this::buildSharedDefaultFileName);
			if (defaultCfgFile.exists()) {
				log.info("No explicit configuration URL defined but found configuration file at default location: "
						+ defaultCfgFile.getAbsolutePath());
				configurationUrl = defaultCfgFile.toURI().toString();
			}
		}

		if (configurationUrl != null) {
			final String url = configurationUrl;
			List<RegistryEntry> entries = readConfigurationUrl(configurationUrl);
			if (entries != null && !entries.isEmpty()) {
				entries.forEach(e -> e.setSource("URL: " + url));
				result.addAll(entries);
			}
		}

		return result;
	}

	@Override
	protected String getSourceInformation() {
		String configurationInjectionUrlProperty = buildUrlProperty();
		String configurationUrl = TribefireRuntime.getProperty(configurationInjectionUrlProperty);
		if (configurationUrl == null) {
			// No configuration URL found. Now try to find it in default location.
			File defaultCfgFile = getDefaultConfigFile(this::buildDefaultFileName);
			if (defaultCfgFile.exists()) {
				return defaultCfgFile.toURI().toString();
			}
		}
		return configurationUrl;
	}

	private String buildSharedDefaultFileName() {
		return "configuration.shared.json";
	}

	private String buildSharedUrlProperty() {
		return TribefireRuntime.ENVIRONMENT_CONFIGURATION_INJECTION_URL + "_SHARED";
	}

	protected abstract String buildDefaultFileName();

	protected abstract String buildUrlProperty();

	private List<RegistryEntry> readConfigurationUrl(String configurationUrl) {
		List<RegistryEntry> entries = new ArrayList<RegistryEntry>();
		try {
			URL url = new URL(configurationUrl);

			try (Reader is = new InputStreamReader(url.openStream(), "UTF-8")) {
				return super.readConfigurationFromInputStream(is);
			}

		} catch (Exception e) {
			log.error("Could not read configuration located at: " + configurationUrl, e);
		}

		return entries;

	}

	@Override
	public String toString() {
		String defaultFilename = buildDefaultFileName();
		String urlProperty = buildUrlProperty();
		return this.getClass().getName() + " (default filename: " + defaultFilename + ", url property: " + urlProperty + ")";
	}
}
