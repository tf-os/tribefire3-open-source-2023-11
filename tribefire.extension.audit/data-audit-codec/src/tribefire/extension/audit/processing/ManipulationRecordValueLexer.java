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
package tribefire.extension.audit.processing;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ManipulationRecordValueLexer {
	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

	private StringBuilder textBuffer = new StringBuilder();
	private int pos;
	private Reader reader;

	private int read() throws IOException {
		int res = reader.read();

		if (res == -1)
			return res;

		pos++;

		return res;
	}

	private void mark() throws IOException {
		reader.mark(1);
	}

	private void reset() throws IOException {
		reader.reset();
	}

	public ManipulationRecordValueLexer(Reader reader) {
		this.reader = reader;
	}

	public enum TokenType {
		// DELIMITER
		openBracket,
		closeBracket,
		openCurlyBracket,
		closeCurlyBracket,
		openParanthesis,
		closeParanthesis,
		colon,
		comma,
		arrow,
		dot,
		tilde,
		// base literals
		nullLiteral,
		stringLiteral,
		dateliteral,
		integerLiteral,
		longLiteral,
		doubleLiteral,
		floatLiteral,
		booleanLiteral,
		decimalLiteral,
		// identifier
		identifier,
		// control literal
		end,
	}

	public static class Token {
		TokenType type;
		Object value;
		int pos;

		public Token(TokenType type, int pos) {
			this.type = type;
			this.pos = pos;
		}

		public Token(TokenType type, Object value, int pos) {
			this.type = type;
			this.value = value;
			this.pos = pos;
		}

		public TokenType getType() {
			return type;
		}

		public <T> T getValue() {
			return (T) value;
		}

		public int getPos() {
			return pos;
		}

	}

	private String consumeTextBuffer() {
		String s = textBuffer.toString();
		textBuffer.setLength(0);
		return s;
	}

	private Token readIdentifier() throws IOException {
		while (true) {
			mark();
			int val = read();

			if (val == -1) {
				return buildIdentifierToken();
			}

			char c = (char) val;

			if (Character.isJavaIdentifierPart(c))
				textBuffer.append(c);
			else {
				reset();
				return buildIdentifierToken();
			}
		}
	}

	private Token buildIdentifierToken() {
		String s = consumeTextBuffer();

		switch (s) {
			case "true":
				return new Token(TokenType.booleanLiteral, true, pos);
			case "false":
				return new Token(TokenType.booleanLiteral, false, pos);
			case "null":
				return new Token(TokenType.nullLiteral, null, pos);
			default:
				return new Token(TokenType.identifier, s, pos);
		}

	}

	private Token readDate() throws IOException {
		// 2021-03-30T14:47:51.832+0000
		int len = 28;

		char buffer[] = new char[len];

		int res = readFully(reader, buffer);

		if (res < len)
			throw new IllegalStateException("unexpected end of string");

		ZonedDateTime dateTime = ZonedDateTime.parse(new String(buffer), dateFormat);

		return new Token(TokenType.dateliteral, Date.from(dateTime.toInstant()), pos);
	}

	private int readFully(Reader reader, char buffer[]) throws IOException {
		int offset = 0;
		int len = buffer.length;
		while (true) {
			int res = reader.read(buffer, offset, len - offset);

			if (res == -1)
				return offset;

			pos++;

			offset += res;

			if (offset == len) {
				return offset;
			}
		}
	}

	public Token readNextToken() throws IOException {
		while (true) {
			int val = read();

			if (val == -1)
				return new Token(TokenType.end, pos);

			char c = (char) val;

			switch (c) {
				case '.':
					return new Token(TokenType.dot, pos);
				case '[':
					return new Token(TokenType.openBracket, pos);
				case ']':
					return new Token(TokenType.closeBracket, pos);
				case '(':
					return new Token(TokenType.openParanthesis, pos);
				case ')':
					return new Token(TokenType.closeParanthesis, pos);
				case '{':
					return new Token(TokenType.openCurlyBracket, pos);
				case '}':
					return new Token(TokenType.closeCurlyBracket, pos);
				case ',':
					return new Token(TokenType.comma, pos);
				case ':':
					return new Token(TokenType.colon, pos);
				case '~':
					return new Token(TokenType.tilde, pos);

				case '+':
					return readNumber();

				case '-':
					val = read();

					if (val == -1)
						throw new IllegalStateException("unexpected character at pos " + pos);

					c = (char) val;

					if (c == '>') {
						return new Token(TokenType.arrow, pos);
					} else if (Character.isDigit(c)) {
						textBuffer.append('-');
						textBuffer.append(c);
						return readNumber();
					} else {
						throw new IllegalStateException("unexpected character at pos " + pos);
					}

				case '\'':
					return readString();

				case '@':
					return readDate();

				default:
					textBuffer.append(c);

					if (Character.isWhitespace(c))
						break;

					if (Character.isDigit(c)) {
						return readNumber();
					} else if (Character.isJavaIdentifierPart(c)) {
						return readIdentifier();
					} else {
						throw new IllegalStateException("unexpected character at pos " + pos);
					}
			}
		}
	}

	private Token readFraction() throws IOException {
		while (true) {
			mark();

			int val = read();

			if (val == -1) {
				return new Token(TokenType.doubleLiteral, Double.parseDouble(consumeTextBuffer()), pos);
			}

			char c = (char) val;

			if (Character.isDigit(c)) {
				textBuffer.append(c);
			} else {
				switch (c) {
					case 'B':
						return new Token(TokenType.decimalLiteral, new BigDecimal(consumeTextBuffer()), pos);
					case 'F':
						return new Token(TokenType.floatLiteral, Float.parseFloat(consumeTextBuffer()), pos);
					case 'D':
						return new Token(TokenType.doubleLiteral, Double.parseDouble(consumeTextBuffer()), pos);

					case 'E':
					case 'e':
						textBuffer.append(c);
						return readExponent();

					default:
						reset();
						return new Token(TokenType.decimalLiteral, Double.parseDouble(consumeTextBuffer()), pos);
				}

			}
		}
	}

	@SuppressWarnings("fallthrough")
	private Token readExponent() throws IOException {
		int i = 0;
		while (true) {
			mark();

			int val = read();

			if (val == -1) {
				return new Token(TokenType.doubleLiteral, Double.parseDouble(consumeTextBuffer()), pos);
			}

			i++;

			char c = (char) val;

			if (Character.isDigit(c)) {
				textBuffer.append(c);
			} else {
				switch (c) {
					case 'B':
						return new Token(TokenType.decimalLiteral, new BigDecimal(consumeTextBuffer()), pos);
					case 'F':
						return new Token(TokenType.floatLiteral, Float.parseFloat(consumeTextBuffer()), pos);
					case 'D':
						return new Token(TokenType.doubleLiteral, Double.parseDouble(consumeTextBuffer()), pos);

					case '+':
					case '-':
						if (i == 1) {
							textBuffer.append(c);
							break;
						}
						// FALL THROUGH

					default:
						reset();
						return new Token(TokenType.decimalLiteral, Double.parseDouble(consumeTextBuffer()), pos);
				}
			}
		}
	}

	private Token readNumber() throws IOException {

		while (true) {
			mark();

			int val = read();

			if (val == -1) {
				return new Token(TokenType.integerLiteral, Integer.parseInt(consumeTextBuffer()), pos);
			}

			char c = (char) val;

			if (Character.isDigit(c)) {
				textBuffer.append(c);
			} else {
				switch (c) {
					case 'L':
						return new Token(TokenType.longLiteral, Long.parseLong(consumeTextBuffer()), pos);
					case 'B':
						return new Token(TokenType.decimalLiteral, new BigDecimal(consumeTextBuffer()), pos);
					case 'F':
						return new Token(TokenType.floatLiteral, Float.parseFloat(consumeTextBuffer()), pos);
					case 'D':
						return new Token(TokenType.doubleLiteral, Double.parseDouble(consumeTextBuffer()), pos);

					case 'E':
					case 'e':
						textBuffer.append(c);
						return readExponent();

					case '.':
						textBuffer.append(c);
						return readFraction();

					default:
						reset();
						return new Token(TokenType.integerLiteral, Integer.parseInt(consumeTextBuffer()), pos);
				}
			}
		}
	}

	private Token readString() throws IOException {

		while (true) {
			int val = read();

			if (val == -1)
				throw new IllegalStateException("unexpected end of string");

			char c = (char) val;

			switch (c) {
				case '\\':
					val = read();

					if (val == -1)
						throw new IllegalStateException("unexpected end of string");

					c = (char) val;

					if (c == '\'' || c == '\\')
						textBuffer.append(c);
					else
						throw new IllegalStateException("invalid escape '\\" + c + "' sequence at position " + pos);

					break;

				case '\'':
					Token token = new Token(TokenType.stringLiteral, textBuffer.toString(), pos);
					textBuffer.setLength(0);
					return token;

				default:
					textBuffer.append(c);
					break;
			}
		}
	}
}
