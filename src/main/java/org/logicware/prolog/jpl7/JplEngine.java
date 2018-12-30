/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2017 WorkLogic Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.logicware.prolog.jpl7;

import static org.logicware.logging.LoggerConstants.IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.ArrayIterator;
import org.logicware.logging.LoggerUtils;
import org.logicware.prolog.AbstractEngine;
import org.logicware.prolog.PredicateIndicator;
import org.logicware.prolog.PrologClause;
import org.logicware.prolog.PrologEngine;
import org.logicware.prolog.PrologIndicator;
import org.logicware.prolog.PrologOperator;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologQuery;
import org.logicware.prolog.PrologTerm;
import org.logicware.prolog.PrologTermType;

public abstract class JplEngine extends AbstractEngine implements PrologEngine {

	// used only for findall list result
	protected static final String KEY = "X";

	// JPL use for fact clauses true prolog term
	protected static final Term BODY = new Atom("true");

	// cache file in OS temporal directory
	private static String cache = null;

	// absolute location of cache in OS
	private String location;

	// formulated string for consult(f),
	private String consultCacheFileAnd;

	// parser to obtain terms from text
	private final JplParser parser = new JplParser();

	// main memory prolog program
	protected JplProgram program = new JplProgram();

	static {
		try {
			File f = File.createTempFile("prolobjectlink-jpi-jpl7-cache-", ".pl");
			cache = f.getCanonicalPath().replace(File.separatorChar, '/');
		} catch (IOException e) {
			LoggerUtils.error(JplEngine.class, IO, e);
		}
	}

	private void initialization() {
		File pf = new File(cache);
		location = pf.getAbsolutePath();
		location = location.toLowerCase();
		location = location.replace(File.separatorChar, '/');
		String consultCacheFile = "consult('" + cache + "')";
		consultCacheFileAnd = consultCacheFile + ",";
	}

