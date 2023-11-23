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
package com.braintribe.doc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import com.braintribe.exception.Exceptions;

public interface UniversalPath extends Iterable<String> {
	static UniversalPath empty() {
		return UniversalPathImpl.emptyPath;
	}

	static UniversalPath start(String name) {
		return empty().push(name);
	}
	UniversalPath normalize();

	String getName();

	Stream<String> stream();

	static UniversalPath from(Path path) {
		return UniversalPath.empty().pushFilePath(path.toString());
	}

	static UniversalPath from(File file) {
		return UniversalPath.empty().pushFilePath(file.toString());
	}

	UniversalPath push(String elements, String delimiter);
	UniversalPath push(Iterable<String> elements);
	UniversalPath push(String element);

	UniversalPath pop();
	UniversalPath popN(int n);

	UniversalPath subpath(int beginIndex, int endIndex);

	String toPath(char delimiter);

	File toFile();
	Path toPath();
	URI toUri();

	int getNameCount();
	boolean isEmpty();
	
	/**
	 * The depth of an {@link UniversalPath} is the count of its individual names, subtracted by the counts of its "..". "." and "" don't count. Note that the result may be negative.
	 * <p>
	 * Examples:<br> <code>
	 * - file: 1 <br>
	 * - a/b/c: 3 <br>
	 * - ./a/b/c: 3 <br>
	 * - ../a/b: 1 <br>
	 * - ../..: -2
	 * </code>
	 */
	default int getDepth() {
		int depth = 0;
		for (String name: this) {
			if (".".equals(name) || name.isEmpty()) {
				// do nothing
			} else if ("..".equals(name)) {
				depth -= 1;
			} else {
				depth ++;
			}
		}
		
		return depth;
	}

	// @formatter:off
	default UniversalPath pushFilePath(String elements) { return push(elements, File.separator); }
	default UniversalPath pushSlashPath(String elements) { return push(elements, "/"); }
	default UniversalPath pushBackSlashPath(String elements) { return push(elements, "\\"); }
	default UniversalPath pushDottedPath(String elements) { return push(elements, "."); }

	default UniversalPath getParent() { return pop(); }
	default UniversalPath getNthParent(int n) { return popN(n); }
	default UniversalPath getChild(String element) { return push(element); }
	default UniversalPath getSibling(String element) { return pop().push(element); }

	default UniversalPath resolve(Iterable<String> path) { return push(path); }

	default String toFilePath() { return toPath(File.separatorChar); }
	default String toDottedPath() { return toPath('.');	}
	default String toSlashPath() { return toPath('/'); }
	default String toBackslashPath() { return toPath('\\'); }
	// @formatter:on
}

class UniversalPathImpl implements UniversalPath {
	private final String name;
	private final UniversalPathImpl parent;

	static final UniversalPathImpl emptyPath = new UniversalPathImpl();

	UniversalPathImpl(String name, UniversalPathImpl parent) {
		this.name = name;
		this.parent = parent;

		if (name == null || parent == null) {
			throw new IllegalArgumentException("Cannot create path element without parent or name. Use the create() method to create an empty path.");
		}
	}

	protected UniversalPathImpl() {
		this.name = null;
		this.parent = null;
	}

	@Override
	public String toString() {
		return toSlashPath();
	}

	@Override
	public boolean isEmpty() {
		return name == null && parent == null;
	}

	@Override
	public UniversalPath push(String elements, String delimiter) {
		if (elements == null) {
			throw new IllegalArgumentException("Cannot push null to a UniversalPath");
		}

		StringTokenizer tokenizer = new StringTokenizer(elements, delimiter);

		UniversalPath result = this;

		if (elements.startsWith(delimiter)) {
			result = result.push("");
		}

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			result = result.push(token);
		}

