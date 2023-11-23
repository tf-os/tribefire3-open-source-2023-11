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
package com.braintribe.gm.config.yaml;

import static com.braintribe.utils.lcd.StringTools.camelCaseToSocialDistancingCase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;


/**
 * This implementation of {@link ModeledConfiguration} uses the filesystem and yaml marshalled modeled data to retrieve configurations.
 * 
 * <p>
 * The lookup strategy is: 
 * 
 * <ol>
 * 	<li>build a filename using kebab cased variant of the {@link EntityType#getShortName() type short name} suffixed with <b>.yaml</b> and 
 * try to find this file in the {@link #setConfigFolder(File) config folder}. (e.g. foo.bar.MyConfig -> &lt;config-folder&gt;/my-config.yaml)
 *  <li>if the file was not found a default initialialized instance of the config type will be created.
 * </ol>
 * 
 * <p>
 * If the configuration is read from the filesystem the yaml unmarshalling supports default initialization, and variables such as:
 * 
 * <ul>
 * 	<li> ${config.base} which references the {@link #setConfigFolder(File) config folder}
 * 	<li> ${env.SOME_ENV_VAR} resolves OS environment variables
 * </ul>
 *  
 * @author dirk.scheffler
 *
 */
public class ModeledYamlConfiguration implements ModeledConfiguration {
	private static final Logger logger = Logger.getLogger(ModeledYamlConfiguration.class);
	private Map<EntityType<? extends GenericEntity>, LazyInitialized<Maybe<? extends GenericEntity>>> configs = new ConcurrentHashMap<>();
	private File configFolder;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private boolean writePooled;
	private LazyInitialized<Map<String, String>> properties = new LazyInitialized<>(this::loadProperties);
	
	@Required
	public void setConfigFolder(File configFolder) {
		this.configFolder = configFolder;
	}
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Configurable
	public void setWritePooled(boolean writePooled) {
		this.writePooled = writePooled;
	}
	
	public <C extends GenericEntity> C config(EntityType<C> configType) {
		return configReasoned(configType).get();
	}
	
	public <C extends GenericEntity> void store(EntityType<C> configType, C config) {
		YamlMarshaller yamlMarshaller = new YamlMarshaller();
		yamlMarshaller.setWritePooled(writePooled);
		File configFile = new File(configFolder, buildConfigFileName(configType));
		
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults() //
			.inferredRootType(configType)
			.setOutputPrettiness(OutputPrettiness.high)
			.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic)
			.build();
		
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(configFile))) {
			yamlMarshaller.marshall(out, config, options);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		configs.remove(configType);
	}
	
	private String buildConfigFileName(EntityType<?> type) {
		return StringTools.camelCaseToDashSeparated(type.getShortName()).toLowerCase() + ".yaml";
	}
	
	public <C extends GenericEntity> void store(C config) {
		store(config.entityType(), config);
	}
	
	public <C extends GenericEntity> Maybe<C> configReasoned(EntityType<C> configType) {
		return (Maybe<C>) configs.computeIfAbsent(configType, k -> new LazyInitialized<>(() -> this.loadConfig(k))).get();
	}
	
	private Maybe<? extends GenericEntity> loadConfig(EntityType<?> configType) {
		String fileName = camelCaseToSocialDistancingCase(configType.getShortName()) + ".yaml";
		File configFile = new File(configFolder, fileName);
		
		return new ModeledYamlConfigurationLoader() //
			.virtualEnvironment(virtualEnvironment) //
			.variableResolver(this::resolveProperty) //
			.loadConfig(configType, configFile, false);
	}
	
	private String resolveProperty(String name) {
		return properties.get().get(name);
	}
	
	private Map<String, String> loadProperties() {
		MapType configType = GMF.getTypeReflection().getMapType(EssentialTypes.TYPE_STRING, EssentialTypes.TYPE_STRING);
		File configFile = new File(configFolder, "properties.yaml");
		
		Maybe<Map<String, String>> propertiesMaybe = new ModeledYamlConfigurationLoader() //
				.virtualEnvironment(virtualEnvironment) //
				.loadConfig(configType, configFile, Collections::emptyMap, false);
		
		if (propertiesMaybe.isSatisfied())
			return propertiesMaybe.get();
		
		logger.error("Error while reading config properties from [" + configFile + "]: " + propertiesMaybe.whyUnsatisfied().stringify());
		
		return Collections.emptyMap();
	}
}
