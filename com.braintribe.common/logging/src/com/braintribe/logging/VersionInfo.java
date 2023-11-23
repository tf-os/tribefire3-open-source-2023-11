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
package com.braintribe.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionInfo {

	protected static final DateFormat dateTimePattern = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ZZ");

	protected static String getFileInfo(final URL u, final boolean terse) throws IOException {
		final URLConnection conn = u.openConnection();

		final long time = conn.getLastModified();
		final long size = conn.getContentLength();

		final StringBuilder buff = new StringBuilder();

		String stamp = null;

		if (time >= 0) {
			buff.append("T");
			buff.append(time);

			if (size >= 0) {
				buff.append("Z");
				buff.append(size);
			}

			stamp = buff.toString();
			buff.setLength(0);
		}

		if (terse) {
			return stamp;
		}

		buff.append("Time: ");

		if (time < 0) {
			buff.append("<unknown>");
		} else {
			buff.append(time);
			buff.append(" (");
			buff.append(dateTimePattern.format(new Date(time))); // TODO: pattern, TZ
			buff.append(")");
		}

		buff.append("\n");

		buff.append("Size: ");

		if (size < 0) {
			buff.append("<unknown>");
		} else {
			buff.append(size);
			buff.append(" (");
			buff.append(size / 1024); // TODO: pattern, TZ
			buff.append("KB)");
		}

		buff.append("\n");

		buff.append("Stamp: ");

		if (stamp == null) {
			buff.append("<unknown>");
		} else {
			buff.append(stamp);
		}

		buff.append("\n");

		return buff.toString();
	}

	protected static void appendFileInfo(final URL u, final String name, final boolean slurp, final StringBuilder buff) {
		buff.append(name);
		buff.append(": ");

		if (u == null) {
			buff.append("<not found>");
		} else {
			try {
				buff.append(u.toExternalForm());
				buff.append(":\n\t");

				String v = slurp ? slurp(u) : getFileInfo(u, false);
				v = v.trim().replaceAll("(\r\n|\r|\n)", "\n\t");

				buff.append(v);
			} catch (final IOException ex) {
				buff.append("<error: ");
				buff.append(ex.toString());
				buff.append(">");
			}
		}

	}

	protected static final String[] manifestAttributes = new String[] { "Bundle-Name", "Bundle-Version", "Implementation-Version", "SHA1-Digest", };

	protected static void appendManifestInfo(final String name, final Attributes attributes, final StringBuilder buff) {
		buff.append(name);
		buff.append(": ");

		if (attributes == null) {
			buff.append("<not found>");
		} else {
			for (int i = 0; i < manifestAttributes.length; i++) {
				buff.append("\n\t");

				final String n = manifestAttributes[i];
				String v = attributes.getValue(n);

				buff.append(n);
				buff.append(": ");

				if (v == null || v.trim().length() == 0) {
					v = "<not found>";
				}
				buff.append(v);
			}
		}
	}

	public static String getVersionStamp(final Class<?> cls) {
		// try version file
		final URL versionFile = getVersionFile(cls);
		if (versionFile != null) {
			try {
				final String v = slurp(versionFile);
				return v.trim();
			} catch (final IOException e) {
				// ignore
			}
		}

		// try manifest
		final Manifest mf = getManifest(cls);
		if (mf != null) {
			final String v = mf.getMainAttributes().getValue("Bundle-Version");
			if (v != null && v.trim().length() > 0) {
				return v;
			}
		}

		// try jar file specs
		final URL jarFile = getJarFile(cls);
		if (jarFile != null) {
			try {
				final String v = getFileInfo(jarFile, true);
				if (v != null) {
					return "J(" + v.trim() + ")";
				}
			} catch (final IOException e) {
				// ignore
			}
		}

		// try class file specs
		final URL classFile = getClassFile(cls);
		if (classFile != null) {
			try {
				final String v = getFileInfo(classFile, true);
				if (v != null) {
					return "C(" + v.trim() + ")";
				}
			} catch (final IOException e) {
				// ignore
			}
		}

		// this is very strange and shouldn't happen.
		return "<unknown>";
	}

	public static String getVersionInfo(final Class<?> cls) {
		final URL versionFile = getVersionFile(cls);
		final URL classFile = getClassFile(cls);
		final URL jarFile = getJarFile(cls);
		final Manifest mani = getManifest(cls);

		final StringBuilder buff = new StringBuilder();

		buff.append("VERSION INFO for " + cls.getName());

		buff.append("\n\n");
		buff.append("VERSION STAMP: ");
		buff.append(getVersionStamp(cls));

		buff.append("\n\n");
		appendFileInfo(versionFile, "VERSION FILE", true, buff);

		buff.append("\n\n");

		if (mani == null) {
			buff.append("MANIFEST INFO: <not found>");
		} else {
			appendManifestInfo("MANIFEST INFO", mani.getMainAttributes(), buff);

			// buff.append("\n\n");
			// String n = cls.getCanonicalName();
			// n = n.replaceAll("\\.", "/");
			// appendManifestInfo("CLASS MANIFEST INFO", mani.getAttributes( n + ".class" ), buff);
		}

		buff.append("\n\n");
		appendFileInfo(jarFile, "JAR FILE", false, buff);

		buff.append("\n\n");
		appendFileInfo(classFile, "CLASS FILE", false, buff);

		buff.append("\n\n");

		return buff.toString();
	}

	public static Manifest getManifest(final Class<?> cls) {
		try {
			final URL u = getManifestFile(cls);
			if (u == null) {
				return null;
			}

			final InputStream in = u.openStream();
			final Manifest m = new Manifest(in);
			in.close();

			return m;
		} catch (final IOException e) {
			// throw new RuntimeException("failed to load manifest", e);
			return null; // no manifest.
		}
	}

	public static URL getManifestFile(final Class<?> cls) {
		final URL jar = getJarFile(cls);
		if (jar == null) {
			return null;
		}

		try {
			final String path = "jar:" + jar.toExternalForm() + "!/META-INF/MANIFEST.MF";
			final URL u = new URL(path);

			return u; // TODO: check if exists
		} catch (final IOException e) {
			// throw new RuntimeException("failed to load manifest", e);
			return null; // no manifest.
		}
	}

	public static URL getClassFile(final Class<?> cls) {
		if (cls.isAnonymousClass()) {
			throw new IllegalArgumentException(cls.getName() + " is anonymous");
		}
		if (cls.isPrimitive()) {
			throw new IllegalArgumentException(cls.getName() + " is primitive");
		}
		if (cls.isArray()) {
			throw new IllegalArgumentException(cls.getName() + " is an array class");
		}

		final String n = cls.getSimpleName();
		if (n == null) {
			throw new IllegalArgumentException("can't get simple name for " + cls.getName());
		}

		final URL rc = cls.getResource(n + ".class");
		if (rc == null) {
			throw new IllegalArgumentException("can't find class file for " + cls.getName());
		}

		return rc;
	}

	public static URL getJarFile(final Class<?> cls) {
		final URL rc = getClassFile(cls);
		if (!rc.getProtocol().equals("jar")) {
			return null; // not loaded from a jar file
		}

		final String jarpath = rc.toExternalForm();

		final Matcher m = Pattern.compile("^jar:(.*?)!/.*").matcher(jarpath);
		if (!m.matches()) {
			throw new RuntimeException("bad jar path: " + jarpath);
		}

		final String jurl = m.group(1);

		try {
			return new URL(jurl);
		} catch (final MalformedURLException e) {
			throw new RuntimeException("bad jar file URL: " + jurl, e);
		}
	}

	public static URL getVersionFile(final Class<?> cls) {
		String n = cls.getCanonicalName();
		n = n.replaceAll("\\.", "/");
		return getVersionFile(cls.getClassLoader(), n);
	}

	public static URL getVersionFile(final ClassLoader cl, final String path) {
		URL u = cl.getResource(path + ".version");

		if (u == null) {
			u = cl.getResource(path + "/version");
		}

		if (u == null) {
			final String p = path.replaceAll("[/\\.\\$][\\w\\d]+$", "");
			if (p.length() < path.length()) {
				u = getVersionFile(cl, p);
			}
		}

		return u;
	}

	protected static String slurp(final URL u) throws IOException {
		final URLConnection conn = u.openConnection();
		final InputStream in = conn.getInputStream();

		final String s = slurp(in, conn.getContentEncoding());
		in.close();

		return s;
	}

	protected static String slurp(final InputStream in, final String enc) throws IOException {
		final InputStreamReader rd = enc == null ? new InputStreamReader(in) : new InputStreamReader(in, enc);
		final String s = slurp(rd);
		rd.close();

		return s;
	}

	protected static String slurp(final Reader rd) throws IOException {
		final StringBuffer s = new StringBuffer();

		final char[] buff = new char[1024 * 16];
		while (true) {
			final int c = rd.read(buff);
			if (c <= 0) {
				break;
			}
			s.append(buff, 0, c);
		}

		return s.toString();
	}

	public static void main(final String[] args) throws ClassNotFoundException {
		final String c = args.length > 0 ? args[0] : null;

		final Class<?> cls = c == null ? VersionInfo.class : Class.forName(c);

		final String info = getVersionInfo(cls);

		System.out.println(info);
	}

}
