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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides operating system related convenience methods and classes.
 *
 * @author michael.lafite
 */
public class OsTools {

	private static final OperatingSystem OPERATING_SYSTEM;
	private static final int ARCHITECTURE_BITS;

	/**
	 * Set of known operating systems.
	 *
	 * @author michael.lafite
	 */
	public enum OperatingSystem {
		Linux,
		MacOS,
		Windows,
		Other
	}

	/**
	 * Initializes some fields.
	 */
	static {
		// *** Set Operating System ***
		final String osName = getOsName();
		if (osName.contains("mac") || osName.contains("darwin")) {
			OPERATING_SYSTEM = OperatingSystem.MacOS;
		} else if (osName.contains("windows")) {
			OPERATING_SYSTEM = OperatingSystem.Windows;
		} else if (osName.contains("linux")) {
			OPERATING_SYSTEM = OperatingSystem.Linux;
		} else {
			OPERATING_SYSTEM = OperatingSystem.Other;
		}

		// very simple check for 64 bit architecture (could be improved)
		final String osArch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
		ARCHITECTURE_BITS = osArch.contains("64") ? 64 : 32;
	}

	private OsTools() {
		// no instantiation required
	}

	/**
	 * Returns the operating system. Note that currently the result is based on a simple check, if the operating system name (based on property
	 * <code>os.name</code>) contains (ignoring case) "mac" (or "darwin"), "windows" or "linux". Otherwise the method returns
	 * {@link OperatingSystem#Other}. Please note that this may be improved in future versions. For example, further OS types may be added, which
	 * means one must not rely on <code>Other</code> to be returned.
	 */
	public static OperatingSystem getOperatingSystem() {
		return OPERATING_SYSTEM;
	}

	/**
	 * Returns <code>true</code>, if the JVM is running on a Windows {@link #getOperatingSystem() operating system}, otherwise <code>false</code>.
	 */
	public static boolean isWindowsOperatingSystem() {
		return OPERATING_SYSTEM == OperatingSystem.Windows;
	}

	public static boolean isUnixSystem() {
		switch (OPERATING_SYSTEM) {
			case Linux:
			case MacOS:
				return true;
			case Windows:
				return false;
			default: {
				String os = getOsName();
				return os.contains("nix") || os.contains("nux") || os.contains("aix") || os.contains("mac");
			}
		}
	}

	private static String getOsName() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Returns the architecture bits based property <code>os.arch</code>. Note that currently there is only a simple check, if the architecture name
	 * contains "64", in which case <code>64</code> is returned, otherwise <code>32</code>. This may be changed/improved in the future. Therefore one
	 * must not rely on this!
	 */
	public static int getArchitetureBits() {
		return ARCHITECTURE_BITS;
	}

	/**
	 * Simple helper class used to build file names that depend on the {@link OsTools#getOperatingSystem() operating system},
	 * {@link OsTools#getArchitetureBits() architecture bits}, etc.
	 *
	 * @author michael.lafite
	 */
	public static class OsSpecificFileNameBuilder {

		private final Map<String, Object> settings = new HashMap<>();

		private enum Setting {
			Prefix,
			OperatingSystemPart,
			ArchitectureBitsPartIncluded,
			Suffix,
			Extension,
			FileNamePartDelimiter
		}

		public OsSpecificFileNameBuilder() {
			withDefaultArchitectureBitsPartIncluded(false);
			withDefaultFileNamePartDelimiter("_");
			// set operating system part based on enum name
			for (final OperatingSystem operatingSystem : OperatingSystem.values()) {
				withOperatingSystemPart(operatingSystem, operatingSystem.name().toLowerCase(Locale.ENGLISH));
			}
			// override operating system part setting for some operating systems
			withOperatingSystemPart(OperatingSystem.Windows, "win");
			withOperatingSystemPart(OperatingSystem.MacOS, "mac");
		}

		private void setSetting(final OperatingSystem operatingSystem, final Setting setting, final Object value) {
			this.settings.put(getSettingsKey(operatingSystem, setting), value);
		}

		private static String getSettingsKey(final OperatingSystem operatingSystem, final Setting setting) {
			return "[OS:" + ((operatingSystem != null) ? operatingSystem.name() : "none") + "|Setting:" + setting.name() + "]";
		}

