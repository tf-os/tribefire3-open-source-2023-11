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
package com.braintribe.utils.genericmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.common.lcd.GmException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.provider.Holder;
import com.braintribe.utils.CollectionTools;

/**
 * This class provides Generic Model related utility methods. In addition to the methods provided by {@link GMCoreTools}
 * this class provides methods dependent on other than just the core artifacts.
 *
 * @author michael.lafite
 */
public class GmTools extends GMCoreTools {

	/**
	 * @see GMCoreTools#getJavaPackageNameFromEntityTypeSignature(String)
	 */
	public static String getJavaPackageName(final GmEntityType entityType) {
		return GMCoreTools.getJavaPackageNameFromEntityTypeSignature(entityType.getTypeSignature());
	}

	/**
	 * @see GMCoreTools#getSimpleEntityTypeNameFromTypeSignature(String)
	 */
	public static String getSimpleEntityTypeName(final GmEntityType entityType) {
		return GMCoreTools.getSimpleEntityTypeNameFromTypeSignature(entityType.getTypeSignature());
	}

	/**
	 * @see GMCoreTools#getSimpleEnumTypeNameFromTypeSignature(String)
	 */
	public static String getSimpleEnumTypeName(final GmEnumType enumType) {
		return GMCoreTools.getSimpleEnumTypeNameFromTypeSignature(enumType.getTypeSignature());
	}

	/**
	 * Returns the {@link GmType#getTypeSignature() type signatures} of the passed <code>types</code>.
	 */
	public static List<String> getTypeSignatures(final Collection<? extends GmType> types) {
		final List<String> result = new ArrayList<>();
		for (final GmType type : CollectionTools.thisOrEmpty(types)) {
			result.add(type.getTypeSignature());
		}
		return result;
	}

	/**
	 * Creates a new {@link PersistenceGmSession} which uses the passed <code>access</code>.
	 */
	public static PersistenceGmSession newSession(final IncrementalAccess access) {
		final BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(access);
		return session;
	}

	/**
	 * Creates a {@link PersistenceGmSession} which uses a {@link #newSmoodAccess(File) new SmoodAccess}.
	 */
	public static PersistenceGmSession newSessionWithSmoodAccess(final File xmlFile) {
		final BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(newSmoodAccess(xmlFile));
		return session;
	}

	/**
	 * Creates a {@link #newSmoodAccess(NonIncrementalAccess) new SmoodAccess} with a {@link #newXmlAccess(File) new
	 * XmlAccess} as delegate.
	 */
	public static SmoodAccess newSmoodAccess(final File xmlFile) {
		return newSmoodAccess(newXmlAccess(xmlFile));
	}

	/**
	 * Returns a new {@link SmoodAccess} which uses the passed <code>delegate</code>.
	 */
	public static SmoodAccess newSmoodAccess(final NonIncrementalAccess delegate) {
		return newSmoodAccess("[ModelNameNotSpecified]", delegate);
	}

	/**
	 * Returns a new {@link SmoodAccess} which uses the passed <code>delegate</code>. Sets the
	 * {@link SmoodAccess#setModelName(String) model name} to the specified <code>modelName</code>.
	 */
	protected static SmoodAccess newSmoodAccess(final String modelName, final NonIncrementalAccess delegate) {
		final SmoodAccess smoodAccess = new SmoodAccess();
		smoodAccess.setModelName(modelName);
		smoodAccess.setDataDelegate(delegate);
		smoodAccess.setReadWriteLock(new MutuallyExclusiveReadWriteLock());
		return smoodAccess;
	}

	/**
	 * Returns a new {@link XmlAccess} which writes to the specified <code>xmlFile</code>.
	 */
	public static XmlAccess newXmlAccess(final File xmlFile) {
		return newXmlAccess(xmlFile, null);
	}

	public static XmlAccess newXmlAccess(final File xmlFile, GmMetaModel metaModel) {
		final XmlAccess xmlAccess = new XmlAccess();
		xmlAccess.setFilePath(xmlFile);
		xmlAccess.setModelProvider(new Holder<GmMetaModel>(metaModel));

		return xmlAccess;
	}

	/**
	 * {@link #decodeLenientlyAndReturnSmoodAccess(File) Leniently decodes} the passed XML and then returns a
	 * {@link SmoodAccess} based session through which the data can be accessed.
	 *
	 * @see #decodeLenientlyAndReturnSmoodAccess(File)
	 */
	public static PersistenceGmSession decodeLeniently(final File xmlFile) {
		SmoodAccess smoodAccess = decodeLenientlyAndReturnSmoodAccess(xmlFile);
		PersistenceGmSession session = newSession(smoodAccess);
		return session;
	}

	/**
	 * Uses an {@link XmlAccess} to {@link GenericModelRootDomCodec#getDecodingLenience() leniently decode} the specified
	 * <code>xmlFile</code> and then returns a {@link SmoodAccess} with the <code>XmlAccess</code> as delegate.
	 *
	 * @see #decodeLeniently(File)
	 */
	public static SmoodAccess decodeLenientlyAndReturnSmoodAccess(final File xmlFile) {
		XmlAccess xmlAccess = newXmlAccess(xmlFile);
		xmlAccess.setDeserializationOptions(GmDeserializationOptions.deriveDefaults().setDecodingLenience(new DecodingLenience(true)).build());
		return newSmoodAccess(xmlAccess);
	}

	/**
	 * Ensures the model types for all {@link GmMetaModel}s in the specified <code>xmlFile</code>. Just invokes
	 * {@link #ensureModelTypes(File, String)} with no model name specified.
	 */
	public static void ensureModelTypes(final File xmlFile) {
		ensureModelTypes(xmlFile, null);
	}

	/**
	 * {@link #decodeLeniently(File) Leniently decodes} the specified <code>xmlFile</code> and passes the session to
	 * {@link #ensureModelTypes(PersistenceGmSession, String)}.
	 */
	public static void ensureModelTypes(final File xmlFile, String metaModelName) {
		PersistenceGmSession session = decodeLeniently(xmlFile);
		ensureModelTypes(session, metaModelName);
	}

	/**
	 * {@link GenericModelTypeSynthesis#ensureModelTypes(GmMetaModel) Ensures the model types} for the {@link GmMetaModel}
	 * instance with the specified <code>modelName</code>.
	 *
	 * @param metaModelName
	 *            the name of the {@link GmMetaModel} for which to ensure the model types. The name may contain wildcards.
	 *            If <code>null</code>, all models will be processed.
	 */
	public static void ensureModelTypes(final PersistenceGmSession session, String metaModelName) {
		TraversingCriterion noMetadataTc = TC.create().typeCondition(TypeConditions.isAssignableTo(MetaData.T)).done();

		EntityQuery query = null;
		if (metaModelName == null) {
			query = EntityQueryBuilder.from(GmMetaModel.class).tc(noMetadataTc).done();
		} else {
			query = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").like(metaModelName).tc(noMetadataTc).done();
		}

		List<GmMetaModel> models;
		try {
			models = session.query().entities(query).list();
		} catch (GmSessionException e) {
			throw new GmException("Unexpected error while querying for meta models.", e);
		}

		for (GmMetaModel model : models) {
			model.deploy();
		}
	}

}
