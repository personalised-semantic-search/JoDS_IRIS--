<h1>RDFS Reasoner</h1>

<p>The <a href="http://tools.deri.org/rdfs-reasoner/">RDFS 
Reasoner</a> is a framework that enables reasoning over RDFS 
ontologies and that uses IRIS as underlying reasoning engine. 
</p>

<p>The RDFS Reasoner implementation is based on the work described in <a 
href="http://www.inf.unibz.it/~jdebruijn/publications-type/Bruijn-Heymans-RDF-Logi-07.html">
RDF and Logic: Reasoning and extension.</a>. The paper explores the 
various kinds of RDF entailment in F-Logic. It shows, amongst others, 
that the embeddings of simple, RDF and RDFS entailment fall in the 
Datalog fragment of F-Logic.
</p>

<h2>RDFS Reasoner and IRIS</h2>

<p>The RDFS Reasoner translates ontology descriptions in RDFS to 
F-Logic molecules and rules. It uses IRIS as underlying generic 
Datalog reasoning component and therefore uses a facade that 
mediates between its internal representation and the IRIS specific 
representation.
</p>

<p>The RDFS Reasoner currently supports the simple, RDF, RDFS and 
extensional RDFS (eRDFS) entailment regimes and allows WSML 
conjunctive queries as queries.
</p>

<h2>Download</h2>

<p>You can download a combined package containing both the RDFS Reasoner 
framework and IRIS at the <a href="http://tools.deri.org/rdfs-reasoner/v0.2/">
RDFS Reasoner website</a>.
