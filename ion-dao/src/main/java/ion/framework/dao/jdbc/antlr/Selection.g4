grammar Selection;

fragment Alpha: (('a'..'z') | ('A'..'Z') | '_');
fragment Alphanumeric: (Alpha|('0'..'9'));

SELECT:'SELECT'|'select';
FROM:'FROM'|'from';
WHERE:'WHERE'|'where';
AS:'AS';
Attribute: (Alphanumeric | '.')+;
Value: Alpha Alphanumeric+;
Condition: '<'|'>'|'='|'!='|'<='|'>='|'in';
Selection: '';
COMMA: ',';
WS: (' ' | '\t')+;

statement: query* EOF;

query
	: selectClause WS fromClause WS whereClause
	| fromClause WS whereClause
	| selectClause WS fromClause
	| fromClause
	;

selectClause
	: SELECT WS selectAttribute+;

selectAttribute
	: attribute=(Attribute | '*')
	| attribute=(Attribute | '*') COMMA
	| attribute=(Attribute | '*') WS? COMMA WS?
	;

fromClause
	: FROM WS className=Attribute WS classNameAlias=Attribute
	| FROM WS className=Attribute
	;

whereClause
	: WHERE WS conditions+;

conditions
	: condition
	| condition COMMA
	| condition WS? COMMA WS?
	;

condition
	: condAttr=Attribute WS condType=Condition WS ':'condValue=Value
	| condAttr=Attribute condType=Condition ':'condValue=Value
	| condAttr=Attribute WS condType=('in'|'IN') WS '(' values+ ')'
	;
	
values
	: ':'condValue=Value
	| ':'condValue=Value COMMA
	| ':'condValue=Value WS? COMMA WS?
	| query
	;