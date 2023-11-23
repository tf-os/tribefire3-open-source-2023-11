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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.EntityFactory;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.PlaceholderSupport;
import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilder;
import com.braintribe.codec.marshaller.api.options.attributes.AbsentifyMissingPropertiesOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.gm.config.yaml.api.ConfigurationReadBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.config.ConfigurationError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.clone.async.AsyncCloningImpl;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeContextBuilder;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.provider.Holder;

/**
 * A Utility class to read configurations written in yaml with support for root type inference, property placeholders, default initialization, variable resolving and different input options.
 * @author Dirk Scheffler
 *
 */
public abstract class YamlConfigurations {
	private static final YamlMarshaller marshaller;
	
	static {
		marshaller = new YamlMarshaller();
		marshaller.setV2(true);
	}
	
	/**
	 * <p>
	 * Starts a {@link ConfigurationReadBuilder} builder that parses a certain {@link EntityType} by using that as root type inference and generic type parameterization. The root type inference
	 * allows to read completely untyped yaml configurations.
	 * 
	 * <p>
	 * If no further option other that a from method is choosen on the builder it will be in the following mode:
	 * 
	 * <ul>
	 * 	<li>entity default initialization active
	 *  <li>placeholder support inactive
	 *  <li>value descriptor resolving inactive
	 *  <li>root type inference active
	 *  <li>
	 * </ul>
	 */
	public static <C extends GenericEntity> ConfigurationReadBuilder<C> read(EntityType<C> type) {
		return new ConfigurationReadBuilderImpl<C>(type);
	}
	
	/**
	 * <p>
	 * Starts a {@link ConfigurationReadBuilder} builder that parses a certain {@link GenericModelType} by using that as root type inference. The generic type parameterization must come
	 * by other ways such as left value type inference or explizit parameterization of the call. The root type inference
	 * allows to read completely untyped yaml configurations.
	 * Starts a {@link ConfigurationReadBuilder} builder that parses a certain {@link EntityType} by using that as root type inference and generic type parameterization. The root type inference
	 * allows to read completely untyped yaml configurations.
	 * 
	 * <p>
	 * If no further option other that a from method is choosen on the builder it will be in the following mode:
	 * 
	 * <ul>
	 * 	<li>entity default initialization active
	 *  <li>placeholder support inactive
	 *  <li>value descriptor resolving inactive
	 *  <li>root type inference active
	 *  <li>
	 * </ul>
	 */
	public static <C> ConfigurationReadBuilder<C> read(GenericModelType type) {
		return new ConfigurationReadBuilderImpl<C>(type);
	}
	
	private static class ConfigurationReadBuilderImpl<E> implements ConfigurationReadBuilder<E> {
		private GmDeserializationContextBuilder optionsBuilder;
		private Function<Variable, Object> resolver;
		private GenericModelType type;
		
		public ConfigurationReadBuilderImpl(GenericModelType type) {
			this.type = type;
			optionsBuilder = GmDeserializationOptions.deriveDefaults().setInferredRootType(type).set(EntityFactory.class, EntityType::create);
		}

		@Override
		public ConfigurationReadBuilder<E> options(Consumer<GmDeserializationContextBuilder> configurer) {
			configurer.accept(optionsBuilder);
			return this;
		}
		
		@Override
		public ConfigurationReadBuilder<E> placeholders() {
			optionsBuilder.set(PlaceholderSupport.class, true);
			return this;
		}
		
		@Override
		public ConfigurationReadBuilder<E> placeholders(Function<Variable, Object> resolver) {
			placeholders();
			this.resolver = resolver;
			return this;
		}
		
		@Override
		public ConfigurationReadBuilder<E> absentifyMissingProperties() {
			noDefaulting();
			optionsBuilder.set(AbsentifyMissingPropertiesOption.class, true);
			
			return this;
		}
		
		@Override
		public ConfigurationReadBuilder<E> noDefaulting() {
			optionsBuilder.set(EntityFactory.class, EntityType::createRaw);
			return this;
		}
		
		@Override
		public Maybe<E> from(InputStream in) {
			Maybe<E> configMaybe = (Maybe<E>)marshaller.unmarshallReasoned(in, optionsBuilder.build());
			return configMaybe.flatMap(this::postProcessConfig);
		}
		
		@Override
		public Maybe<E> from(Reader reader) {
			Maybe<E> configMaybe = (Maybe<E>)marshaller.unmarshallReasoned(reader, optionsBuilder.build());
			return configMaybe.flatMap(this::postProcessConfig);
		}

		private Maybe<E> postProcessConfig(E config) {
			if (resolver != null) {
				VdeRegistry vdeRegistry = VDE.registryBuilder()
						.loadDefaultSetup()
						.withConcreteExpert(AbsenceInformation.class, new AiEvaluator())
						.done();
				
				VdeContextBuilder builder = VDE.evaluate()
						.withRegistry(vdeRegistry)
						.with(VariableProviderAspect.class, resolver);
				
				Holder<E> resultHolder = new Holder<>();
				
				new AsyncCloningImpl((vd,c) -> c.onSuccess(builder.forValue(vd)), Runnable::run, e -> false)
				.cloneValue(config, AsyncCallback.of(
						resultHolder, 
						t -> t.printStackTrace()
						));
				
				config = resultHolder.get();
			}
			
			return Maybe.complete(config);
		}
		
		@Override
		public Maybe<E> from(InputStreamProvider streamProvider) {
			try (InputStream in = streamProvider.openInputStream()) {
				return from(in);
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		
		@Override
		public Maybe<E> from(File file) {
			if (file.exists()) {
				Maybe<E> maybe = from(() -> new FileInputStream(file));
				
				if (maybe.isUnsatisfied())
					return errorFor(file, maybe.whyUnsatisfied());
				
				return maybe;
			} else
				return Reasons.build(NotFound.T) //
						.text("File not found: " + file.getAbsolutePath()) //
						.toMaybe();
		}
		
		private <T> Maybe<T> errorFor(File file, Reason cause) {
			return Reasons.build(ConfigurationError.T).text("Error while reading " + type.getTypeSignature() + " configuration from: " + file.getAbsolutePath()).cause(cause).toMaybe();
		}
		
		@Override
		public Maybe<E> from(URL url) {
			return from(url::openStream);
		}
	}
	
	private static class AiEvaluator implements ValueDescriptorEvaluator<AbsenceInformation> {
		@Override
		public VdeResult evaluate(VdeContext context, AbsenceInformation valueDescriptor) throws VdeRuntimeException {
			return new VdeResultImpl(valueDescriptor, false);
		}
	}
}
