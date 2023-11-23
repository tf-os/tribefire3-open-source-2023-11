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
package tribefire.platform.impl.resource;

import java.io.File;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.provider.Hub;
import com.braintribe.utils.FileTools;

import tribefire.platform.api.resource.ResourcesBuilder;

/**
 * @author peter.gazdik
 */
public class ResourcesBuilderBasedPersistence<T> implements Hub<T> {

	private Supplier<ResourcesBuilder> storageResourcesBuilderFactory;
	private Marshaller marshaller;
	private GmSerializationOptions serializationOptions = GmSerializationOptions.defaultOptions.derive().outputPrettiness(OutputPrettiness.high).build();
	private String descriptor = "";

	@Required
	public void setStorageResourcesBuilderFactory(Supplier<ResourcesBuilder> storageResourcesBuilderFactory) {
		this.storageResourcesBuilderFactory = storageResourcesBuilderFactory;
	}

	@Required
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Configurable
	public void setSerializationOptions(GmSerializationOptions serializationOptions) {
		this.serializationOptions = serializationOptions;
	}

	@Configurable
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public T get() {
		return storageResourcesBuilderFactory.get().asAssembly(marshaller, null);
	}

	@Override
	public void accept(T value) {
		File file = storageResourcesBuilderFactory.get().asFile();

		try {
			FileTools.write(file).usingOutputStream(os -> marshaller.marshall(os, value, serializationOptions));

		} catch (Exception e) {
			throw new GenericModelException("Storing " + descriptor + " failed.", e);
		}
	}

}
