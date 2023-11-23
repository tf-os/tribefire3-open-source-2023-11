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
package com.braintribe.model.processing.cryptor.basic.factory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Cryptor.Encoding;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.configuration.CryptoConfiguration;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.crypto.factory.CryptorFactory;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;

/**
 * <p>
 * A {@link CryptorFactory} which caches the {@link Cryptor} it creates.
 * 
 * <p>
 * The cache is maintained based on the contents of the {@link CryptoConfiguration} as given to
 * {@link #newCryptor(CryptoConfiguration)}
 * 
 * <p>
 * If {@link #newCryptor(CryptoConfiguration)} is invoked with a {@link CryptoConfiguration} representing the same
 * configuration properties of a previous call, the {@link Cryptor} created from this previous call is returned.
 * 
 *
 * @param <T>
 *            The type of {@link CryptoConfiguration} from which {@link Cryptor} (s) will be created
 * @param <E>
 *            The type of {@link Cryptor} to be created by the factory
 */
public abstract class AbstractCachingCryptorFactory<T extends CryptoConfiguration, E extends Cryptor> implements CryptorFactory<T, E> {

	private static final String defaultStringCharset = "UTF-8";

	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected Map<Encoding, Codec<byte[], String>> stringCodecs;

	protected Charset stringCharset = Charset.forName(defaultStringCharset);

	private final boolean cacheCryptors = true;

	private final Map<EntityPropertiesKey<T>, E> cachedCryptorsPerInstance = new ConcurrentHashMap<>();

	@Configurable
	public void setTypeReflection(GenericModelTypeReflection typeReflection) {
		this.typeReflection = typeReflection;
	}

	/**
	 * <p>
	 * Sets the string {@link Codec} implementations to be used by the {@link Cryptor}, mapped to their respective
	 * encoding format (e.g.: hex, base64 ..)
	 * 
	 * @param stringCodecs
	 *            The string {@link Codec} implementations to be used by the {@link Cryptor}
	 */
	public void setStringCodecs(Map<Encoding, Codec<byte[], String>> stringCodecs) {
		this.stringCodecs = stringCodecs;
	}

	@Configurable
	public void setStringCharset(String charset) {

		if (charset == null || charset.trim().isEmpty()) {

			if (getLogger().isWarnEnabled()) {
				getLogger().warn("A string charset is required, using default: " + defaultStringCharset);
			}

			charset = defaultStringCharset;
		}

		stringCharset = Charset.forName(charset);

	}

	// ##############################
	// ## .. Abstract Methods .... ##
	// ##############################

	protected abstract E newCryptor(T cryptoConfiguration) throws CryptorFactoryException;

	protected abstract Logger getLogger();

	// ##############################
	// ## .. //Abstract Methods .. ##
	// ##############################

	@Override
	public E getCryptor(T cryptoConfiguration) throws CryptorFactoryException {

		E cryptor = null;

		if (cacheCryptors) {
			cryptor = getCryptorFromCache(cryptoConfiguration);
		} else if (getLogger().isDebugEnabled()) {
			getLogger().debug("Caching has been disabled for this " + this.getClass().getName());
		}

		if (cryptor == null) {
			cryptor = createNewCryptor(cryptoConfiguration);
		}

		if (cryptor == null) {
			throw new CryptorFactoryException("Failed to create a cryptor. null was returned from the underlying factory");
		}

		return cryptor;

	}

	@Override
	public <R extends Cryptor> R getCryptor(Class<R> requiredType, T cryptoConfiguration) throws CryptorFactoryException {

		E cryptor = getCryptor(cryptoConfiguration);

		try {
			return requiredType.cast(cryptor);
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Obtained Cryptor is not compatible with the required type", e);
		}

	}

	protected void validateConfiguration(T cryptoConfiguration) throws CryptorFactoryException {

		if (cryptoConfiguration.getAlgorithm() == null || cryptoConfiguration.getAlgorithm().isEmpty()) {
			throw new CryptorFactoryException("Provided " + cryptoConfiguration.getClass().getSimpleName() + " contains no algorithm.");
		}

	}