		public Object getSetting(final Setting setting) {
			Object settingValue = this.settings.get(getSettingsKey(OPERATING_SYSTEM, setting));
			if (settingValue == null) {
				settingValue = this.settings.get(getSettingsKey(null, setting));
			}

			return settingValue;
		}

		public OsSpecificFileNameBuilder withDefaultNamePrefix(final String prefix) {
			return withNamePrefix(null, prefix);
		}

		public OsSpecificFileNameBuilder withNamePrefix(final OperatingSystem operatingSystem, final String prefix) {
			setSetting(operatingSystem, Setting.Prefix, prefix);
			return this;
		}

		public OsSpecificFileNameBuilder withDefaultOperatingSystemPart(final String operatingSystemPart) {
			return withOperatingSystemPart(null, operatingSystemPart);
		}

		public OsSpecificFileNameBuilder withOperatingSystemPart(final OperatingSystem operatingSystem, final String operatingSystemPart) {
			setSetting(operatingSystem, Setting.OperatingSystemPart, operatingSystemPart);
			return this;
		}

		public OsSpecificFileNameBuilder withDefaultArchitectureBitsPartIncluded(final boolean architectureBitsPartIncluded) {
			return withArchitectureBitsPartIncluded(null, architectureBitsPartIncluded);
		}

		public OsSpecificFileNameBuilder withArchitectureBitsPartIncluded(final OperatingSystem operatingSystem,
				final boolean architectureBitsPartIncluded) {
			setSetting(operatingSystem, Setting.ArchitectureBitsPartIncluded, architectureBitsPartIncluded);
			return this;
		}

		public OsSpecificFileNameBuilder withDefaultNameSuffix(final String suffix) {
			return withNameSuffix(null, suffix);
		}

		public OsSpecificFileNameBuilder withNameSuffix(final OperatingSystem operatingSystem, final String suffix) {
			setSetting(operatingSystem, Setting.Suffix, suffix);
			return this;
		}

		public OsSpecificFileNameBuilder withDefaultExtension(final String extension) {
			return withExtension(null, extension);
		}

		public OsSpecificFileNameBuilder withExtension(final OperatingSystem operatingSystem, final String extension) {
			setSetting(operatingSystem, Setting.Extension, extension);
			return this;
		}

		public OsSpecificFileNameBuilder withDefaultFileNamePartDelimiter(final String fileNamePartDelimiter) {
			return withFileNamePartDelimiter(null, fileNamePartDelimiter);
		}

		public OsSpecificFileNameBuilder withFileNamePartDelimiter(final OperatingSystem operatingSystem, final String fileNamePartDelimiter) {
			setSetting(operatingSystem, Setting.FileNamePartDelimiter, fileNamePartDelimiter);
			return this;
		}

		public String build() {
			final String prefix = (String) getSetting(Setting.Prefix);
			final String operatingSystemPart = (String) getSetting(Setting.OperatingSystemPart);
			final boolean architectureBitsPartIncluded = (Boolean) getSetting(Setting.ArchitectureBitsPartIncluded);
			final String architectureBitsPart = architectureBitsPartIncluded ? "" + ARCHITECTURE_BITS : null;
			final String suffix = (String) getSetting(Setting.Suffix);
			String extension = (String) getSetting(Setting.Extension);
			if (extension != null && !extension.startsWith(".")) {
				extension = "." + extension;
			}

			final List<String> fileNameParts = CommonTools.getList(prefix, operatingSystemPart, architectureBitsPart, suffix);

			final String fileNamePartDelimiter = (String) getSetting(Setting.FileNamePartDelimiter);

			final StringBuilder builder = new StringBuilder();
			boolean fileNamePartAppended = false;
			for (final String fileNamePart : fileNameParts) {
				if (fileNamePart != null) {
					// if we already appended a file name part, we have to append delimiter before appending the next
					// one
					if (fileNamePartAppended) {
						if (fileNamePartDelimiter != null) {
							builder.append(fileNamePartDelimiter);
						}
					} else {
						fileNamePartAppended = true;
					}

					builder.append(fileNamePart);
				}
			}

			if (extension != null) {
				builder.append(extension);
			}

			final String result = builder.toString();
			return result;
		}
	}

}