		return result;
	}

	@Override
	public UniversalPath push(Iterable<String> elements) {
		UniversalPath result = this;

		for (String element : elements) {
			result = result.push(element);
		}

		return result;
	}

	@Override
	public UniversalPath push(String element) {
		return new UniversalPathImpl(element, this);
	}

	@Override
	public UniversalPath pop() {
		return parent;
	}

	@Override
	public UniversalPath popN(int n) {
		UniversalPath result = this;

		for (int i = 0; i < n; i++) {
			result = result.pop();
		}

		return result;
	}

	@Override
	public String toPath(char delimiter) {
		return toPath(delimiter, 0).toString();
	}

	private StringBuilder toPath(char delimiter, int pathStringLength) {
		if (isEmpty()) {
			return new StringBuilder(pathStringLength);
		} else if (parent.isEmpty()) {
			return parent.toPath(delimiter, pathStringLength + name.length()).append(name);
		} else {
			return parent.toPath(delimiter, pathStringLength + name.length() + 1) //
					.append(delimiter) //
					.append(name);
		}
	}

	@Override
	public File toFile() {
		return new File(toFilePath());
	}

	@Override
	public Path toPath() {
		return Paths.get(toFilePath());
	}

	@Override
	public Stream<String> stream() {
		return toList().stream();
	}

	public List<String> toList() {
		if (isEmpty()) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(toList(0));
	}

	private List<String> toList(int numElements) {
		if (isEmpty()) {
			return new ArrayList<>(numElements);
		}

		List<String> list = parent.toList(numElements + 1);
		list.add(this.name);

		return list;
	}

	@Override
	public URI toUri() {
		String path = toSlashPath();
		try {
			return new URI(null, null, path, null);
		} catch (URISyntaxException e) {
			throw Exceptions.unchecked(e, "Relative path was not valid URI path");
		}
	}

	@Override
	public Iterator<String> iterator() {
		return toList().iterator();
	}

	@Override
	public int getNameCount() {
		if (isEmpty()) {
			return 0;
		} else {
			return 1 + parent.getNameCount();
		}
	}

	@Override
	public UniversalPath subpath(int beginIndex, int endIndex) {
		Iterator<String> iterator = iterator();
		String currentName = null;

		if (beginIndex < 0 || endIndex <= beginIndex) {
			throw new IllegalArgumentException(
					"Following condition must be met: 0 <= beginIndex < endIndex. But got beginIndex=" + beginIndex + ", endIndex=" + endIndex);
		}

		for (int i = 0; i <= beginIndex; i++) {
			if (!iterator.hasNext()) {
				throw new IllegalArgumentException("beginIndex must be smaller than the path has elements. Was: " + beginIndex + " but path had only "
						+ getNameCount() + " elements: " + toString());
			}
			currentName = iterator.next();
		}

		UniversalPath subpath = UniversalPath.empty().push(currentName);

		for (int i = beginIndex + 1; i < endIndex; i++) {
			if (!iterator.hasNext()) {
				throw new IllegalArgumentException("endIndex must be smaller than the path has elements. Was: " + endIndex + " but path had only "
						+ getNameCount() + " elements: " + toString());
			}
			currentName = iterator.next();
			subpath = subpath.push(currentName);
		}
		return subpath;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!UniversalPathImpl.class.isAssignableFrom(obj.getClass())) {
			return false;
		}

		final UniversalPathImpl other = (UniversalPathImpl) obj;
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}

		if ((this.parent == null) ? (other.parent != null) : !this.parent.equals(other.parent)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 53 * hash + (this.parent != null ? this.parent.hashCode() : 0);
		return hash;
	}

	@Override
	public UniversalPath normalize() {
		// TODO: Make a more efficient implementation?
		return UniversalPath.from(toPath().normalize());
	}
}

class CachedUniversalPath extends UniversalPathImpl {
	private List<String> cachedList;

	@SuppressWarnings("hiding")
	static final CachedUniversalPath emptyPath = new CachedUniversalPath();

	CachedUniversalPath() {
		super();
	}

	CachedUniversalPath(String name, CachedUniversalPath parent) {
		super(name, parent);
	}

	@Override
	public List<String> toList() {
		if (cachedList == null) {
			cachedList = super.toList();
		}

		return cachedList;
	}

	@Override
	public int getNameCount() {
		if (cachedList == null) {
			return super.getNameCount();
		}

		return cachedList.size();
	}

}
