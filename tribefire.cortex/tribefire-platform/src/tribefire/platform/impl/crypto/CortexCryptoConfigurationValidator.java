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
package tribefire.platform.impl.crypto;

import java.util.UUID;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.crypto.BidiCryptor;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.logging.Logger;

public class CortexCryptoConfigurationValidator implements InitializationAware {

	private static final Logger log = Logger.getLogger(CortexCryptoConfigurationValidator.class);

	private boolean runOnStartUp = true;
	private Encryptor cortexHasher;
	private BidiCryptor cortexSymmetricCryptor;
	private BidiCryptor cortexAsymmetricCryptor;

	@Configurable
	public void setRunOnStartUp(boolean runOnStartUp) {
		this.runOnStartUp = runOnStartUp;
	}

	@Configurable
	public void setCortexHasher(Encryptor cortexHasher) {
		this.cortexHasher = cortexHasher;
	}

	@Configurable
	public void setCortexSymmetricCryptor(BidiCryptor cortexSymmetricCryptor) {
		this.cortexSymmetricCryptor = cortexSymmetricCryptor;
	}

	public void setCortexAsymmetricCryptor(BidiCryptor cortexAsymmetricCryptor) {
		this.cortexAsymmetricCryptor = cortexAsymmetricCryptor;
	}

	@Override
	public void postConstruct() {
		if (!runOnStartUp) {
			return;
		}
		validateConfiguration();
	}

	public void validateConfiguration() throws CortexCryptoConfiguratorException {

		byte[] validationData = UUID.randomUUID().toString().getBytes();

		if (cortexHasher != null) {
			validateCryptor("Cortex hasher", cortexHasher, validationData);
		}

		if (cortexSymmetricCryptor != null) {
			validateCryptor("Cortex symmetric key-based cryptor", cortexSymmetricCryptor, validationData);
		}

		if (cortexAsymmetricCryptor != null) {
			validateCryptor("Cortex asymmetric key-based cryptor", cortexAsymmetricCryptor, validationData);
		}

	}

	protected static void validateCryptor(String context, Cryptor cryptor, byte[] validationData) throws CortexCryptoConfiguratorException {

		testCryptor(context, cryptor, validationData);

		if (log.isDebugEnabled()) {
			log.debug(context + " is valid");
		}

	}

	protected static void testCryptor(String context, Cryptor cryptor, byte[] validationData) throws CortexCryptoConfiguratorException {
		if (cryptor instanceof BidiCryptor) {
			testBidiCryptor(context, (BidiCryptor) cryptor, validationData);
		} else if (cryptor instanceof Encryptor) {
			testEncryptor(context, (Encryptor) cryptor, validationData);
		}
	}

	protected static byte[] testEncryptor(String context, Encryptor encryptor, byte[] validationData) throws CortexCryptoConfiguratorException {

		try {
			byte[] encrypted = encryptor.encrypt(validationData).result().asBytes();

			if (log.isTraceEnabled()) {
				log.trace(context + " encrypted successfully");
			}

			return encrypted;

		} catch (Exception e) {
			throw new CortexCryptoConfiguratorException(context + " failed to encrypt: " + e.getMessage(), e);
		}

	}

	protected static byte[] testBidiCryptor(String context, BidiCryptor cryptor, byte[] validationData) throws CortexCryptoConfiguratorException {

		byte[] encrypted = testEncryptor(context, cryptor, validationData);

		try {
			byte[] decrypted = cryptor.decrypt(encrypted).result().asBytes();

			if (log.isTraceEnabled()) {
				log.trace(context + " decrypted successfully");
			}

			return decrypted;

		} catch (Exception e) {
			throw new CortexCryptoConfiguratorException(context + " failed to decrypt: " + e.getMessage(), e);
		}

	}

}
