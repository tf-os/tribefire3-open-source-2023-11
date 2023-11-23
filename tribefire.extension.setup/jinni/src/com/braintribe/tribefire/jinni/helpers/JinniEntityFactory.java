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
package com.braintribe.tribefire.jinni.helpers;

import static com.braintribe.utils.lcd.CollectionTools2.union;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.JinniOptions;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.tribefire.jinni.Jinni;
import com.braintribe.tribefire.jinni.cmdline.api.EntityFactory;
import com.braintribe.tribefire.jinni.support.request.alias.JinniAlias;
import com.braintribe.tribefire.jinni.wire.contract.JinniContract;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.string.StringDistance;

public class JinniEntityFactory implements EntityFactory {

	private final JinniAlias alias;
	private final Map<String, EntityType<?>> shortcuts;
	private JinniOptions jinniOptions;

	public JinniEntityFactory(JinniContract jinniContract) {
		this.alias = jinniContract.alias();
		this.shortcuts = jinniContract.shortcuts();
	}

	@Override
	public Maybe<GenericEntity> create(String requestIdentifier) {
		ServiceRequest request = alias.resolveAlias(requestIdentifier);

		if (request != null)
			return Maybe.complete(request);

		EntityType<?> entityType = GMF.getTypeReflection().findType(requestIdentifier);

		if (entityType == null) {
			entityType = shortcuts.get(requestIdentifier);

			if (entityType == null)
				return onNoEntityTypeFound(requestIdentifier);
		}

		if (entityType == JinniOptions.T)
			return Maybe.complete(getOptions());

		return Maybe.complete(entityType.create());
	}

	private Maybe<GenericEntity> onNoEntityTypeFound(String requestIdentifier) {
		Set<String> knownTypes = union(alias.getAliases().keySet(), shortcuts.keySet());

		String closest = StringDistance.findCloseStrings(requestIdentifier, knownTypes, 4) //
				.map(s -> "\t\t" + s) //
				.collect(Collectors.joining("\n"));

		String msg = "Unsupported type '" + requestIdentifier + "' See 'jinni help'.";

		if (!closest.isEmpty())
			msg += "\n\nThe most similar types are:\n" + closest;

		return Reasons.build(NotFound.T).text(msg).toMaybe();
	}

	private JinniOptions getOptions() {
		if (jinniOptions == null) {
			jinniOptions = createOptions();
		}

		return jinniOptions;
	}

	private JinniOptions createOptions() {
		String confDir = System.getProperty("conf.dir");

		if (confDir == null) {
			// try to infer "conf" directory from standard layout
			try {
				Path jinniPath = Path.of(Jinni.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				Path confPath = Paths.get(jinniPath.getParent().getParent().toString(), "conf");
				if (Files.exists(confPath) && Files.isDirectory(confPath)) {
					confDir = confPath.toString();
				}
			} catch (URISyntaxException e) {
				// cannot identify "conf" path. So move on without
			}
		}

		if (confDir == null)
			return JinniOptions.T.create();

		Path optionsFile = Paths.get(confDir, "options.yaml");

		if (!Files.exists(optionsFile))
			return JinniOptions.T.create();
		else
			return FileTools.read(optionsFile).fromInputStream(in ->

			unmarshallJinniOptions(in));
	}

	private JinniOptions unmarshallJinniOptions(InputStream in) {
		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
				.setInferredRootType(JinniOptions.T) //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.set(com.braintribe.codec.marshaller.api.EntityFactory.class, EntityType::create) //
				.build(); //

		return (JinniOptions) new YamlMarshaller().unmarshall(in, options);
	}

}