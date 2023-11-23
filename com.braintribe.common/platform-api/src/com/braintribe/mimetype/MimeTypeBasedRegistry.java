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
package com.braintribe.mimetype;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author peter.gazdik
 */
public class MimeTypeBasedRegistry<T> {

	private final Map<String, MimeTypeBaseEntry<T>> map = new ConcurrentHashMap<>();

	// ###############################################
	// ## . . . . . . . . . Read . . . . . . . . . .##
	// ###############################################

	/** Tries to find the value associated with given mime-type. Directly derived from {@link #findEntry(String)} */
	public T find(String mimeType) {
		MimeTypeBaseEntry<T> e = findEntry(mimeType);
		return e != null ? e.getValue() : null;
	}

	/**
	 * Tries to find the entry associated with given mimeType. This method supports wildcards as input, e.g. "application/*"
	 */
	public MimeTypeBaseEntry<T> findEntry(String mimeType) {
		if (mimeType == null) {
			return null;
		}

		if (containsWildcard(mimeType)) {
			return getCompatibleRegistryEntries(mimeType).findFirst().orElse(null);
		}

		String key = normalizeMimetype(mimeType);
		return map.get(key);
	}

	/**
	 * Tries to find the entries associated with given mimeType. This method supports wildcards as input, e.g. "application/*"
	 */
	public List<? extends MimeTypeBaseEntry<T>> getEntries(String mimeType) {
		if (mimeType == null) {
			return null;
		}

		if (containsWildcard(mimeType)) {
			return getCompatibleRegistryEntries(mimeType).collect(Collectors.toList());
		}

		String key = normalizeMimetype(mimeType);

		MimeTypeBaseEntry<T> entry = map.get(key);
		return entry == null ? Collections.EMPTY_LIST : Collections.singletonList(entry);
	}

	public Stream<? extends MimeTypeBaseEntry<T>> streamEntries(String mimeType) {
		if (mimeType == null) {
			return Stream.empty();
		}

		if (containsWildcard(mimeType)) {
			return getCompatibleRegistryEntries(mimeType);
		}

		String key = normalizeMimetype(mimeType);

		MimeTypeBaseEntry<T> entry = map.get(key);
		return entry == null ? Stream.empty() : Stream.of(entry);
	}

	private boolean containsWildcard(String mimeType) {
		return mimeType.length() > 0 && (mimeType.charAt(mimeType.length() - 1) == '*' || mimeType.charAt(0) == '*');
	}

	private Stream<MimeTypeBaseEntry<T>> getCompatibleRegistryEntries(String mimeTypeWithWildcard) {
		Pattern pattern = Pattern.compile(mimeTypeWithWildcard.replace("*", ".*"));
		return map.values().stream() //
				.filter(entry -> pattern.matcher(entry.getMimeType()).matches());
	}

	// ###############################################
	// ## . . . . . . . . . Write . . . . . . . . . ##
	// ###############################################

	public void registerValue(String mimeType, T value) {
		registerValue(mimeType, value, false);
	}

	public T registerValue(String mimeType, T value, boolean force) {
		MimeTypeBaseEntry<T> previousEntry = registerEntry(newEntry(mimeType, value), force);
		return previousEntry == null ? null : previousEntry.value;
	}

	protected MimeTypeBaseEntry<T> newEntry(String mimeType, T value) {
		return new MimeTypeBaseEntry<>(mimeType, value);
	}

	public void registerEntry(MimeTypeBaseEntry<T> entry) {
		registerEntry(entry, false);
	}

	public MimeTypeBaseEntry<T> registerEntry(MimeTypeBaseEntry<T> entry, boolean force) {
		String mimeType = entry.getMimeType();
		Objects.requireNonNull(mimeType, "Mime type cannot be null for a marshaller registry entry");

		String key = normalizeMimetype(mimeType);

		if (force) {
			return map.put(key, entry);
		}

		MimeTypeBaseEntry<T> previousEntry = map.putIfAbsent(key, entry);

		if (previousEntry == null) {
			return null;
		}

		throw new IllegalStateException("Duplicate registration attempted for mimeType '" + mimeType + "'. Currently associated: "
				+ previousEntry.getValue() + ", newly to-be-registered (but ignored): " + entry.getValue());
	}

	public boolean unregisterValue(String mimeType, T value) {
		String key = normalizeMimetype(mimeType);

		MimeTypeBaseEntry<T> entry = map.get(key);
		if (entry != null && entry.value == value) {
			return map.remove(key, entry);
		} else {
			return false;
		}
	}

	protected static String normalizeMimetype(String mimeType) {
		if (mimeType == null) {
			return null;
		}

		ParsedMimeType parsedType = MimeTypeParser.getParsedMimeType(mimeType);
		parsedType.getParams().keySet().retainAll(Collections.singleton("spec"));

		return parsedType.toString();
	}

	public static class MimeTypeBaseEntry<T> {

		protected String mimeType;
		protected T value;

		public MimeTypeBaseEntry(String mimeType, T value) {
			this.mimeType = mimeType;
			this.value = value;
		}

		// @formatter:off
		public String getMimeType() { return mimeType; }
		public T getValue() { return value;}
		// @formatter:on

	}

}
