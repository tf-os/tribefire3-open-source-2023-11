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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.Obfuscation;
import com.braintribe.utils.genericmodel.PropertyTransformer;
import com.braintribe.utils.genericmodel.PropertyTransformer.NameMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.PropertyMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.TypeMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.ValueMatcher;
import com.braintribe.utils.genericmodel.PropertyTransformer.ValueTransformer;

/**
 * This utility leverages the {@link PropertyTransformer} utility to traverse a
 * given entity and obfuscate the values of properties identified by a
 * {@link PropertyMatcher}. The obfuscation is done by leveraging
 * {@link Obfuscation} utility methods. Note, that only properties of type
 * string are inspected for obfuscation.
 */
public class EntityObfuscation {

	private static TypeMatcher<Object> stringMatcher = new TypeMatcher<Object>(GenericModelTypeReflection.TYPE_STRING);
	private static ValueMatcher<Object> nullMatcher = new ValueMatcher<Object>(null);
	private static NameMatcher<String> passwordNameMatcher = new NameMatcher<String>("password");

	/**
	 * Obfuscates all properties in the entity assembly with the name "password".
	 * <br>Same as calling:<br> 
	 * <code>
	 * obfuscateProperties(entity, new NameMatcher<String>("password"));
	 * </code>
	 */
	public static void obfuscateProperties(GenericEntity entity) {
		obfuscateProperties(entity, passwordNameMatcher);
	}

	/**
	 * Deobfuscates all properties in the entity assembly identified by the
	 * {@link ObfuscationPrefixMatcher}.
	 * <br>Same as calling:<br> 
	 * <code>
	 * deobfuscateProperties(entity, new ObfuscationPrefixMatcher())
	 * </code>
	 * 
	 */
	public static void deobfuscateProperties(GenericEntity entity) {
		deobfuscateProperties(entity, new ObfuscationPrefixMatcher());
	}
	
	/**
	 * Obfuscates all properties of the passed assembly that have {@link Confidential} configured.
	 * <br>Same as calling:<br> 
	 * <code>
	 * obfuscateProperties(GenericEntity, new PasswordMatcher(session)
	 * </code>
	 */
	public static void obfuscateProperties(GenericEntity entity, PersistenceGmSession session) {
		obfuscateProperties(entity, new PasswordMatcher(session));
	}

	/**
	 * Deobfuscates all properties of the passed assembly that have {@link Confidential} configured. 
	 * <br>Same as calling:<br> 
	 * <code>
	 * deobfuscateProperties(GenericEntity, new PasswordMatcher(session)
	 * </code>
	 */
	public static void deobfuscateProperties(GenericEntity entity, PersistenceGmSession session) {
		deobfuscateProperties(entity, new PasswordMatcher(session));
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
	public static void obfuscateProperties(GenericEntity entity, final PropertyMatcher<String> matcher) {
		PropertyTransformer.transformProperties(entity, new PropertyMatcher<Object>() {
			@Override
			public boolean matches(GenericEntity matchEntity, Property property, Object propertyValue,
					EntityType<GenericEntity> type) {
				return (stringMatcher.matches(matchEntity, property, propertyValue, type)
						&& !(nullMatcher.matches(matchEntity, property, propertyValue, type))
						&& matcher.matches(matchEntity, property, (String) propertyValue, type));
			}
		}, new ValueTransformer<Object, Object>() {
			@Override
			public String transform(Object value) {
				return Obfuscation.obfuscate((String) value);
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
	public static void deobfuscateProperties(GenericEntity entity, final PropertyMatcher<String> matcher) {
		PropertyTransformer.transformProperties(entity, new PropertyMatcher<Object>() {
			@Override
			public boolean matches(GenericEntity matchEntity, Property property, Object propertyValue,
					EntityType<GenericEntity> type) {
				return (stringMatcher.matches(matchEntity, property, propertyValue, type)
						&& !(nullMatcher.matches(matchEntity, property, propertyValue, type))
						&& matcher.matches(matchEntity, property, (String) propertyValue, type));
			}
		}, new ValueTransformer<Object, Object>() {
			@Override
			public Object transform(Object value) {
				return Obfuscation.deobfuscate((String) value);
			}
		});
	}


	/**
	 * Matches the property by comparing the value of the property with the {@link Obfuscation#OBFUSCATION_PREFIX}. 
	 */
	public static class ObfuscationPrefixMatcher implements PropertyMatcher<String> {

		private String prefix = Obfuscation.OBFUSCATION_PREFIX;

		public ObfuscationPrefixMatcher() {
		}

		public ObfuscationPrefixMatcher(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public boolean matches(GenericEntity entity, Property property, String propertyValue,
				EntityType<GenericEntity> type) {
			return Obfuscation.isObfuscated(propertyValue, prefix);
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
