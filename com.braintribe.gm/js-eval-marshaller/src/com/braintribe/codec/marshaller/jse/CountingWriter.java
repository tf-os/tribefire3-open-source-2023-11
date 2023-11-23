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
package com.braintribe.codec.marshaller.jse;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CountingWriter extends Writer {
	private Writer delegate;
	private int charactersPerBlock;
	private int count;
	private int lastCount;
	private List<Integer> splitPoints = new ArrayList<Integer>();
	
	public CountingWriter(Writer delegate, int charactersPerBlock) {
		super();
		this.delegate = delegate;
		this.charactersPerBlock = charactersPerBlock;
	}
	
	public int getCount() {
		return count;
	}
	
	public List<Integer> getSplitPoints() {
		return splitPoints;
	}

	public void writeBreakingPoint() throws IOException {
		delegate.write('\n');
		count++;
		
		if ((count - lastCount) > charactersPerBlock) {
			//delegate.write(breakingPoint);
			//delegate.write(JseFunctionsImport.functionsImport);
			splitPoints.add(count);
			lastCount = count;
		}
	}
	
	public void write(int c) throws IOException {
		count++;
		delegate.write(c);
	}

	public void write(char[] cbuf) throws IOException {
		count += cbuf.length;
		delegate.write(cbuf);
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		count += len;
		delegate.write(cbuf, off, len);
	}

	public void write(String str) throws IOException {
		count += str.length();
		delegate.write(str);
	}

	public void write(String str, int off, int len) throws IOException {
		count += len;
		delegate.write(str, off, len);
	}

	public Writer append(CharSequence csq) throws IOException {
		count += csq.length();
		return delegate.append(csq);
	}

	public Writer append(CharSequence csq, int start, int end) throws IOException {
		count += end - start;
		return delegate.append(csq, start, end);
	}

	public Writer append(char c) throws IOException {
		count++;
		return delegate.append(c);
	}

	public void flush() throws IOException {
		delegate.flush();
	}

	public void close() throws IOException {
		delegate.close();
	}
}
