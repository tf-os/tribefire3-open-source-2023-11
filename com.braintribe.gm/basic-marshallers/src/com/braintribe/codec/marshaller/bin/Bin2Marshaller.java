// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.codec.marshaller.bin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.AbstractCharacterMarshaller;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.processing.dataio.GmInputStream;
import com.braintribe.model.processing.dataio.GmOutputStream;
import com.braintribe.utils.Base64;

public class Bin2Marshaller extends AbstractCharacterMarshaller {

	private GmCodec<Object, String> stringCodec;
	private Consumer<Set<String>> requiredTypesReceiver;

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
		return unmarshall(in, GmDeserializationOptions.deriveDefaults().setRequiredTypesReceiver(requiredTypesReceiver).build());
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
			GmOutputStream gmOut = new GmOutputStream(out, options.useDirectPropertyAccess(),
					options.findAttribute(EntityVisitorOption.class).orElse(null));
			gmOut.writeObject(value);
			gmOut.flush();
		} catch (Exception e) {
			throw new MarshallException("error while marshalling value", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {

			if (options.getRequiredTypesReceiver() == null)
				options = options.derive().setRequiredTypesReceiver(requiredTypesReceiver).build();

			@SuppressWarnings("resource")
			GmInputStream gmIn = new GmInputStream(in, options);
			return gmIn.readObject();
		} catch (Exception e) {
			throw new MarshallException("error while unmarshalling value", e);
		}
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		int base64Options = Base64.DONT_BREAK_LINES | Base64.ENCODE;
		if (options.outputPrettiness() != OutputPrettiness.none) {
			base64Options = Base64.ENCODE;
		}

		OutputStream out = new Base64.OutputStream(writer, base64Options);
		marshall(out, value, options);

		try {
			out.flush();
		} catch (IOException e) {
			throw new MarshallException("error while flushing base64 output stream", e);
		}
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		InputStream in = new Base64.InputStream(reader, Base64.DECODE);
		return unmarshall(in, options);
	}

}
