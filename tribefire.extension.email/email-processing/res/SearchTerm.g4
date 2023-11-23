grammar SearchTerm;

// Keywords


NOT:		'not';
AND:		'and';
OR:			'or';

// Operators. Comparation

EQ:			'=';
LT:			'<';
LE:			'<=';
GT:			'>';
GE:			'>=';
NE:			'!=';

    
searchTerm
	: negation 
	| conjunction 
	| disjunction 
	| from 
	| to 
	| cc 
	| bcc 
	| unread 
	| older 
	| younger 
	| recvdate 
	| sentdate 
	| body 
	| subject 
	;

negation
	: NOT '(' searchTerm ')'
	| NOT searchTerm
	; 

conjunction
	: '(' searchTerm (AND searchTerm)* ')'
	; 

disjunction
	: '(' searchTerm (OR searchTerm)* ')'
	; 

from
	: 'from' expression
	| 'from' '=' expression
	;

to
	: 'to' expression
	| 'to' '=' expression
	;

cc
	: 'cc' expression
	| 'cc' '=' expression
	;

bcc
	: 'bcc' expression
	| 'bcc' '=' expression
	;

unread
	: 'unread'
	;

older
	: 'older' decimal
	;

younger
	: 'younger' decimal
	;

recvdate
	: 'recvdate' operator expression
	;

sentdate
	: 'sentdate' operator expression
	;

body
	: 'body' expression
	| 'body' '=' expression
	;

subject
	: 'subject' expression
	| 'subject' '=' expression
	;
	
expression
	: STRING
	;

STRING
    : '"' ~('"')* '"'
    ;

DIGIT 
	: [0-9]
	;
	
decimal
	: DIGIT+
	;   

operator
    : EQ
    | LT
    | LE
    | GT
    | GE
    | NE
    ;

SPACE:	[ \t\r\n]+    -> channel(HIDDEN);

