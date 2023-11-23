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
package com.braintribe.model.processing.itw.synthesis.java.jar;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.DebugInfoProvider;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.utils.CommonTools;

/**
 * 
 */
public class JavaTypeSerializer {

	private final ProtoGmMetaModel metaModel;
	private final ZipOutputStream zos;
	private final DebugInfoProvider debugInfoProvider;
	private final JavaTypeSynthesis jts;
	private boolean hash;
	private MessageDigest digest;

	/**
	 * Stores java binaries for given {@link ProtoGmMetaModel} into given {@link ZipOutputStream}. Optionally, it also
	 * generates debug information for entities.
	 * 
	 * @param metaModel
	 *            Source of entities and enums to be serialized. Note that not all types are serialized, but only those
	 *            where artifact binding is <tt>null</tt> or where it represents the same artifact as what the
	 *            meta-model name suggests.
	 * @param zos
	 *            Stream to write the output to. The stream is not closed by this class and has to be done by the
	 *            caller.
	 * @param entitySources
	 *            Maps entity type signature to it's corresponding source-code. (No debug info for enums.) May be
	 *            <tt>null</tt>.
	 * 
	 * @throws JavaTypeSynthesisException
	 *             If either a problem occurs during synthesis or while writing to the output stream.
	 */
	public static void serialize(ProtoGmMetaModel metaModel, ZipOutputStream zos, Map<String, String> entitySources) throws JavaTypeSynthesisException {
		new JavaTypeSerializer(metaModel, zos, entitySources).serialize();
	}

	public static String serializeAndGenerateHash(ProtoGmMetaModel metaModel, ZipOutputStream zos, Map<String, String> entitySources)
			throws JavaTypeSynthesisException {

		JavaTypeSerializer javaTypeSerializer = new JavaTypeSerializer(metaModel, zos, entitySources);
		javaTypeSerializer.hash = true;
		return javaTypeSerializer.serialize();
	}

	private JavaTypeSerializer(ProtoGmMetaModel metaModel, ZipOutputStream zos, Map<String, String> entitySources) {
		this.metaModel = metaModel;
		this.zos = zos;
		this.debugInfoProvider = new SimpleDebugInfoProvider(nullSafe(entitySources));
		this.jts = new JavaTypeSynthesis(debugInfoProvider, false);
	}

	/** @return hash over the class files sorted by type signature (if hashing enabled), null otherwise */
	private String serialize() throws JavaTypeSynthesisException {
		Collection<? extends ProtoGmType> types = nullSafe(metaModel.getTypes());

		if (hash) {
			prepareDigest();
			types = sortTypes(types);
		}

		for (ProtoGmType type : types)
			if (type.isGmCustom())
				serializeProtoGmType(type);

		return hash ? finalizeDigest() : null;
	}

	private void serializeProtoGmType(ProtoGmType gmType) throws JavaTypeSynthesisException {
		AsmNewClass newClass = (AsmNewClass) jts.ensureClass(gmType);
		writeClass(newClass);
	}

	private void writeClass(AsmNewClass newClazz) throws JavaTypeSynthesisException {
		// for whatever reason the first one does not work on windows, even if the files look identical after extracting
		// String entryName = newClazz.getName().replace(".", File.separator) + ".class";
		String entryName = newClazz.getName().replace(".", "/") + ".class";

		try {
			zos.putNextEntry(new ZipEntry(entryName));

			byte[] bytes = newClazz.getBytes();

			if (hash)
				digest.update(bytes);

			zos.write(bytes);

		} catch (IOException e) {
			throw new JavaTypeSynthesisException("Error while serializing generated class.", e);
		}
	}

	// #########################################
	// ## . . . . . . . . MD5 . . . . . . . . ##
	// #########################################

	private void prepareDigest() throws JavaTypeSynthesisException {
		try {
			digest = MessageDigest.getInstance("MD5");

		} catch (NoSuchAlgorithmException e) {
			throw new JavaTypeSynthesisException("error while creating MessageDigest for MD5", e);
		}
	}

	private String finalizeDigest() {
		byte[] hashBytes = digest.digest();
		return CommonTools.printHexBinary(hashBytes).toLowerCase();
	}

	private List<ProtoGmType> sortTypes(Collection<? extends ProtoGmType> types) {
		List<ProtoGmType> result = newList(types);
		result.sort(Comparator.comparing(ProtoGmType::getTypeSignature));

		return result;
	}

}
