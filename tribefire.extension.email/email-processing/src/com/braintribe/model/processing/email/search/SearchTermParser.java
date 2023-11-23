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
package com.braintribe.model.processing.email.search;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SearchTermParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, NOT=14, AND=15, OR=16, EQ=17, LT=18, 
		LE=19, GT=20, GE=21, NE=22, STRING=23, DIGIT=24, SPACE=25;
	public static final int
		RULE_searchTerm = 0, RULE_negation = 1, RULE_conjunction = 2, RULE_disjunction = 3, 
		RULE_from = 4, RULE_to = 5, RULE_cc = 6, RULE_bcc = 7, RULE_unread = 8, 
		RULE_older = 9, RULE_younger = 10, RULE_recvdate = 11, RULE_sentdate = 12, 
		RULE_body = 13, RULE_subject = 14, RULE_expression = 15, RULE_decimal = 16, 
		RULE_operator = 17;
	public static final String[] ruleNames = {
		"searchTerm", "negation", "conjunction", "disjunction", "from", "to", 
		"cc", "bcc", "unread", "older", "younger", "recvdate", "sentdate", "body", 
		"subject", "expression", "decimal", "operator"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'from'", "'to'", "'cc'", "'bcc'", "'unread'", "'older'", 
		"'younger'", "'recvdate'", "'sentdate'", "'body'", "'subject'", "'not'", 
		"'and'", "'or'", "'='", "'<'", "'<='", "'>'", "'>='", "'!='"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, "NOT", "AND", "OR", "EQ", "LT", "LE", "GT", "GE", "NE", "STRING", 
		"DIGIT", "SPACE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SearchTerm.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SearchTermParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class SearchTermContext extends ParserRuleContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public ConjunctionContext conjunction() {
			return getRuleContext(ConjunctionContext.class,0);
		}
		public DisjunctionContext disjunction() {
			return getRuleContext(DisjunctionContext.class,0);
		}
		public FromContext from() {
			return getRuleContext(FromContext.class,0);
		}
		public ToContext to() {
			return getRuleContext(ToContext.class,0);
		}
		public CcContext cc() {
			return getRuleContext(CcContext.class,0);
		}
		public BccContext bcc() {
			return getRuleContext(BccContext.class,0);
		}
		public UnreadContext unread() {
			return getRuleContext(UnreadContext.class,0);
		}
		public OlderContext older() {
			return getRuleContext(OlderContext.class,0);
		}
		public YoungerContext younger() {
			return getRuleContext(YoungerContext.class,0);
		}
		public RecvdateContext recvdate() {
			return getRuleContext(RecvdateContext.class,0);
		}
		public SentdateContext sentdate() {
			return getRuleContext(SentdateContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public SubjectContext subject() {
			return getRuleContext(SubjectContext.class,0);
		}
		public SearchTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchTerm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterSearchTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitSearchTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitSearchTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchTermContext searchTerm() throws RecognitionException {
		SearchTermContext _localctx = new SearchTermContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_searchTerm);
		try {
			setState(50);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				negation();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(37);
				conjunction();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(38);
				disjunction();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(39);
				from();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(40);
				to();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(41);
				cc();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(42);
				bcc();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(43);
				unread();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(44);
				older();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(45);
				younger();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(46);
				recvdate();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(47);
				sentdate();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(48);
				body();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(49);
				subject();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NegationContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(SearchTermParser.NOT, 0); }
		public SearchTermContext searchTerm() {
			return getRuleContext(SearchTermContext.class,0);
		}
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitNegation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitNegation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_negation);
		try {
			setState(59);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(52);
				match(NOT);
				setState(53);
				match(T__0);
				setState(54);
				searchTerm();
				setState(55);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				match(NOT);
				setState(58);
				searchTerm();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConjunctionContext extends ParserRuleContext {
		public List<SearchTermContext> searchTerm() {
			return getRuleContexts(SearchTermContext.class);
		}
		public SearchTermContext searchTerm(int i) {
			return getRuleContext(SearchTermContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(SearchTermParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(SearchTermParser.AND, i);
		}
		public ConjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitConjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitConjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConjunctionContext conjunction() throws RecognitionException {
		ConjunctionContext _localctx = new ConjunctionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_conjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			match(T__0);
			setState(62);
			searchTerm();
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(63);
				match(AND);
				setState(64);
				searchTerm();
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(70);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DisjunctionContext extends ParserRuleContext {
		public List<SearchTermContext> searchTerm() {
			return getRuleContexts(SearchTermContext.class);
		}
		public SearchTermContext searchTerm(int i) {
			return getRuleContext(SearchTermContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(SearchTermParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(SearchTermParser.OR, i);
		}
		public DisjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitDisjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitDisjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DisjunctionContext disjunction() throws RecognitionException {
		DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(T__0);
			setState(73);
			searchTerm();
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(74);
				match(OR);
				setState(75);
				searchTerm();
				}
				}
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(81);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_from; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterFrom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitFrom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromContext from() throws RecognitionException {
		FromContext _localctx = new FromContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_from);
		try {
			setState(88);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				match(T__2);
				setState(84);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				match(T__2);
				setState(86);
				match(EQ);
				setState(87);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ToContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ToContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_to; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterTo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitTo(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ToContext to() throws RecognitionException {
		ToContext _localctx = new ToContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_to);
		try {
			setState(95);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				match(T__3);
				setState(91);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				match(T__3);
				setState(93);
				match(EQ);
				setState(94);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CcContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterCc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitCc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitCc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CcContext cc() throws RecognitionException {
		CcContext _localctx = new CcContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_cc);
		try {
			setState(102);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				match(T__4);
				setState(98);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				match(T__4);
				setState(100);
				match(EQ);
				setState(101);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BccContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BccContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bcc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterBcc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitBcc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitBcc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BccContext bcc() throws RecognitionException {
		BccContext _localctx = new BccContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_bcc);
		try {
			setState(109);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(104);
				match(T__5);
				setState(105);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(106);
				match(T__5);
				setState(107);
				match(EQ);
				setState(108);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnreadContext extends ParserRuleContext {
		public UnreadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unread; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterUnread(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitUnread(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitUnread(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnreadContext unread() throws RecognitionException {
		UnreadContext _localctx = new UnreadContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_unread);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OlderContext extends ParserRuleContext {
		public DecimalContext decimal() {
			return getRuleContext(DecimalContext.class,0);
		}
		public OlderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_older; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterOlder(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitOlder(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitOlder(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OlderContext older() throws RecognitionException {
		OlderContext _localctx = new OlderContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_older);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(T__7);
			setState(114);
			decimal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YoungerContext extends ParserRuleContext {
		public DecimalContext decimal() {
			return getRuleContext(DecimalContext.class,0);
		}
		public YoungerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_younger; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterYounger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitYounger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitYounger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YoungerContext younger() throws RecognitionException {
		YoungerContext _localctx = new YoungerContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_younger);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			match(T__8);
			setState(117);
			decimal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RecvdateContext extends ParserRuleContext {
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public RecvdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recvdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterRecvdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitRecvdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitRecvdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecvdateContext recvdate() throws RecognitionException {
		RecvdateContext _localctx = new RecvdateContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_recvdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(T__9);
			setState(120);
			operator();
			setState(121);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SentdateContext extends ParserRuleContext {
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SentdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sentdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterSentdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitSentdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitSentdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SentdateContext sentdate() throws RecognitionException {
		SentdateContext _localctx = new SentdateContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sentdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(T__10);
			setState(124);
			operator();
			setState(125);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BodyContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_body);
		try {
			setState(132);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(127);
				match(T__11);
				setState(128);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(129);
				match(T__11);
				setState(130);
				match(EQ);
				setState(131);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubjectContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SubjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subject; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterSubject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitSubject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubjectContext subject() throws RecognitionException {
		SubjectContext _localctx = new SubjectContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_subject);
		try {
			setState(139);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(134);
				match(T__12);
				setState(135);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(136);
				match(T__12);
				setState(137);
				match(EQ);
				setState(138);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(SearchTermParser.STRING, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecimalContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(SearchTermParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(SearchTermParser.DIGIT, i);
		}
		public DecimalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decimal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterDecimal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitDecimal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitDecimal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DecimalContext decimal() throws RecognitionException {
		DecimalContext _localctx = new DecimalContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_decimal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(143);
				match(DIGIT);
				}
				}
				setState(146); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DIGIT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperatorContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(SearchTermParser.EQ, 0); }
		public TerminalNode LT() { return getToken(SearchTermParser.LT, 0); }
		public TerminalNode LE() { return getToken(SearchTermParser.LE, 0); }
		public TerminalNode GT() { return getToken(SearchTermParser.GT, 0); }
		public TerminalNode GE() { return getToken(SearchTermParser.GE, 0); }
		public TerminalNode NE() { return getToken(SearchTermParser.NE, 0); }
		public OperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).enterOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SearchTermListener ) ((SearchTermListener)listener).exitOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SearchTermVisitor ) return ((SearchTermVisitor<? extends T>)visitor).visitOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << LT) | (1L << LE) | (1L << GT) | (1L << GE) | (1L << NE))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\33\u0099\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2"+
		"\65\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3>\n\3\3\4\3\4\3\4\3\4\7\4D\n\4"+
		"\f\4\16\4G\13\4\3\4\3\4\3\5\3\5\3\5\3\5\7\5O\n\5\f\5\16\5R\13\5\3\5\3"+
		"\5\3\6\3\6\3\6\3\6\3\6\5\6[\n\6\3\7\3\7\3\7\3\7\3\7\5\7b\n\7\3\b\3\b\3"+
		"\b\3\b\3\b\5\bi\n\b\3\t\3\t\3\t\3\t\3\t\5\tp\n\t\3\n\3\n\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3"+
		"\17\5\17\u0087\n\17\3\20\3\20\3\20\3\20\3\20\5\20\u008e\n\20\3\21\3\21"+
		"\3\22\6\22\u0093\n\22\r\22\16\22\u0094\3\23\3\23\3\23\2\2\24\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$\2\3\3\2\23\30\u009d\2\64\3\2\2\2\4="+
		"\3\2\2\2\6?\3\2\2\2\bJ\3\2\2\2\nZ\3\2\2\2\fa\3\2\2\2\16h\3\2\2\2\20o\3"+
		"\2\2\2\22q\3\2\2\2\24s\3\2\2\2\26v\3\2\2\2\30y\3\2\2\2\32}\3\2\2\2\34"+
		"\u0086\3\2\2\2\36\u008d\3\2\2\2 \u008f\3\2\2\2\"\u0092\3\2\2\2$\u0096"+
		"\3\2\2\2&\65\5\4\3\2\'\65\5\6\4\2(\65\5\b\5\2)\65\5\n\6\2*\65\5\f\7\2"+
		"+\65\5\16\b\2,\65\5\20\t\2-\65\5\22\n\2.\65\5\24\13\2/\65\5\26\f\2\60"+
		"\65\5\30\r\2\61\65\5\32\16\2\62\65\5\34\17\2\63\65\5\36\20\2\64&\3\2\2"+
		"\2\64\'\3\2\2\2\64(\3\2\2\2\64)\3\2\2\2\64*\3\2\2\2\64+\3\2\2\2\64,\3"+
		"\2\2\2\64-\3\2\2\2\64.\3\2\2\2\64/\3\2\2\2\64\60\3\2\2\2\64\61\3\2\2\2"+
		"\64\62\3\2\2\2\64\63\3\2\2\2\65\3\3\2\2\2\66\67\7\20\2\2\678\7\3\2\28"+
		"9\5\2\2\29:\7\4\2\2:>\3\2\2\2;<\7\20\2\2<>\5\2\2\2=\66\3\2\2\2=;\3\2\2"+
		"\2>\5\3\2\2\2?@\7\3\2\2@E\5\2\2\2AB\7\21\2\2BD\5\2\2\2CA\3\2\2\2DG\3\2"+
		"\2\2EC\3\2\2\2EF\3\2\2\2FH\3\2\2\2GE\3\2\2\2HI\7\4\2\2I\7\3\2\2\2JK\7"+
		"\3\2\2KP\5\2\2\2LM\7\22\2\2MO\5\2\2\2NL\3\2\2\2OR\3\2\2\2PN\3\2\2\2PQ"+
		"\3\2\2\2QS\3\2\2\2RP\3\2\2\2ST\7\4\2\2T\t\3\2\2\2UV\7\5\2\2V[\5 \21\2"+
		"WX\7\5\2\2XY\7\23\2\2Y[\5 \21\2ZU\3\2\2\2ZW\3\2\2\2[\13\3\2\2\2\\]\7\6"+
		"\2\2]b\5 \21\2^_\7\6\2\2_`\7\23\2\2`b\5 \21\2a\\\3\2\2\2a^\3\2\2\2b\r"+
		"\3\2\2\2cd\7\7\2\2di\5 \21\2ef\7\7\2\2fg\7\23\2\2gi\5 \21\2hc\3\2\2\2"+
		"he\3\2\2\2i\17\3\2\2\2jk\7\b\2\2kp\5 \21\2lm\7\b\2\2mn\7\23\2\2np\5 \21"+
		"\2oj\3\2\2\2ol\3\2\2\2p\21\3\2\2\2qr\7\t\2\2r\23\3\2\2\2st\7\n\2\2tu\5"+
		"\"\22\2u\25\3\2\2\2vw\7\13\2\2wx\5\"\22\2x\27\3\2\2\2yz\7\f\2\2z{\5$\23"+
		"\2{|\5 \21\2|\31\3\2\2\2}~\7\r\2\2~\177\5$\23\2\177\u0080\5 \21\2\u0080"+
		"\33\3\2\2\2\u0081\u0082\7\16\2\2\u0082\u0087\5 \21\2\u0083\u0084\7\16"+
		"\2\2\u0084\u0085\7\23\2\2\u0085\u0087\5 \21\2\u0086\u0081\3\2\2\2\u0086"+
		"\u0083\3\2\2\2\u0087\35\3\2\2\2\u0088\u0089\7\17\2\2\u0089\u008e\5 \21"+
		"\2\u008a\u008b\7\17\2\2\u008b\u008c\7\23\2\2\u008c\u008e\5 \21\2\u008d"+
		"\u0088\3\2\2\2\u008d\u008a\3\2\2\2\u008e\37\3\2\2\2\u008f\u0090\7\31\2"+
		"\2\u0090!\3\2\2\2\u0091\u0093\7\32\2\2\u0092\u0091\3\2\2\2\u0093\u0094"+
		"\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095#\3\2\2\2\u0096"+
		"\u0097\t\2\2\2\u0097%\3\2\2\2\r\64=EPZaho\u0086\u008d\u0094";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}