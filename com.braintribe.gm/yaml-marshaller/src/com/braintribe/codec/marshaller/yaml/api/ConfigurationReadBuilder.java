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
package com.braintribe.codec.marshaller.yaml.api;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.value.Variable;

/**
 * Prepares a yaml configuration read with fluent methods. After the preparation is done one of the from methods can be called to trigger the actual reading and potential post processing.
 * @author Dirk Scheffler
 *
 * @param <T> The expected root type of the assembly return from any of the from methods.
 */
public interface ConfigurationReadBuilder<T> {

	/**
	 * Reads the configuration from the given file. If the file is not present the {@link Maybe} will be unsatisfied with a {@link NotFound} reason.
	 */
	Maybe<T> from(File file);

	/**
	 * Reads the configuration from the given url file.
	 */
	Maybe<T> from(URL file);

	/**
	 * Reads the configuration from the input stream that is taken from the given provider. The {@link InputStream} will be closed after reading.
	 */
	Maybe<T> from(InputStreamProvider streamProvider);

	/**
	 * Reads the configuration from the given input stream. The {@link InputStream} <b>won't</b> be closed after reading.
	 */
	Maybe<T> from(InputStream in);
	
	/**
	 * Reads the configuration from the given reader. The {@link Reader} <b>won't</b> be closed after reading.
	 */
	Maybe<T> from(Reader reader);

	/**
	 * Activates the placeholder parsing in the yaml unmarshalling and further more the ValueDescriptor resolving using the given resolver.
	 */
	ConfigurationReadBuilder<T> placeholders(Function<Variable, Object> resolver);

	/**
	 * Activates the placeholder parsing in the yaml unmarshalling. ValueDescriptors are not being resolved but returned within the final assembly.
	 */
	ConfigurationReadBuilder<T> placeholders();

	/**
	 * Deactivates the entity default initialization.
	 */
	ConfigurationReadBuilder<T> noDefaulting();

	/**
	 * Allows to further control the yaml marshaller's working with a configurer that can apply options on a {@link GmDeserializationContextBuilder}.
	 */
	ConfigurationReadBuilder<T> options(Consumer<GmDeserializationContextBuilder> configurer);

	ConfigurationReadBuilder<T> absentifyMissingProperties();

}
