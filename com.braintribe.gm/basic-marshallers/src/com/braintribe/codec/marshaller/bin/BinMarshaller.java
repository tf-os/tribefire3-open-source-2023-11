// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.codec.marshaller.bin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.processing.dataio.GenericModelInputStream;
import com.braintribe.model.processing.dataio.GenericModelOutputStream;

public class BinMarshaller implements Marshaller, HasStringCodec {

	private GmCodec<Object, String> stringCodec;
	private Consumer<Set<String>> requiredTypesReceiver;
	private boolean writeRequiredTypes = false;

	@Configurable
	public void setWriteRequiredTypes(boolean writeRequiredTypes) {
		this.writeRequiredTypes = writeRequiredTypes;
	}

	@Configurable
	public void setStringCodec(GmCodec<Object, String> stringCodec) {
		this.stringCodec = stringCodec;
	}

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public GmCodec<Object, String> getStringCodec() {
		if (stringCodec == null)
			stringCodec = getDefaultStringCodec();
		return stringCodec;
	}

	protected GmCodec<Object, String> getDefaultStringCodec() {

		return new GmCodec<Object, String>() {

			@Override
			public String encode(Object value) throws CodecException {
				return encode(value, GmSerializationOptions.deriveDefaults().build());
			}

			@Override
			public Object decode(String encodedValue) throws CodecException {
				return decode(encodedValue, GmDeserializationOptions.deriveDefaults().build());
			}

			@Override
			public Class<Object> getValueClass() {
				return Object.class;
			}

			@Override
			public Object decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
				try {
					return unmarshall(new ByteArrayInputStream(encodedValue.getBytes("UTF-8")), options);
				} catch (Exception e) {
					throw new CodecException(e);
				}
			}

			@Override
			public String encode(Object value, GmSerializationOptions options) throws CodecException {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					marshall(baos, value, options);
					return baos.toString("UTF-8");
				} catch (Exception e) {
					throw new CodecException(e);
				}
			}

		};

	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			@SuppressWarnings("resource")
			GenericModelOutputStream gmOut = new GenericModelOutputStream(out, writeRequiredTypes);
			gmOut.writeObject(value);
			gmOut.flush();
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			@SuppressWarnings("resource")
			GenericModelInputStream gmOut = new GenericModelInputStream(in, requiredTypesReceiver, options.getAbsentifyMissingProperties());
			return gmOut.readObject();
		} catch (Exception e) {
			throw new MarshallException("error while unmarshalling value", e);
		}
	}

}