	private Set<PrologIndicator> predicates() {
		Set<PrologIndicator> indicators = new HashSet<PrologIndicator>();
		String opQuery = consultCacheFileAnd + "findall(X/Y,current_predicate(X/Y)," + KEY + ")";
		Query query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (Term t : termArray) {
				Term f = t.arg(1);
				Term a = t.arg(2);

				int arity = a.intValue();
				String functor = f.name();

				PredicateIndicator pi = new PredicateIndicator(functor, arity);
				indicators.add(pi);
			}
		}
		return indicators;
	}

	protected JplEngine(PrologProvider provider) {
		super(provider);
		initialization();
		consult(cache);
	}

	protected JplEngine(PrologProvider provider, String path) {
		super(provider);
		initialization();
		consult(path);
	}

	public final Iterator<PrologClause> iterator() {
		List<PrologClause> cls = new ArrayList<PrologClause>();
		for (List<Term> family : program.getClauses().values()) {
			for (Term clause : family) {
				if (clause.hasFunctor(":-", 2)) {
					PrologTerm head = toTerm(clause.arg(1), PrologTerm.class);
					PrologTerm body = toTerm(clause.arg(2), PrologTerm.class);
					if (body.getType() != PrologTermType.TRUE_TYPE) {
						cls.add(new JplClause(provider, head, body, false, false, false));
					} else {
						cls.add(new JplClause(provider, head, false, false, false));
					}
				}
			}
		}
		return new PrologProgramIterator(cls);
	}

	public final void include(String path) {
		program.add(parser.parseProgram(path));
		persist(cache);
	}

	public final void consult(String path) {
		program = parser.parseProgram(path);
		persist(cache);
	}

	public final void persist(String path) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(path, false));
			writer.print(program);
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public final void abolish(String functor, int arity) {
		program.removeAll(functor, arity);
		persist(cache);
	}

	public final void asserta(String stringClause) {
		asserta(Util.textToTerm(stringClause));
	}

	public final void asserta(PrologTerm head, PrologTerm... body) {
		asserta(fromTerm(head, body, Term.class));
	}

	private void asserta(Term t) {
		program.push(t);
		persist(cache);
	}

	public final void assertz(String stringClause) {
		assertz(Util.textToTerm(stringClause));
	}

	public final void assertz(PrologTerm head, PrologTerm... body) {
		assertz(fromTerm(head, body, Term.class));
	}

	private void assertz(Term t) {
		program.add(t);
		persist(cache);
	}

	public final boolean clause(String stringClause) {
		return clause(Util.textToTerm(stringClause));
	}

	public final boolean clause(PrologTerm head, PrologTerm... body) {
		return clause(fromTerm(head, body, Term.class));
	}

	private boolean clause(Term t) {
		Term h = t;
		Term b = BODY;
		if (t.hasFunctor(":-", 2)) {
			h = t.arg(1);
			b = t.arg(2);
		}
		return new JplQuery(

				this, cache, "clause(" + h + "," + b + ")"

		).hasSolution();
	}

	public final void retract(String stringClause) {
		retract(Util.textToTerm(stringClause));
	}

	public final void retract(PrologTerm head, PrologTerm... body) {
		retract(provider.fromTerm(head, body, Term.class));
	}

	private void retract(Term t) {
		program.remove(t);
		persist(cache);
	}

	public final PrologQuery query(String stringQuery) {
		return new JplQuery(this, cache, stringQuery);
	}

	public final PrologQuery query(PrologTerm[] terms) {
		Iterator<PrologTerm> i = new ArrayIterator<PrologTerm>(terms);
		StringBuilder buffer = new StringBuilder();
		while (i.hasNext()) {
			buffer.append(',');
			buffer.append(i.next());
		}
		buffer.append(".");
		return query("" + buffer + "");
	}

	public final PrologQuery query(PrologTerm term, PrologTerm... terms) {
		Iterator<PrologTerm> i = new ArrayIterator<PrologTerm>(terms);
		StringBuilder buffer = new StringBuilder();
		buffer.append("" + term + "");
		while (i.hasNext()) {
			buffer.append(',');
			buffer.append(i.next());
		}
		buffer.append(".");
		return query("" + buffer + "");
	}

	public final void operator(int priority, String specifier, String operator) {
		new Query(consultCacheFileAnd + "op(" + priority + "," + specifier + ", " + operator + ")").hasSolution();
	}

	public final boolean currentPredicate(String functor, int arity) {
		return new Query(consultCacheFileAnd + "current_predicate(" + functor + "/" + arity + ")").hasSolution();
	}

	public final boolean currentOperator(int priority, String specifier, String operator) {
		return new Query(consultCacheFileAnd + "current_op(" + priority + "," + specifier + ", " + operator + ")")
				.hasSolution();
	}

	public final Set<PrologOperator> currentOperators() {
		Set<PrologOperator> operators = new HashSet<PrologOperator>();
		String opQuery = consultCacheFileAnd + "findall(P/S/O,current_op(P,S,O)," + KEY + ")";
		Query query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (Term t : termArray) {
				Term prio = t.arg(1).arg(1);
				Term pos = t.arg(1).arg(2);
				Term op = t.arg(2);

				int p = prio.intValue();
				String s = pos.name();
				String n = op.name();

				PrologOperator o = new JplOperator(p, s, n);
				operators.add(o);
			}
		}
		return operators;
	}

	public final int getProgramSize() {
		return program.size();
	}

	public final Set<PrologIndicator> getPredicates() {
		Set<PrologIndicator> pis = predicates();
		Set<PrologIndicator> builtins = getBuiltIns();
		for (PrologIndicator prologIndicator : pis) {
			if (!builtins.contains(prologIndicator)) {
				pis.remove(prologIndicator);
			}
		}
		return pis;
	}

	public final Set<PrologIndicator> getBuiltIns() {
		Set<PrologIndicator> pis = predicates();
		Set<PrologClause> clauses = getProgramClauses();
		for (PrologClause prologClause : clauses) {
			PrologIndicator pi = prologClause.getPrologIndicator();
			if (pis.contains(pi)) {
				pis.remove(pi);
			}
		}
		return pis;
	}

	public final void dispose() {
		File c = new File(cache);
		c.deleteOnExit();
	}

	public final String getCache() {
		return cache;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JplEngine other = (JplEngine) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location)) {
			return false;
		}
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program)) {
			return false;
		}
		return true;
	}

}
