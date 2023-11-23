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
package com.braintribe.model.processing.aspect.crypto.test.commons;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.crypto.configuration.CryptoConfiguration;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingFormat;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class TestDataProvider {
	
	public static Map<String, CryptoConfiguration> configurationMap = new HashMap<>();

	public static Map<String, String> propertyToConfigurationMap = new HashMap<>();
	
	public static Map<String, Map<String, String>> configurationTestInputs = new HashMap<>();

	public static HashingConfiguration defaultHashingConfiguration;
	public static String defaultHashingConfigurationKey = "MD5";

	public static EncryptionConfiguration defaultEncryptionConfiguration;
	public static String defaultEncryptionConfigurationKey = "AES";
	
	public static final String inputAString = "TEST-INPUT";
	public static final String inputBString = "TEST-INPUT.";
	public static final String salt = "SALT";
	public static final Map<String, String> symmetricKeys = new HashMap<>();
	
	static {
		
		// ########################
		// # .. Hashing ......... #
		// ########################

		createHashingConfiguration("MD5");
		createHashingConfiguration("SHA-1");
		createHashingConfiguration("SHA-256");

		defaultHashingConfiguration = (HashingConfiguration) configurationMap.get(defaultHashingConfigurationKey);

		// ##############################
		// # .. Symmetric Encryption .. #
		// ##############################

		symmetricKeys.put("AES", "oAEKRrjj8ZXQsGFX/Upheg==");
		symmetricKeys.put("DES", "5V1GGTibero=");
		symmetricKeys.put("DESede", "RZSuhl6MONXpcwsyH6eK+Ixz1V4TkipA");

		createSymmetricEncryptionConfiguration("AES");
		createSymmetricEncryptionConfiguration("DES");
		createSymmetricEncryptionConfiguration("DESede");

		defaultEncryptionConfiguration = (EncryptionConfiguration) configurationMap.get(defaultEncryptionConfigurationKey);

		
		// ##############################
		// # .. Expected Values ....... #
		// ##############################
		
		configurationTestInputs.put("MD5", new HashMap<String, String>());
		configurationTestInputs.get("MD5").put(inputAString, "LU9Us/7YenN1T3UWqfbXRg==");
		configurationTestInputs.get("MD5").put(inputBString, "ld6bvuBha2EBIwtaXLDxug==");

		configurationTestInputs.put("MD5-SALTED", new HashMap<String, String>());
		configurationTestInputs.get("MD5-SALTED").put(inputAString, "xeOjE7YeVDy+ILml9GVQhw==");
		configurationTestInputs.get("MD5-SALTED").put(inputBString, "J2bDg/cJXKHJpWpj5OAN6g==");

		
		configurationTestInputs.put("SHA-1", new HashMap<String, String>());
		configurationTestInputs.get("SHA-1").put(inputAString, "kubuiSZv/TFetJNfYvXh6siFgpU=");
		configurationTestInputs.get("SHA-1").put(inputBString, "dp6+AsMwoSt7Cyao3tqdbRsmjtg=");
		
		configurationTestInputs.put("SHA-1-SALTED", new HashMap<String, String>());
		configurationTestInputs.get("SHA-1-SALTED").put(inputAString, "FcvdC3WZWk/Xib75d8wESapZskI=");
		configurationTestInputs.get("SHA-1-SALTED").put(inputBString, "Lb4PtW6fargBp3vKWh6cfSiJSJo=");
		
		configurationTestInputs.put("SHA-256", new HashMap<String, String>());
		configurationTestInputs.get("SHA-256").put(inputAString, "Emevvo4O8G0PRLIYXJP4oj5q96dpAFUyaBeNes/mn6k=");
		configurationTestInputs.get("SHA-256").put(inputBString, "RlFnfxmzerCi/WtgI2hpq05sZZcZOqmeLg5EXRngluc=");
		
		configurationTestInputs.put("SHA-256-SALTED", new HashMap<String, String>());
		configurationTestInputs.get("SHA-256-SALTED").put(inputAString, "9ys7rBLNi5Y0GroBv6RLAwWLWZ3KwatEejMVvRKYb70=");
		configurationTestInputs.get("SHA-256-SALTED").put(inputBString, "zhDsQ72AmFozpIHQHmOavqxLlBxWMkeE4cjGta5Opkg=");

		configurationTestInputs.put("AES", new HashMap<String, String>());
		configurationTestInputs.get("AES").put(inputAString, "RZcTyWVBueqGYvp0EX5BPw==");
		configurationTestInputs.get("AES").put(inputBString, "FeGgqB5Opm5en3TQWLAmlA==");
		
		configurationTestInputs.put("DES", new HashMap<String, String>());
		configurationTestInputs.get("DES").put(inputAString, "bLkjK6U5T3utizTA0qYSeQ==");
		configurationTestInputs.get("DES").put(inputBString, "bLkjK6U5T3u9bzgmM222zA==");
		
		configurationTestInputs.put("DESede", new HashMap<String, String>());
		configurationTestInputs.get("DESede").put(inputAString, "aGgrLeaAjaGjfgICEgWBlA==");
		configurationTestInputs.get("DESede").put(inputBString, "aGgrLeaAjaFy1afvZz+ICQ==");
		
	}
	
	public static void savePropertyConfiguration(EntityType<?> type, String propertyName, String cryptoConfigurationKey) {
		propertyToConfigurationMap.put(type.getTypeSignature()+":"+propertyName, cryptoConfigurationKey);
	}
	
	public static String getExpected(EntityType<? extends GenericEntity> type, String propertyName, String cleanValue) {
		
		String cryptoConfigurationKey = propertyToConfigurationMap.get(type.getTypeSignature()+":"+propertyName);
		
		if (cryptoConfigurationKey == null) {
			return cleanValue;
		}
		
		return configurationTestInputs.get(cryptoConfigurationKey).get(cleanValue);
		
	}
	
	private static void createHashingConfiguration(String algorithm) {

		HashingConfiguration config = HashingConfiguration.T.create();
		config.setAlgorithm(algorithm);
		configurationMap.put(algorithm, config);

		config = HashingConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setEnablePublicSalt(true);
		config.setPublicSalt("SALT-FOR-" + algorithm);
		configurationMap.put(algorithm + "-SALTED", config);

		config = HashingConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setEnableRandomSalt(true);
		config.setRandomSaltSize(16);
		configurationMap.put(algorithm + "-RANDOM-SALTED", config);
		
	}
	
	private static void createSymmetricEncryptionConfiguration(String algorithm) {
		
		EncodedSecretKey secretKey = EncodedSecretKey.T.create();
		secretKey.setKeyAlgorithm(algorithm);
		secretKey.setEncodedKey(symmetricKeys.get(algorithm));
		secretKey.setEncodingFormat(KeyEncodingFormat.raw);
		secretKey.setEncodingStringFormat(KeyEncodingStringFormat.base64);

		SymmetricEncryptionConfiguration config = SymmetricEncryptionConfiguration.T.create();
		config.setAlgorithm(algorithm);
		config.setSymmetricEncryptionToken(secretKey);

		configurationMap.put(algorithm, config);

	}
	
}
