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
package tribefire.platform.impl.security;

import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.genericmodel.PropertyTransformer;
import com.braintribe.utils.genericmodel.PropertyTransformer.NameMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.PropertyMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.TypeMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.ValueMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.ValueTransformer;

/**
 * This utility leverages the {@link PropertyTransformer} utility to traverse a given entity and decrypt the values of
 * properties identified by a {@link PropertyMatcher}. The decryption is done by leveraging encryption/decryption
 * utility methods utilized in other places (e.g., Jinni). Note, that only properties of type string are inspected for
 * obfuscation.
 */
public class EntityDecryption {

	private static TypeMatcher<Object> stringMatcher = new TypeMatcher<Object>(GenericModelTypeReflection.TYPE_STRING);
	private static ValueMatcher<Object> nullMatcher = new ValueMatcher<Object>(null);
	private static NameMatcher<String> passwordNameMatcher = new NameMatcher<String>("password");

	private static String secret = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_TRIBEFIRE_DECRYPT_SECRET);

	/**
	 * Encrypts all properties in the entity assembly with the name "password". <br>
	 * Same as calling:<br>
	 * <code>
	 * obfuscateProperties(entity, new NameMatcher<String>("password"));
	 * </code>
	 */
	public static void encryptProperties(GenericEntity entity) {
		encryptProperties(entity, passwordNameMatcher);
	}

	/**
	 * Deobfuscates all properties in the entity assembly identified by the {@link DecryptionPrefixMatcher}. <br>
	 * Same as calling:<br>
	 * <code>
	 * deobfuscateProperties(entity, new ObfuscationPrefixMatcher())
	 * </code>
	 * 
	 */
	public static void decryptProperties(GenericEntity entity) {
		decryptProperties(entity, new DecryptionPrefixMatcher(), rawInput -> {
			if (rawInput == null) {
				return null;
			}
			if (rawInput.startsWith(Cryptor.DECRYPTION_PREFIX)) {
				rawInput = rawInput.substring(Cryptor.DECRYPTION_PREFIX.length());
			}
			return Cryptor.decrypt(secret, null, null, null, rawInput);
		});
		decryptProperties(entity, new DecryptionMethodMatcher(), rawInput -> {
			if (rawInput == null) {
				return null;
			}
			if ((rawInput.startsWith("${decrypt('") && rawInput.endsWith("')}"))
					|| (rawInput.startsWith("${decrypt(\"") && rawInput.endsWith("\")}"))) {
				String quote = rawInput.startsWith("${decrypt('") ? "'" : "\"";

				int idx1 = rawInput.indexOf(quote);
				int idx2 = rawInput.lastIndexOf(quote);
				if (idx1 > 0 && idx2 > idx1) {
					rawInput = rawInput.substring(("${decrypt(" + quote).length(), rawInput.length() - 3);
					return Cryptor.decrypt(secret, null, null, null, rawInput);
				}
			}
			return rawInput;
		});
	}

	/**
	 * Obfuscates all properties of the passed assembly that have {@link Confidential} configured. <br>
	 * Same as calling:<br>
	 * <code>
	 * obfuscateProperties(GenericEntity, new PasswordMatcher(session)
	 * </code>
	 */
	public static void encryptProperties(GenericEntity entity, PersistenceGmSession session) {
		encryptProperties(entity, new PasswordMatcher(session));
	}

	/**
	 * Obfuscats all properties of the passed assembly matching following rules:
	 * <ul>
	 * <li>are of type string</li>
	 * <li>have a non null value</li>
	 * <li>matches the passed matcher</li>
	 * </ul>
	 * 
	 */
	public static void encryptProperties(GenericEntity entity, final PropertyMatcher<String> matcher) {
		PropertyTransformer.transformProperties(entity, new PropertyMatcher<Object>() {
			@Override
			public boolean matches(GenericEntity matchEntity, Property property, Object propertyValue, EntityType<GenericEntity> type) {
				return (stringMatcher.matches(matchEntity, property, propertyValue, type)
						&& !(nullMatcher.matches(matchEntity, property, propertyValue, type))
						&& matcher.matches(matchEntity, property, (String) propertyValue, type));
			}
		}, new ValueTransformer<Object, Object>() {
			@Override
			public String transform(Object value) {
				return Cryptor.encrypt(secret, null, null, null, (String) value);
			}
		});
	}

	/**
	 * Deobfuscats all properties of the passed assembly matching following rules:
	 * <ul>
	 * <li>are of type string</li>
	 * <li>have a non null value</li>
	 * <li>matches the passed matcher</li>
	 * </ul>
	 * 
	 */
	public static void decryptProperties(GenericEntity entity, final PropertyMatcher<String> matcher, Function<String, String> decryption) {

		PropertyTransformer.transformProperties(entity, new PropertyMatcher<Object>() {
			@Override
			public boolean matches(GenericEntity matchEntity, Property property, Object propertyValue, EntityType<GenericEntity> type) {
				return (stringMatcher.matches(matchEntity, property, propertyValue, type)
						&& !(nullMatcher.matches(matchEntity, property, propertyValue, type))
						&& matcher.matches(matchEntity, property, (String) propertyValue, type));
			}
		}, new ValueTransformer<Object, Object>() {
			@Override
			public Object transform(Object value) {
				return decryption.apply((String) value);
			}
		});
	}

	/**
	 * Matches the property by comparing the value of the property with the {@link Cryptor#DECRYPTION_PREFIX}.
	 */
	public static class DecryptionPrefixMatcher implements PropertyMatcher<String> {

		private String prefix = Cryptor.DECRYPTION_PREFIX;

		public DecryptionPrefixMatcher() {
		}

		public DecryptionPrefixMatcher(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, String propertyValue, EntityType<GenericEntity> type) {
			return propertyValue != null && propertyValue.startsWith(prefix);
		}
	}

	public static class DecryptionMethodMatcher implements PropertyMatcher<String> {

		public DecryptionMethodMatcher() {
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, String propertyValue, EntityType<GenericEntity> type) {

			if (propertyValue != null) {
				if ((propertyValue.startsWith("${decrypt('") && propertyValue.endsWith("')}"))
						|| (propertyValue.startsWith("${decrypt(\"") && propertyValue.endsWith("\")}"))) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Matches the property by inspecting the {@link Confidential} meta data.
	 */
	public static class PasswordMatcher implements PropertyMatcher<String> {

		PersistenceGmSession session;

		public PasswordMatcher(PersistenceGmSession session) {
			this.session = session;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, String propertyValue, EntityType<GenericEntity> type) {
			return session.getModelAccessory().getMetaData().entity(entity).property(property).is(Confidential.T);
		}
	}

}
