// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe-IT Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================
lexer grammar GmqlLexer;

@header {
package com.braintribe.model.processing.query.parser.impl.autogenerated;
}

// most specific rules first, then general rules afterwards


// --------------------------Keywords-------------------

Boolean: T R U E | F A L S E;
Entity:	R E F E R E N C E;
TypeSignature: T Y P E S I G N A T U R E;
Enum: E N U M;

Select:	S E L E C T;
Distinct: D I S T I N C T;
From: F R O M;
Join: J O I N;
Full: F U L L;
Right: R I G H T;
Left: L E F T;
Where: W H E R E;
Null: N U L L;
Limit: L I M I T;
Offset: O F F S E T;
OrderBy: O R D E R ' ' B Y;
Ascending: A S C;
Descending: D E S C;
GroupBy: G R O U P ' ' B Y;
Having: H A V I N G;
Property: P R O P E R T Y;
Of: O F;

//--------------------Aggregate functions-------------

AggCount: C O U N T;
AggAvg: A V G;
AggMin: M I N;
AggMax: M A X;
AggSum: S U M;

//-----------------------Join Functions--------------

ListIndex: L I S T I N D E X;
MapKey:	M A P K E Y;

//---------------------------String Functions---------

Lower: L O W E R;
Upper: U P P E R;
ToStringToken: T O S T R I N G;
Concatenation: C O N C A T E N A T I O N;
Localize: L O C A L I Z E;
Username: U S E R N A M E;

Star: '*';
Colon: ':';

//-------------------Boolean Functions----------------

FullText: F U L L T E X T;

//------------------Time Functions-------------------

Now: N O W;

//----------------------------Operators----------------

// --------------------------Boolean Operators---------

And: A N D;
Not: N O T;
Or: O R;

//--------------------------Comparison Operators------------

Operator
:
	'='
	| '!='
	| '>'
	| '>='
	| '<'
	| '<='
	| L I K E
	| I L I K E
	| I N
	| C O N T A I N S
;

//---------------------------Symbols-------------------

LB: '(';
RB: ')';
LSB: '[';
RSB: ']';
LCB: '{';
RCB: '}';
COMMA: ',';
Dot: '.';

//----------------------------Identifier-----------------

StandardIdentifier
:
	IdentifierFirstCharacter
	(
		IdentifierFirstCharacter
		| Digit
	)*
;

EscapedIdentifier
:
	EscapedIdentifierQuote -> pushMode ( EscapedIdentifierMode )
;

fragment IdentifierFirstCharacter: Char	| UnderScore | DollarSign;
fragment Char: 'a' .. 'z' | 'A' .. 'Z';
fragment UnderScore: '_';
fragment DollarSign: '$';

//----------------------------Date Literal---------------

DateFunction: 'date' LB;

DateOffset
:
	YearFragment
	| MonthFragment
	| DayFragment
	| HourFragment
	| MinuteFragment
	| SecondFragment
	| MilliSecondFragment
;

TimeZoneOffset:	ZoneFragment;

fragment YearFragment: Digit+ 'Y';
fragment MonthFragment:	Digit+ 'M';
fragment DayFragment: Digit+ 'D';
fragment HourFragment: Digit+ 'H';
fragment MinuteFragment: Digit+ 'm';
fragment SecondFragment: Digit+ 'S';
fragment MilliSecondFragment: Digit+ 's';

fragment ZoneFragment:
	PlusOrMinus? Digit Digit Digit Digit 'Z' // format always hhmm with a + or - before it
;

//----------------------------Base Literals--------------

fragment DecimalSuffix: B;
fragment FloatSuffix: F;
fragment DoubleSuffix: D;


DecimalLiteral:	( FloatingPointLiteral | IntegerBase10Literal ) DecimalSuffix ;
FloatLiteral:   ( FloatingPointLiteral | IntegerBase10Literal | FloatingPointSpecial) FloatSuffix;

DoubleLiteral:
	FloatingPointLiteral DoubleSuffix?
	| IntegerBase10Literal DoubleSuffix
	| FloatingPointSpecial DoubleSuffix
;

fragment FloatingPointSpecial: '+NaN' | PlusOrMinus 'Infinity';

fragment FloatingPointLiteral:
	PlusOrMinus?
	(
		Digit+ Dot Digit* Exponent?
		| Dot Digit+ Exponent?
		| Digit+ Exponent
	)
;

fragment Exponent: ExponentIndicator PlusOrMinus? Digit+;
fragment ExponentIndicator: E;

LongBase16Literal: IntegerBase16Literal LongSuffix;
LongBase10Literal: IntegerBase10Literal LongSuffix;

IntegerBase16Literal:
	PlusOrMinus? ZeroDigit
	(
		'x'
		| 'X'
	) HexDigit+
;

IntegerBase10Literal:
	PlusOrMinus?
	(
		Digit+
	)
;

fragment HexDigit: Digit | 'a' .. 'f' | 'A' .. 'F';
fragment LongSuffix: L;
fragment Digit:	ZeroDigit | PositiveDigit;
fragment PositiveDigit: '1' .. '9';
fragment ZeroDigit: '0';
fragment PlusOrMinus: '+' | '-';

StringOpen:
	EscapedQuote -> pushMode ( InsideString )
;

//---------------------------- Alphabet for case-insensitive keywords ----------------

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

//----------------------------White space----------------
////fix WS !?

WS:
	[ \t\f\r\n]+ -> skip
; // skip spaces, tabs, newlines

//----------------------------String Mode----------------
mode InsideString;

fragment EscapedQuote: '\'';
fragment BackSlash:	'\\';

UnicodeEscape:
	BackSlash 'u' HexDigit HexDigit HexDigit HexDigit
;

EscB: BackSlash 'b';
EscT: BackSlash 't';
EscN: BackSlash 'n';
EscF: BackSlash 'f';
EscR: BackSlash 'r';
EscSQ: BackSlash '\'';
EscBS: BackSlash BackSlash;

PlainContent
:
	(
		~( '\'' | '\\' )
	)+
;

StringClose
:
	EscapedQuote -> popMode
;

//----------------------------EscapedIdentifier Mode----------------
mode EscapedIdentifierMode;

fragment EscapedIdentifierQuote: '"';

//=====================================
//*************IMPORTANT***************
//=====================================
// Whenever this list needs to be updated, changes have to be made to the static list in GmqlQueryParserImpl
// Both lists MUST be synchronized.

KeyWord
:
	Entity
	| TypeSignature
	| Enum
	| Where
	| From
	| Limit
	| Offset
	| Ascending
	| Descending
	| Distinct
	| Property
	| Of
	| Select
	| Join
	| Full
	| Right
	| Left
	| AggCount
	| AggAvg
	| AggMin
	| AggMax
	| AggSum
	| Having
	| ListIndex
	| MapKey
	| Lower
	| Upper
	| ToStringToken
	| Concatenation
	| Localize
	| Username
	| FullText
	| Now
	| And
	| Not
	| Or
;

EscapedIdentifierClose
:
	EscapedIdentifierQuote -> popMode
;
