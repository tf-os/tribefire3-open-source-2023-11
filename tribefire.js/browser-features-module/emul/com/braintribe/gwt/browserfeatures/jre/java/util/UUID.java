// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.util;

import java.io.Serializable;

public final class UUID implements Serializable, Comparable<UUID> {
	private static final long serialVersionUID = 4994166298252940096L;

	private final long msb;
	private final long lsb;

	public UUID(long msb, long lsb) {
		this.msb = msb;
		this.lsb = lsb;
	}

	public static UUID fromString(String encodedUUID) {

		String[] uuidParts = encodedUUID.split("-");
		if (uuidParts.length != 5) {
			throw new IllegalArgumentException("UUID string \"" + encodedUUID + "\" has too many parts.");
		}

		long msb = Long.parseLong(uuidParts[0], 16);
		msb <<= 16;
		msb |= Long.parseLong(uuidParts[1], 16);
		msb <<= 16;
		msb |= Long.parseLong(uuidParts[2], 16);

		long lsb = Long.parseLong(uuidParts[3], 16);
		lsb <<= 48;
		lsb |= Long.parseLong(uuidParts[4], 16);

		return new UUID(msb, lsb);
	}

	public long getLeastSignificantBits() {
		return this.lsb;
	}
	
	public static UUID randomUUID() {
		return fromString(generateUUIDString());
	}

	public long getMostSignificantBits() {
		return this.msb;
	}

	private static String toHex(long value, int digits) {
		long high = 1L << (digits * 4);
		return Long.toHexString(high | (value & (high - 1))).substring(1);
	}

	public int compareTo(UUID value) {
		return (this.msb < value.msb ? -1 : (this.msb > value.msb ? 1 : (this.lsb < value.lsb ? -1
				: (this.lsb > value.lsb ? 1 : 0))));
	}

	@Override
	public String toString() {
		return (toHex(this.msb >> 32, 8) + "-" + toHex(this.msb >> 16, 4) + "-" + toHex(this.msb, 4) + "-"
				+ toHex(this.lsb >> 48, 4) + "-" + toHex(this.lsb, 12));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UUID))
			return false;

		UUID id = (UUID) obj;
		return (this.msb == id.msb && this.lsb == id.lsb);
	}

	@Override
	public int hashCode() {
		return (int) ((this.msb >> 32) ^ this.msb ^ (this.lsb >> 32) ^ this.lsb);
	}
	
	private static native String generateUUIDString() /*-{
		var chars = '0123456789ABCDEF'.split(''), uuid = [];
		radix = chars.length;
		
		var r;
		
		// rfc4122 requires these characters
		uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
		uuid[14] = '4';
		
		// Fill in random data.  At i==19 set the high bits of clock sequence as per rfc4122, sec. 4.1.5
		for (var i = 0; i < 36; i++) {
			if (!uuid[i]) {
				r = 0 | Math.random() * 16;
				uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
			}
		}
		
		return uuid.join('');
	}-*/;
}
