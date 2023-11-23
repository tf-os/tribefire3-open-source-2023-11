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
package com.braintribe.model.processing.core.commons.datasink;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Date;

/**
 * This interface resembles most methods from {@link DataOutput} but leaves out the thrown {@link IOException}
 * to make this interface better usable in lambda expressions. Also most of the methods have meaningful default
 * implementations.
 * 
 * With {@link #from(DataOutput)} and {@link #from(MessageDigest)} meaning full implementations can be retrieved.
 * 
 * @author Dirk Scheffler
 *
 */
public interface DataSink {
    void write(int b);
    void write(byte b[], int off, int len);
    
    default void write(byte b[]) {
    	write(b, 0, b.length);
    }
    
    default void writeBoolean(boolean v) {
    	write(v ? 1 : 0);
    }
    
    default void writeByte(int v) {
    	write(v);
    }
    
    default void writeShort(int v) {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }
    
    default void writeChar(int v) {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }
    
    default void writeInt(int v) {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>>  0) & 0xFF);
    }
    
    default void writeLong(long v) {
    	write((byte)(v >>> 56));
    	write((byte)(v >>> 48));
    	write((byte)(v >>> 40));
    	write((byte)(v >>> 32));
    	write((byte)(v >>> 24));
    	write((byte)(v >>> 16));
    	write((byte)(v >>>  8));
    	write((byte)(v >>>  0));
    }
    
    default void writeFloat(float v) {
    	writeInt(Float.floatToIntBits(v));
    }
    
    default void writeDouble(double v) {
    	writeLong(Double.doubleToLongBits(v));
    }
    
    default void writeDecimal(BigDecimal value) {
		writeString(value.toString());
	}
	
	default void writeDate(Date value) {
		writeLong(value.getTime());
	}
	
	default void writeEnum(Enum<?> value) {
		writeString(value.name());
	}

	default void writeString(String s) {
		try {
			byte[] bytes = s.getBytes("UTF-8");
			writeInt(bytes.length);
			write(bytes);
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	static DataSink from(DataOutput dataOutput) {
		return new DataSink() {
			@Override
			public void write(byte[] b, int off, int len) {
				try {
					dataOutput.write(b, off, len);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			
			@Override
			public void write(int b) {
				try {
					dataOutput.write(b);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		};
	}
	
	static DataSink from(MessageDigest digest) {
		return new DataSink() {
			@Override
			public void write(byte[] b, int off, int len) {
				digest.update(b, off, len);
			}
			
			@Override
			public void write(int b) {
				digest.update((byte)b);
			}
		};
	}
}
