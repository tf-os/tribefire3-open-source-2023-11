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
package com.braintribe.utils;

import java.util.Comparator;

/**
 * This class can be used to hold a version number, which is supposed to have a format like x.y.z
 *
 * There is no limit on how many levels the version may have. Each part of the version must be a number (i.e., an int value). No Strings are allowed.
 * This class is able to parse Version Strings as well as comparing two Version objects for equality or rank (i.e., which version is higher).
 *
 */
public class Version implements Comparator<Version> {

	private final static int INVALID_VERSION = 0;

	protected String versionString = null;
	protected boolean parseableVersionString = false;

	protected int[] versionParts = null;

	/**
	 * Creates a Version based on the String provided in the constructor.
	 *
	 * @param version
	 *            The version as a String (e.g., 1.1.23)
	 * @throws IllegalArgumentException
	 *             Thrown when the version argument is null.
	 */
	public Version(String version) throws IllegalArgumentException {
		if (version == null) {
			throw new IllegalArgumentException("The version must not be null.");
		}
		this.parse(version);
	}

	/**
	 * Creates a Version by copying the version information from the provided template. If the provided Version is, for example, 1.1.23, this newly
	 * created Version will contain the exact same version information.
	 *
	 * @param other
	 *            The version object that contains the version information that should be copied.
	 * @throws IllegalArgumentException
	 *             Thrown if the provided version object is null.
	 */
	public Version(Version other) throws IllegalArgumentException {
		if (other == null) {
			throw new IllegalArgumentException("The prototype version must not be null.");
		}
		this.versionString = other.versionString;
		this.parseableVersionString = other.parseableVersionString;
		if (other.versionParts != null) {
			int size = other.versionParts.length;
			this.versionParts = new int[size];
			System.arraycopy(other.versionParts, 0, this.versionParts, 0, size);
		}
	}

	/**
	 * Creates a new Version object based on the individual parts of the version. The first element of the array would be the major version, the
	 * second the minor version, and so on. THere is no limit on the number of parts.
	 *
	 * @param versionParts
	 *            The numbers for the individual parts (levels) of the version. If an empty array is provided, the version will be set to 0.
	 * @throws IllegalArgumentException
	 *             Thrown if the provided array is null.
	 */
	public Version(int[] versionParts) throws IllegalArgumentException {
		if (versionParts == null) {
			throw new IllegalArgumentException("The versionParts must not be null.");
		}
		if (versionParts.length == 0) {
			this.versionParts = new int[] { 0 };
		} else {
			this.versionParts = versionParts;
		}
		this.parseableVersionString = true;
	}

	protected void parse(String version) {

		if (version != null) {
			this.versionString = version;
			this.parseableVersionString = true;

			String[] parts = version.split("\\.");
			if (parts != null) {
				this.versionParts = new int[parts.length];
				for (int i = 0; i < parts.length; ++i) {
					this.versionParts[i] = parseVersionPart(parts[i]);
				}
			}
		}
	}

	protected int parseVersionPart(String part) {
		try {
			int partInt = Integer.parseInt(part);
			return partInt;
		} catch (Exception e) {
			this.parseableVersionString = false;
			// ignore
			return INVALID_VERSION;
		}
	}

	@Override
	public int compare(Version o1, Version o2) {
		if (!o1.parseableVersionString || !o2.parseableVersionString) {
			return o1.versionString.compareTo(o2.versionString);
		}
		int o1Length = o1.versionParts.length;
		int o2Length = o2.versionParts.length;
		int size = Math.min(o1Length, o2Length);
		for (int i = 0; i < size; ++i) {
			if (o1.versionParts[i] < o2.versionParts[i]) {
				return -1;
			}
			if (o1.versionParts[i] > o2.versionParts[i]) {
				return 1;
			}
		}
		if (o1Length < o2Length) {
			return -1;
		}
		if (o1Length > o2Length) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Version)) {
			return false;
		}
		// TODO: MLA: why not always compare the version string? for a typical version string, this might even be faster
		Version other = (Version) obj;
		if (!parseableVersionString || !other.parseableVersionString) {
			return versionString.equals(other.versionString);
		}
		int o1Length = this.versionParts.length;
		int o2Length = other.versionParts.length;
		if (o1Length != o2Length) {
			return false;
		}
		for (int i = 0; i < o1Length; ++i) {
			if (this.versionParts[i] != other.versionParts[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (versionString != null) {
			return versionString.hashCode();
		} else {
			return 0;
		}
	}

	public boolean isParseableVersionString() {
		return parseableVersionString;
	}

	@Override
	public String toString() {
		if (this.parseableVersionString) {
			StringBuilder sb = new StringBuilder();
			for (int versionPart : this.versionParts) {
				if (sb.length() > 0) {
					sb.append(".");
				}
				sb.append(versionPart);
			}
			return sb.toString();
		} else {
			return this.versionString;
		}
	}

	public int[] getVersionParts() {
		return versionParts;
	}

}
