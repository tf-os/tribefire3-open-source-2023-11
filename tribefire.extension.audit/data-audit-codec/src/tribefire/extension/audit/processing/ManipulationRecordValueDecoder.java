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
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

import tribefire.extension.audit.processing.ManipulationRecordValueLexer.Token;
import tribefire.extension.audit.processing.ManipulationRecordValueLexer.TokenType;

public class ManipulationRecordValueDecoder {
	private ManipulationRecordValueLexer lexer;
	private Token bufferedToken;
	
	private ManipulationRecordValueDecoder(ManipulationRecordValueLexer lexer) {
		super();
		this.lexer = lexer;
	}

	private Token nextToken() throws IOException {
		if (bufferedToken != null) {
			Token token = bufferedToken;
			bufferedToken = null;
			return token;
		}
		
		return lexer.readNextToken();
	}
	
	public Object parse() throws IOException {
		Object value = readValue();
		
		Token token = lexer.readNextToken();
		
		if (token.getType() != TokenType.end)
			throw new IllegalStateException("unexpected content at pos: " + token.getPos());
		
		return value;
	}
	
	private Object readValue() throws IOException {
		return readValue(nextToken());
	}
	
	private String readString() throws IOException {
		return readExpected(TokenType.stringLiteral).getValue();
	}
	
	private Object readValue(Token token) throws IOException {
		switch (token.getType()) {
		case booleanLiteral:
		case dateliteral:
		case decimalLiteral:
		case doubleLiteral:
		case floatLiteral:
		case integerLiteral:
		case longLiteral:
		case stringLiteral:
		case nullLiteral:
			return token.getValue();
			
		case identifier:
			bufferedToken = token;
			return readCustomTypeValue(false);
		case tilde: 
			return readCustomTypeValue(true);

		case openBracket: return readList();
		case openCurlyBracket: return readMap();
		case openParanthesis: return readSet();

		default:
			throw new IllegalStateException("unexpected token type [" + token.getType() + "] at pos [" + token.getPos() + "]");
		}
	}
	
	
	private Object readSet() throws IOException {
		return readCollection(new LinkedHashSet<>(), TokenType.closeParanthesis);
	}

	private Object readMap() throws IOException {
		Map<Object, Object> map = new LinkedHashMap<>();
		
		Token token = nextToken();
		
		if (token.getType() == TokenType.closeCurlyBracket)
			return map;
		
		bufferedToken = token;
		
		while (true) {
			Object key = readValue();
			readExpected(TokenType.colon);
			Object value = readValue();
			
			map.put(key, value);
			
			token = nextToken();
			
			if (token.getType() == TokenType.closeCurlyBracket) {
				return map;
			}
			else if (token.getType() != TokenType.comma) {
				throw new IllegalStateException("unexpected token [" + token.getType() +"] at pos [" + token.getPos() + "]");
			}
		}
	}

	private Token readExpected(TokenType tokenType) throws IOException {
		Token token = nextToken();
		
		if (token.getType() != tokenType) {
			throw new IllegalStateException("unexpected token [" + token.getType() +"] at pos [" + token.getPos() + "]");
		}
		
		return token;
	}


	private Object readList() throws IOException {
		return readCollection(new ArrayList<>(), TokenType.closeBracket);
	}
	
	private Object readCollection(Collection<Object> collection, TokenType closeToken) throws IOException {
		Token token = nextToken();
		
		if (token.getType() == closeToken)
			return collection;
		
		bufferedToken = token;
		
		while (true) {
			collection.add(readValue());
			
			token = nextToken();
			
			if (token.getType() == closeToken) {
				return collection;
			}
			else if (token.getType() != TokenType.comma) {
				throw new IllegalStateException("unexpected token [" + token.getType() +"] at pos [" + token.getPos() + "]");
			}
		}
	}
	
	private Object readCustomTypeValue(boolean preliminaryReference) throws IOException {
		String typeSignature = readQualifiedIdentifier();
		
		Token token = nextToken();
		
		switch (token.getType()) {
		case arrow:
			if (preliminaryReference)
				break;

			// enum value
			String identifier = readExpected(TokenType.identifier).getValue();
			return GMF.getTypeReflection().getEnumType(typeSignature).instanceFromString(identifier);
			
		case openBracket:
			EntityReference reference = preliminaryReference? // 
					PreliminaryEntityReference.T.create(): //
					PersistentEntityReference.T.create();
			
			// entity reference value
			Object id = readValue();
			reference.setRefId(id);
			reference.setTypeSignature(typeSignature);
			
			Token delimiterToken = nextToken();
			
			switch (delimiterToken.getType()) {
			case comma:
				String partition = readString();
				reference.setRefPartition(partition);
				readExpected(TokenType.closeBracket);
				break;
				
			case closeBracket:
				break;
				
			default:
				throw new IllegalStateException("unexpected token [" + delimiterToken.getType() +"] at pos [" + delimiterToken.getPos() + "]");
			}
			
			return reference;
			
		default:
			break;
		}
		
		throw new IllegalStateException("unexpected token [" + token.getType() +"] at pos [" + token.getPos() + "]");
	}
	
	private String readQualifiedIdentifier() throws IOException {
		StringBuilder builder = new StringBuilder();
		
		while (true) {
			Token token = readExpected(TokenType.identifier);

			String qualifiedIdentifierPart = token.getValue();
			builder.append(qualifiedIdentifierPart);

			token = nextToken();
			
			if (token.getType() != TokenType.dot) {
				bufferedToken = token;
				return builder.toString();
			}
			
			builder.append('.');
		}
	}

	public static <T> T parse(String s) {
		try {
			return parse(new StringReader(s));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static <T> T parse(Reader reader) throws IOException {
		ManipulationRecordValueLexer lexer = new ManipulationRecordValueLexer(reader);
		return (T)new ManipulationRecordValueDecoder(lexer).parse();
	}
}
