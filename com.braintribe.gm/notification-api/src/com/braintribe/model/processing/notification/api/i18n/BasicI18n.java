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
package com.braintribe.model.processing.notification.api.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.i18n.I18nBundle;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

public class BasicI18n implements I18n, InitializationAware {

	private Supplier<PersistenceGmSession> sessionProvider;
	private Supplier<String> currentLocaleProvider = () -> null;

	private Map<String, Map<EntityType<? extends I18nBundle>, I18nBundle>> cache = new HashMap<>();

	// ***************************************************************************************************
	// Configuration
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Configurable
	public void setCurrentLocaleProvider(Supplier<String> localeProvider) {
		this.currentLocaleProvider = localeProvider;
	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	@Override
	public void postConstruct() {
		loadBundles();
	}

	// ***************************************************************************************************
	// I18n
	// ***************************************************************************************************

	@Override
	public <T extends I18nBundle> T get(EntityType<T> bundleType) {
		return get(bundleType, this.currentLocaleProvider.get());
	}

	@Override
	public <T extends I18nBundle> T get(EntityType<T> bundleType, String locale) {
		if (locale == null) {
			locale = LocalizedString.LOCALE_DEFAULT;
		}
		T bundle = find(bundleType, locale);
		if (bundle == null) {
			bundle = createDefault(bundleType, locale);
		}
		return bundle;
	}

	// ***************************************************************************************************
	// Helpers
	// ***************************************************************************************************

	private <T extends I18nBundle> T find(EntityType<T> bundleType, String locale) {
		Map<EntityType<? extends I18nBundle>, I18nBundle> localeCache = this.cache.get(locale);
		return (localeCache != null) ? (T) localeCache.get(bundleType) : null;
	}
	
	private <T extends I18nBundle> T createDefault(EntityType<T> bundleType, String locale) {
		T bundle = bundleType.create(); // Default bundle with initial values
		bundle.setLocale(locale);
		return bundle;
	}

	private void loadBundles() {
		PersistenceGmSession session = this.sessionProvider.get();

		//@formatter:off
		EntityQuery bundleQuery = 
				EntityQueryBuilder
					.from(I18nBundle.T)
					.done();

		List<I18nBundle> bundles = 
			session
			.query()
			.entities(bundleQuery)
			.list();

		bundles
			.stream()
			.forEach(this::cacheBundle);
		//@formatter:on
	}

	private void cacheBundle(I18nBundle b) {
		//@formatter:off
		this.cache
		.computeIfAbsent(b.getLocale(), k -> new HashMap<>())
		.put(b.entityType(), b);
		//@formatter:on
	}

}