	protected E createNewCryptor(T cryptoConfiguration) throws CryptorFactoryException {

		E cryptor = null;

		long s = 0;
		if (getLogger().isDebugEnabled()) {
			s = System.currentTimeMillis();
		}

		cryptor = newCryptor(cryptoConfiguration);

		if (getLogger().isDebugEnabled() && cryptor != null) {
			s = System.currentTimeMillis() - s;
			getLogger().debug("Created " + cryptor.getClass().getSimpleName() + " using " + this.getClass().getSimpleName() + " in " + s + " ms ");
		}

		return cryptor;

	}

	protected E getCryptorFromCache(T cryptoConfiguration) throws CryptorFactoryException {

		if (!cacheCryptors) {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("Cachinng has been disabled for this " + this.getClass().getName());
			}
			return null;
		}

		EntityType<T> entityType = cryptoConfiguration.entityType();

		EntityPropertiesKey<T> cachedCryptorKey = new EntityPropertiesKey<>(entityType, cryptoConfiguration);

		E cryptor = cachedCryptorsPerInstance.get(cachedCryptorKey);

		if (cryptor == null) {
			removePrevious(cachedCryptorKey);
			cryptor = createNewCryptor(cryptoConfiguration);
			cachedCryptorsPerInstance.put(cachedCryptorKey, cryptor);
		}

		return cryptor;

	}

	/**
	 * <p>
	 * Removes from {@link #cachedCryptorsPerInstance} the possible entries for which the entity reference are the same,
	 * but their values have changed.
	 * 
	 * @param entityPropertiesKey
	 *            The key to be removed
	 * @throws CryptorFactoryException
	 *             If the removal fails
	 */
	private void removePrevious(EntityPropertiesKey<T> entityPropertiesKey) throws CryptorFactoryException {

		try {
			Set<Map.Entry<EntityPropertiesKey<T>, E>> cryptorEntrySet = cachedCryptorsPerInstance.entrySet();

			if (cryptorEntrySet.isEmpty()) {
				return;
			}

			Iterator<Entry<EntityPropertiesKey<T>, E>> iterator = cryptorEntrySet.iterator();

			while (iterator.hasNext()) {
				Map.Entry<EntityPropertiesKey<T>, E> cryptorEntry = iterator.next();
				if (entityPropertiesKey.sameOrigin(cryptorEntry.getKey())) {
					iterator.remove();
				}
			}
		} catch (Exception e) {
			throw CryptorFactoryException.wrap("Failed to remove previous entry from cache", e);
		}

	}

	static class EntityPropertiesKey<T extends GenericEntity> {

		private final int entityPropertiesChecksum;
		private final EntityReference entityReference;

		EntityPropertiesKey(EntityType<T> entityType, T entity) {

			if (entity == null) {
				throw new IllegalArgumentException("Cannot create a " + EntityPropertiesKey.class.getName() + " with a null entity");
			}

			entityReference = entity.reference();
			entityPropertiesChecksum = hashCodeReference(entityType, entity);
		}

		@Override
		public int hashCode() {
			return entityPropertiesChecksum;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && (obj instanceof EntityPropertiesKey) ? entityPropertiesChecksum == obj.hashCode() : false;
		}

		int hashCodeReference(EntityType<T> entityType, T entity) {
			return EntityValuesHashProvider.<T> provideFor(entityType, entity);
		}

		boolean sameOrigin(EntityPropertiesKey<T> otherEntityPropertiesKey) {
			return hasSameIdentity(entityReference, otherEntityPropertiesKey.entityReference);
		}

		boolean hasSameIdentity(EntityReference propertyCryptingRef, EntityReference otherPropertyCryptingRef) {
			return EntRefHashingComparator.INSTANCE.compare(propertyCryptingRef, otherPropertyCryptingRef);
		}

	}

	static class EntityValuesHashProvider {

		static final Logger log = Logger.getLogger(EntityValuesHashProvider.class);

		static <T extends GenericEntity> int provideFor(EntityType<T> entityType, T entity) {

			long t = 0;
			if (log.isTraceEnabled()) {
				t = System.currentTimeMillis();
			}

			final int prime = 31;
			int result = 1;

			result = prime * result + entityType.getTypeSignature().hashCode();

			for (Property property : entityType.getProperties()) {

				Object propertyValue = entityType.getProperty(property.getName()).get(entity);

				if (propertyValue != null) {
					result = prime * result + propertyValue.hashCode();
				}

			}

			if (log.isTraceEnabled()) {
				t = System.currentTimeMillis() - t;
				log.trace("Hash " + result + " calculated for " + entity + " in " + t + " ms");
			}

			return result;

		}

	}

}
