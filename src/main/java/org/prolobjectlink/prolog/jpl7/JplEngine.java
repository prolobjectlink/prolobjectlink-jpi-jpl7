/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2019 Prolobjectlink Project
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.prolobjectlink.prolog.jpl7;

import static org.prolobjectlink.prolog.PrologLogger.IO;

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

import javax.script.ScriptEngine;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.prolobjectlink.prolog.AbstractEngine;
import org.prolobjectlink.prolog.ArrayIterator;
import org.prolobjectlink.prolog.PrologClause;
import org.prolobjectlink.prolog.PrologEngine;
import org.prolobjectlink.prolog.PrologIndicator;
import org.prolobjectlink.prolog.PrologOperator;
import org.prolobjectlink.prolog.PrologProvider;
import org.prolobjectlink.prolog.PrologQuery;
import org.prolobjectlink.prolog.PrologScript;
import org.prolobjectlink.prolog.PrologTerm;
import org.prolobjectlink.prolog.PrologTermType;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
public abstract class JplEngine extends AbstractEngine implements PrologEngine {

	// used only for findall list result
	protected static final String KEY = "X";

	// JPL use for fact clauses true prolog term
	protected static final Term BODY = new Atom("true");

	// cache file in OS temporal directory
	private static String cache = null;

	// parser to obtain terms from text
	private final JplParser parser = new JplParser();

	// main memory prolog program
	protected JplProgram program = new JplProgram();

	// formulated string for < consult(cache), >
	private static String consultCacheComma;
	static {
		try {
			File f = File.createTempFile("prolobjectlink-jpi-jpl7-cache-", ".pl");
			cache = f.getCanonicalPath().replace(File.separatorChar, '/');
			consultCacheComma = "consult('" + cache + "'),";
		} catch (IOException e) {
			JplProvider.logger.error(JplEngine.class, IO, e);
		}
	}

	protected JplEngine(PrologProvider provider) {
		super(provider);
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
			getLogger().error(getClass(), IO + cache, e);
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
			buffer.append(i.next());
			if (i.hasNext()) {
				buffer.append(',');
			}
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
		new Query(consultCacheComma + "op(" + priority + "," + specifier + ", " + operator + ")").hasSolution();
	}

	public final boolean currentPredicate(String functor, int arity) {
		String x = functor;
		if (x.startsWith("'") && x.endsWith("'")) {
			x = x.substring(1, x.length() - 1);
		}
		return getPredicates().contains(new JplIndicator(x, arity));
	}

	public final boolean currentOperator(int priority, String specifier, String operator) {
		return new Query(consultCacheComma + "current_op(" + priority + "," + specifier + ", " + operator + ")")
				.hasSolution();
	}

	public final Set<PrologOperator> currentOperators() {
		Set<PrologOperator> operators = new HashSet<PrologOperator>();
		String opQuery = consultCacheComma + "findall(P/S/O,current_op(P,S,O)," + KEY + ")";
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
		query.close();
		return operators;
	}

	public final ScriptEngine getPrologScript() {
		return new PrologScript(new JplScriptFactory(this));
	}

	public final int getProgramSize() {
		return program.size();
	}

	public final Set<PrologIndicator> getPredicates() {
		Set<PrologIndicator> indicators = new HashSet<PrologIndicator>();
		String opQuery = consultCacheComma + "findall(X/Y,current_predicate(X/Y)," + KEY + ")";
		Query query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (Term t : termArray) {
				Term f = t.arg(1);
				Term a = t.arg(2);

				int arity = a.intValue();
				String functor = f.name();

				JplIndicator pi = new JplIndicator(functor, arity);
				indicators.add(pi);
			}
		}
		return indicators;
	}

	public final Set<PrologIndicator> getBuiltIns() {
		Set<PrologIndicator> indicators = new HashSet<PrologIndicator>();
		String opQuery = consultCacheComma + "findall(X/Y,current_predicate(X/Y)," + KEY + ")";
		Query query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (Term t : termArray) {
				Term f = t.arg(1);
				Term a = t.arg(2);

				int arity = a.intValue();
				String functor = f.name();

				JplIndicator pi = new JplIndicator(functor, arity);
				indicators.add(pi);
			}
		}
		Set<PrologClause> clauses = getProgramClauses();
		for (PrologClause prologClause : clauses) {
			PrologIndicator pi = prologClause.getPrologIndicator();
			if (indicators.contains(pi)) {
				indicators.remove(pi);
			}
		}
		return indicators;
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
				} else {
					cls.add(new JplClause(provider, toTerm(clause, PrologTerm.class), false, false, false));
				}
			}
		}
		return new PrologProgramIterator(cls);
	}

	public final void dispose() {
		File c = new File(cache);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(cache, false));
			writer.print("");
		} catch (FileNotFoundException e) {
			getLogger().error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		c.deleteOnExit();
		program.clear();
	}

	public final String getCache() {
		return cache;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program)) {
			return false;
		}
		return true;
	}

}
