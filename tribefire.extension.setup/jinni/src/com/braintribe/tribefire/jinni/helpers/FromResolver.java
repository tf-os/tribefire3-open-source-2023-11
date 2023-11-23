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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.EntityFactory;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilder;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.gm.config.yaml.ConfigVariableResolver;
import com.braintribe.gm.config.yaml.YamlConfigurations;
import com.braintribe.gm.config.yaml.api.ConfigurationReadBuilder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.From;
import com.braintribe.model.jinni.api.FromFile;
import com.braintribe.model.jinni.api.FromStdin;
import com.braintribe.model.jinni.api.FromUrl;
import com.braintribe.model.resource.FileResource;
import com.braintribe.tribefire.jinni.wire.contract.JinniContract;
import com.braintribe.utils.stream.KeepAliveDelegateInputStream;
import com.braintribe.ve.api.VirtualEnvironment;

/**
 * @author peter.gazdik
 */
public class FromResolver implements Function<From, GenericEntity> {

	private final JinniContract jinniContract;
	private VirtualEnvironment virtualEnvironment;

	public FromResolver(JinniContract jinniContract, VirtualEnvironment virtualEnvironment) {
		this.jinniContract = jinniContract;
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	public GenericEntity apply(From from) {

		String mimeType = from.getMimeType();

		Marshaller marshaller = jinniContract.marshallerRegistry().getMarshaller(mimeType);

		if (marshaller == null)
			throw new NoSuchElementException("No marshaller registered for mimetype: " + mimeType);
		
		if (marshaller instanceof YamlMarshaller && from instanceof FromFile && ((FromFile)from).getHasVars()) {
			FromFile fromFile = (FromFile)from;
			File file = new File(fromFile.getFile().getPath());
			
			ConfigVariableResolver variableResolver = new ConfigVariableResolver(virtualEnvironment, file);
					
			ConfigurationReadBuilder<GenericEntity> builder = YamlConfigurations.read(GenericEntity.T).placeholders(variableResolver::resolve);
			if (from.getReproduce())
				builder.noDefaulting();
			
			return builder.from(file).get();
		}
		else {
			try (InputStream in = openInputStream(from)) {
				GmDeserializationContextBuilder options = GmDeserializationOptions.deriveDefaults();
				// options
				if (!from.getReproduce())
					options = options.set(EntityFactory.class, EntityType::create);

				return (GenericEntity) marshaller.unmarshall(in, options.build());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private InputStream openInputStream(From from) throws IOException {
		if (from instanceof FromStdin) {
			return new KeepAliveDelegateInputStream(System.in);
		} else if (from instanceof FromUrl) {
			FromUrl fromUrl = (FromUrl) from;
			String urlProperty = fromUrl.getUrl();

			if (urlProperty == null)
				throw new IllegalStateException("FromUrl is missing url");

			URL url = new URL(urlProperty);
			return url.openStream();
		} else if (from instanceof FromFile) {
			FromFile fromFile = (FromFile) from;
			FileResource file = fromFile.getFile();

			if (file == null)
				throw new IllegalStateException("FromFile is missing file");

			return file.openStream();
		} else {
			throw new NoSuchElementException("No support for From type: " + from.entityType());
		}
	}
}