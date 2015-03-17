<h1>Safe Rules</h1>

<p>
	The algorithm for detecting unsafe rules is now located in a dedicated
	class: RuleValidator. This algorithm is taken from Principles of Database and
	Knowledgebase Systems, Ullman, page 105:
</p>

<p>
	A rule is considered safe if all variables are limited.
</p>

<p>
	A variable is limited if:
	<ul>
		<li>It appears in a positive ordinary predicate</li>
		<li>It appears in a positive equality with a constant, e.g. ?X = 'a'</li>
		<li>It appears in a positive equality with another variable known to be limited, e.g. ?X = ?Y, ?Y = 'a'</li>
	</ul>
<p>
	However, rule validation in IRIS can be parameterised to allow the relaxation
	of two aspects of this algorithm, specifically:
	<ul>
		<li>
			variables that ONLY appear in a negated ordinary predicate (and nowhere
			else) can still make for a safe rule, because such a rule can be re-written to
			move the negated sub-goal to a separate rule, see the example in Ullman,
			page 129-130
		</li>
		<li>
			Furthermore, variables that appear in arithmetic predicates can also be considered limited if
			all the other variables are known to be limited, e.g.
			?X + ?Y = ?Z, ?X = 3, ?Z = 4, implies that ?Y is also limited
		</li>
	</ul>
</p>

<p>
	These two relaxations of the definition of a safe rule are configurable (on/off) in the RuleValidator class.
</p>

<p>
	If an unsafe rule is detected during the evaluation of a logic program then a RuleUnsafeException is thrown
	containing details of why the rule is considered unsafe.
</p>
