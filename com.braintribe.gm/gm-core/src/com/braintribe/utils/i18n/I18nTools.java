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
package com.braintribe.utils.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.session.GmSession;


public class I18nTools {

	public static final String defaultLocale = "default";
	
	public static Supplier<String> localeProvider = new Supplier<String>() {
		@Override
		public String get() throws RuntimeException {
			return defaultLocale;
		}
	};

	public static String getLocalized(LocalizedString s) {
		if (localeProvider != null) {
			try {
				return I18nTools.get(s, localeProvider.get());
			} catch (RuntimeException e) {
				throw new RuntimeException("error while asking provider for current locale", e);
			}
		}
		else {
			return I18nTools.getDefault(s, null);
		}
	}

	public static String get(LocalizedString s, String locale) {
		if (s == null) {
			return null;
		}
		
		Map<String, String> localizedValues = s.getLocalizedValues();
		
		if (localizedValues == null) {
			return null;
		}
		
		while (locale != null) {
			String localizedName = localizedValues.get(locale);

			if (localizedName != null)
				return localizedName;

			int index = locale.lastIndexOf('_');
			if (index != -1)
				locale = locale.substring(0, index);
			else
				locale = null;
		}

		return getDefault(s, null);
	}
	
	/**
	 * Invokes {@link I18nTools#getDefault(LocalizedString, String)} with a default value of null.
	 * 
	 * @param s The localized String.
	 * @return The default value or null, if no default value is available.
	 */
	public static String getDefault(LocalizedString s) {
		return getDefault(s, null);
	}

	/**
	 * Gets the default value of the {@link com.braintribe.model.generic.i18n.LocalizedString} (which has
	 * the key &quot;default&quot;). If this value is not set in the map,
	 * the provided value of defaultIfNotAvailable will be returned. 
	 * 
	 * @param s The localized String.
	 * @param defaultIfNotAvailable The value that should be returned when no default value is specified.
	 * @return The default value or defaultIfNotAvailable, if no default value is available.
	 */
	public static String getDefault(LocalizedString s, String defaultIfNotAvailable) {
		if (s == null) {
			return defaultIfNotAvailable;
		}
		Map<String, String> localizedValues = s.getLocalizedValues();
		if (localizedValues == null) {
			return defaultIfNotAvailable;
		}
		
		String defaultValue = localizedValues.get(defaultLocale);
		if (defaultValue == null) {
			return defaultIfNotAvailable;
		}
		
		return defaultValue;
	}
	
	
	/**
	 * Convenient method to create a {@link LocalizedString} for the default locale.
	 */
	public static LocalizedString createLs(GmSession session, String globalId, String defaultValue) {
		LocalizedString localizedString = session.create(LocalizedString.T).putDefault(defaultValue);
		localizedString.setGlobalId(globalId);
		return localizedString;
	}
	
	/**
	 * Convenient method to create a {@link LocalizedString} for the default locale.
	 */
	public static LocalizedString createLs(GmSession session, String defaultValue) {
		return session.create(LocalizedString.T).putDefault(defaultValue);
	}
	
	
	/**
	 * Convenient method to create a {@link LocalizedString} for the default locale.
	 */
	public static LocalizedString createLs(String defaultValue) {
		return lsBuilder()
					.addDefault(defaultValue)
					.build();
	}

	public static LocalizedString createLsWithGlobalId(String defaultValue, String globalId) {
		LocalizedString localizedString = LocalizedString.T.create().putDefault(defaultValue);
		localizedString.setGlobalId(globalId);
		return localizedString;
	}
	
	/**
	 * Convenient method to create a {@link LocalizedString} with a default value and value for a given locale.
	 */
	public static LocalizedString createLs(String defaultValue, String locale, String value) {
		return lsBuilder()
					.addDefault(defaultValue)
					.addValue(locale, value)
					.build();
	}
	
	/**
	 * Returns a {@link LocalizedStringBuilder} which can be used to build a {@link LocalizedString}. 
	 */
	public static LocalizedStringBuilder lsBuilder() {
		return new LocalizedStringBuilder();
	}

	/**
	 * A builder for {@link LocalizedString}'s. 
	 */
	public static class LocalizedStringBuilder {
		private final Map<String, String> map = new HashMap<String,String>();
		private Supplier<LocalizedString> factory = new Supplier<LocalizedString>() {
			@Override
			public LocalizedString get() throws RuntimeException {
				return LocalizedString.T.create();
			}
		};

		/**
		 * A custom factory ({@link Supplier}) can be configured in case the {@link LocalizedString}
		 * instance needs to be created in a special way.<br />
		 * A common use case is to create the instance on an existing session object.
		 */
		public LocalizedStringBuilder factory(Supplier<LocalizedString> factory) {
			this.factory = factory;
			return this;
		}
		
		/**
		 * Adds a value for the default locale. 
		 */
		public LocalizedStringBuilder addDefault(String value) {
			return addValue(defaultLocale, value);
		}
		
		/**
		 * Adds a value for the given locale.
		 */
		public LocalizedStringBuilder addValue(String locale, String value) {
			this.map.put(locale, value);
			return this;
		}
		
		/**
		 * Finally builds the {@link LocalizedString} instance provided by the lsProvider and
		 * adds all values with their associated to locales. 
		 */
		public LocalizedString build() {
			try {
				LocalizedString ls = factory.get();
				ls.getLocalizedValues().putAll(map);
				return ls;
			} catch (RuntimeException e) {
				throw new GenericModelException("Could not create new LocalizedString instance.",e);
			}
		}
		
		
		
	}
	
}
