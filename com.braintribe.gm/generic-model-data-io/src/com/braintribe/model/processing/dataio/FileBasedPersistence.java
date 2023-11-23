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
package com.braintribe.model.processing.dataio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.provider.Hub;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class FileBasedPersistence<T> implements Hub<T> {

	private File file;
	private Marshaller marshaller;
	private GmSerializationOptions serializationOptions = GmSerializationOptions.defaultOptions;
	private GmDeserializationOptions deserializationOptions = GmDeserializationOptions.defaultOptions;
	private String descriptor = "";

	@Required
	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
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
	public void setDeserializationOptions(GmDeserializationOptions deserializationOptions) {
		this.deserializationOptions = deserializationOptions;
	}

	@Configurable
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public T get() {
		if (!file.exists())
			return null;

		try (InputStream inputStream = new FileInputStream(file)) {
			return (T) marshaller.unmarshall(inputStream, deserializationOptions);

		} catch (Exception e) {
			throw new GenericModelException("Reading " + descriptor + " failed.", e);
		}
	}

	@Override
	public void accept(T value) {
		if (!file.exists())
			FileTools.ensureFolderExists(file.getParentFile());

		try (OutputStream outputStream = new FileOutputStream(file)) {
			marshaller.marshall(outputStream, value, serializationOptions);

		} catch (Exception e) {
			throw new GenericModelException("Storing " + descriptor + " failed.", e);
		}
	}

}
