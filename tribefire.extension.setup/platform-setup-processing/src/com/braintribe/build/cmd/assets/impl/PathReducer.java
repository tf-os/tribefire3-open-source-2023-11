package com.braintribe.build.cmd.assets.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PathReducer {
	private Path reducedPath;
	
	public void process(Path path) {
		if (reducedPath == null) {
			reducedPath = path;
		}
		else {
			Iterator<String> i1 = elementStream(path).iterator();
			Iterator<String> i2 = elementStream(reducedPath).iterator();

			Path reduced = null;
			
			while (i1.hasNext() && i2.hasNext()) {
				String s1 = i1.next();
				String s2 = i2.next();
				if (!s1.equals(s2)) {
					break;
				}
				if (reduced == null)
					reduced = Paths.get(s1);
				else
					reduced = reduced.resolve(s1);
			}

			reducedPath = reduced;
		}
	}
	
	private static Stream<String> elementStream(Path path) {
		Path root = path.getRoot();
		
		Stream<String> pathStream = StreamSupport.stream(path.spliterator(), false).map(Path::toString);
		
		if (root != null) {
			String rootName = root.toString();
			pathStream = Stream.concat(Stream.of(rootName), pathStream);
		}

		return pathStream;
	}

	
	public Path getReducedPath() {
		return reducedPath;
	}
}