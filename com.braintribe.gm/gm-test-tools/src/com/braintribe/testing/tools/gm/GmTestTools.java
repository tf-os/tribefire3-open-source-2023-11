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
package com.braintribe.testing.tools.gm;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.meta.GmMetaModel;
//import com.braintribe.model.processing.resource.streaming.access.LocalResourceAccessFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.testing.tools.gm.access.TransientNonIncrementalAccess;
import com.braintribe.testing.tools.gm.session.TestModelAccessory;
import com.braintribe.utils.genericmodel.GmTools;

/**
 * This class provides Generic Model related convenience methods that can be used in tests.
 *
 * @author michael.lafite
 */
public abstract class GmTestTools {

	/**
	 * The default id for test accesses.
	 */
	public static String DEFAULT_TESTACCESS_ID = "test.access";

	protected GmTestTools() {
		// no instantiation required
	}

	/**
	 * Creates a new session that operates on the passed <code>access</code>.
	 */
	public static PersistenceGmSession newSession(IncrementalAccess access) {
		BasicPersistenceGmSession session = new BasicPersistenceGmSession(access);

		GmMetaModel model = access.getMetaModel();
		if (model != null)
			session.setModelAccessory(TestModelAccessory.newModelAccessory(model));

		return session;
	}

	/**
	 * Creates a new session that operates on a {@link #newSmoodAccessWithTemporaryFile() SmoodAccess with temporary
	 * file}.
	 */
	public static PersistenceGmSession newSessionWithSmoodAccessAndTemporaryFile() {
		return newSession(newSmoodAccessWithTemporaryFile());
	}

	/**
	 * Similar to {@link #newSessionWithSmoodAccessAndTemporaryFile()}, but also sets up the
	 * {@link BasicPersistenceGmSession#setModelAccessory(com.braintribe.model.processing.session.api.managed.ModelAccessory)
	 * model accessory}.
	 */
	public static PersistenceGmSession newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(GmMetaModel model) {
		BasicPersistenceGmSession session = (BasicPersistenceGmSession) newSessionWithSmoodAccessAndTemporaryFile();
		session.setModelAccessory(TestModelAccessory.newModelAccessory(model));

		return session;
	}

	/**
	 * Creates a new session that operates on a {@link #newSmoodAccessMemoryOnly() memory-only SmoodAccess}.
	 * 
	 * @see #newSmoodAccessMemoryOnly(String, GmMetaModel)
	 */
	public static PersistenceGmSession newSessionWithSmoodAccessMemoryOnly() {
		return newSession(newSmoodAccessMemoryOnly());
	}

	/**
	 * Creates a new session that operates on a {@link #newSmoodAccessMemoryOnly() memory-only SmoodAccess}.
	 */
	public static SmoodAccess newSmoodAccessWithTemporaryFile() {
		return newSmoodAccessWithTemporaryFile(DEFAULT_TESTACCESS_ID);
	}

	/**
	 * Creates a new {@link SmoodAccess} with the specified <code>accessId</code> and <code>delegate</code> access.
	 */
	public static SmoodAccess newSmoodAccess(String accessId, NonIncrementalAccess delegate) {
		SmoodAccess result = GmTools.newSmoodAccess(delegate);
		result.setAccessId(accessId);
		return result;
	}

	/**
	 * Creates a new {@link SmoodAccess} with the specified <code>accessId</code> and <code>file</code>, which is used
	 * to configure an {@link XmlAccess} as delegate access.
	 */
	public static SmoodAccess newSmoodAccess(String accessId, File file) {
		final XmlAccess delegate = new XmlAccess();
		delegate.setFilePath(file);
		return newSmoodAccess(accessId, delegate);
	}

	/**
	 * Creates a new {@link SmoodAccess} with the specified <code>accessId</code>. An {@link XmlAccess} using a
	 * temporary file is configured as delegate access.
	 */
	public static SmoodAccess newSmoodAccessWithTemporaryFile(String accessId) {
		return newSmoodAccess(accessId, TestTools.newTempFile());
	}

	/**
	 * Creates a new {@link SmoodAccess} with the specified <code>file</code>, which is used to configure an
	 * {@link XmlAccess} as delegate access.
	 */
	public static SmoodAccess newSmoodAccess(File file) {
		return newSmoodAccess(DEFAULT_TESTACCESS_ID, file);
	}

	/**
	 * Equivalent to {@code newSmoodAccessMemoryOnly(DEFAULT_TESTACCESS_ID, null)}
	 * 
	 * @see #newSmoodAccessMemoryOnly(String, GmMetaModel)
	 */
	public static SmoodAccess newSmoodAccessMemoryOnly() {
		return newSmoodAccessMemoryOnly(DEFAULT_TESTACCESS_ID, null);
	}

	/**
	 * Creates a new memory-only {@link SmoodAccess} which means the delegate access doesn't persist data to any file.
	 * <p>
	 * 
	 * <h3>PRO TIP:</h3> If you want to assign assign partitions yourself, you have to call
	 * {@code getDatabase().setIgnorePartitions(false)} on the result. Otherwise, the explicit partition assignments are
	 * ignored and induced manipulations are generated, which leads to problems when applying these induced
	 * manipulations)
	 */
	public static SmoodAccess newSmoodAccessMemoryOnly(String accessId, GmMetaModel metaModel) {
		return newSmoodAccess(accessId, new TransientNonIncrementalAccess(metaModel));
	}

	/**
	 * Disables synchronization in the passed <code>smoodAccess</code>. This can be used for single-threaded tests where
	 * performance is important.
	 */
	public static void disableSynchronization(SmoodAccess smoodAccess) {
		smoodAccess.setReadWriteLock(EmptyReadWriteLock.INSTANCE);
	}

	/**
	 * Attaches resources from the specified <code>storageDirectory</code> to the passed <code>session</code>.
	 */
	@SuppressWarnings("unused")
	public static void attachStandaloneResources(BasicPersistenceGmSession session, File storageDirectory) {
//		LocalResourceAccessFactory localAccessFactory = new LocalResourceAccessFactory();
//		localAccessFactory.setResourceBuilder(new StandaloneResourceBuilder());
//		localAccessFactory.setResourceDataUpload(new StandaloneResourceDataUpload(storageDirectory));
//		localAccessFactory.setResourceResolver(new StandaloneSessionBindableResourceResolver(storageDirectory));
//		session.setResourcesAccessFactory(localAccessFactory);
	}

	public static Smood newSmood(GmMetaModel metaModel) throws Exception {
		BasicManagedGmSession session = new BasicManagedGmSession();

		Smood smood = new Smood(session, new ReentrantReadWriteLock());
		smood.setMetaModel(metaModel);

		return smood;
	}
}
