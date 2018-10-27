/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2017 Logicware Project
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

import static org.logicware.logging.LoggerConstants.FILE_NOT_FOUND;
import static org.logicware.logging.LoggerConstants.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jpl7.Atom;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.ArrayIterator;
import org.logicware.Licenses;
import org.logicware.logging.LoggerUtils;
import org.logicware.prolog.AbstractEngine;
import org.logicware.prolog.OperatorEntry;
import org.logicware.prolog.PredicateIndicator;
import org.logicware.prolog.PrologClause;
import org.logicware.prolog.PrologEngine;
import org.logicware.prolog.PrologIndicator;
import org.logicware.prolog.PrologOperator;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologQuery;
import org.logicware.prolog.PrologTerm;

public abstract class JplEngine extends AbstractEngine implements PrologEngine {

	protected Query query;

	// used only for findall list result
	protected static final String KEY = "X";

	// JPL use for fact clauses true prolog term
	protected static final Term BODY = new Atom("true");

	// cache file in OS temporal directory
	private static String cache = null;

	// absolute location of cache in OS
	private String location;

	// formulated string for consult(f)
	private String consultCacheFile;

	// formulated string for consult(f),
	private String consultCacheFileAnd;

	static {
		try {
			File f = File.createTempFile("prolobjectlink-jpi-jpl7-cache-", ".pl");
			cache = f.getCanonicalPath().replace(File.separatorChar, '/');
		} catch (IOException e) {
			LoggerUtils.error(JplEngine.class, IO, e);
		}
		LoggerUtils.info(JplEngine.class, cache);
	}

	private void initialization() {
		File pf = new File(cache);
		location = pf.getAbsolutePath();
		location = location.toLowerCase();
		location = location.replace(File.separatorChar, '/');
		consultCacheFile = "consult('" + cache + "')";
		consultCacheFileAnd = consultCacheFile + ",";
	}

	private void open(String path, boolean append) {

		FileReader reader = null;
		PrintWriter writer = null;
		BufferedReader buffer = null;

		try {
			reader = new FileReader(path);
			writer = new PrintWriter(new FileOutputStream(cache, append));
			buffer = new BufferedReader(reader);
			String line = buffer.readLine();
			while (line != null) {
				/*
				 * TODO We need a dedicated variable scanner for save variable name because SWI
				 * lost original variables names in main memory
				 */
				writer.append(line);
				writer.append('\n');
				line = buffer.readLine();
			}
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), FILE_NOT_FOUND, e);
		} catch (IOException e) {
			LoggerUtils.error(getClass(), IO + path, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO + path, e);
				}
			}
			if (writer != null) {
				writer.close();
			}
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO + path, e);
				}
			}
		}

		query = new Query(consultCacheFile);

	}

	private synchronized Set<PrologIndicator> predicates() {
		Set<PrologIndicator> indicators = new HashSet<PrologIndicator>();
		String opQuery = consultCacheFileAnd + "findall(X/Y,current_predicate(X/Y)," + KEY + ")";
		query = new Query(opQuery);
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

	private void writeExcept(Iterator<PrologClause> i, PrintWriter writer, Term t) {
		
		Term termHead = t;
		Term termBody = BODY;
		if (t.hasFunctor(":-", 2)) {
			termHead = t.arg(1);
			termBody = t.arg(2);
		}

		PrologTerm th = toTerm(termHead, PrologTerm.class);
		PrologTerm tb = toTerm(termBody, PrologTerm.class);

		// copy all except current term
		while (i.hasNext()) {
			PrologClause c = i.next();
			PrologTerm head = c.getHead();
			PrologTerm body = c.getBody();
			if (body == null) {
				body = provider.prologTrue();
			}

			// using unify because equals is a strong condition
			if (!(th.unify(head) && tb.unify(body))) {
				writer.append("" + c + "");
				writer.append('\n');
			}
		}

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

	protected abstract Iterator<PrologClause> iterator(String path);

	public final synchronized void consult(String path) {
		open(path, false);
	}

	public final synchronized void include(String path) {
		open(path, true);
	}

	public final synchronized void abolish(String functor, int arity) {
		PrintWriter writer = null;
		try {
			Iterator<PrologClause> i = iterator();
			writer = new PrintWriter(new FileOutputStream(cache, false));
			while (i.hasNext()) {
				PrologClause c = i.next();
				if (!c.getIndicator().equals(functor + "/" + arity)) {
					writer.append("" + c + "");
					writer.append('\n');
				}
			}
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public final synchronized void persist(String path) {
		PrintWriter writer = null;
		try {
			Iterator<PrologClause> i = iterator();
			writer = new PrintWriter(new FileOutputStream(path, false));
			while (i.hasNext()) {
				PrologClause c = i.next();
				writer.append("" + c + "");
				writer.append("\n");
			}
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public final synchronized void asserta(String stringClause) {
		asserta(Util.textToTerm(stringClause));
	}

	public final synchronized void asserta(PrologTerm head, PrologTerm... body) {
		asserta(fromTerm(head, body, Term.class));
	}

	private synchronized void asserta(Term t) {

		PrintWriter writer = null;
		try {
			Iterator<PrologClause> i = iterator();
			writer = new PrintWriter(new FileOutputStream(cache, false));

			// add first
			writer.append("" + t + "");
			writer.append('.');
			writer.append('\n');

			writeExcept(i, writer, t);

		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}

	public final synchronized void assertz(String stringClause) {
		assertz(Util.textToTerm(stringClause));
	}

	public final synchronized void assertz(PrologTerm head, PrologTerm... body) {
		assertz(fromTerm(head, body, Term.class));
	}

	private synchronized void assertz(Term t) {

		Term termHead = t;
		Term termBody = BODY;
		if (t.hasFunctor(":-", 2)) {
			termHead = t.arg(1);
			termBody = t.arg(2);
		}

		PrologTerm th = toTerm(termHead, PrologTerm.class);
		PrologTerm tb = toTerm(termBody, PrologTerm.class);

		PrintWriter writer = null;
		try {
			Iterator<PrologClause> i = iterator();
			writer = new PrintWriter(new FileOutputStream(cache, true));
			while (i.hasNext()) {
				PrologClause c = i.next();
				PrologTerm head = c.getHead();
				PrologTerm body = c.getBody();
				if (body == null) {
					body = provider.prologTrue();
				}

				// using unify because equals is a strong condition
				if (th.unify(head) && tb.unify(body)) {
					return;
				}
			}
			writer.append("" + t + "");
			writer.append('.');
			writer.append('\n');
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public final synchronized boolean clause(String stringClause) {
		return clause(Util.textToTerm(stringClause));
	}

	public final synchronized boolean clause(PrologTerm head, PrologTerm... body) {
		return clause(fromTerm(head, body, Term.class));
	}

	private synchronized boolean clause(Term t) {
		Term h = t;
		Term b = BODY;
		if (t.hasFunctor(":-", 2)) {
			h = t.arg(1);
			b = t.arg(2);
		}
		query = new Query("" +

				consultCacheFileAnd +

				"clause(" + h + "," + b + ")"

		);
		return query.hasSolution();
	}

	public final synchronized void retract(String stringClause) {
		retract(Util.textToTerm(stringClause));
	}

	public final synchronized void retract(PrologTerm head, PrologTerm... body) {
		retract(provider.fromTerm(head, body, Term.class));
	}

	private synchronized void retract(Term t) {

		PrintWriter writer = null;
		try {
			Iterator<PrologClause> i = iterator();
			writer = new PrintWriter(new FileOutputStream(cache, false));
			writeExcept(i, writer, t);
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), IO + cache, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public final synchronized PrologQuery query(String stringQuery) {
		return new JplQuery(this, cache, stringQuery);
	}

	public final synchronized PrologQuery query(PrologTerm[] terms) {
		Iterator<PrologTerm> i = new ArrayIterator<PrologTerm>(terms);
		StringBuilder buffer = new StringBuilder();
		while (i.hasNext()) {
			buffer.append(',');
			buffer.append(i.next());
		}
		buffer.append(".");
		return query("" + buffer + "");
	}

	public final synchronized PrologQuery query(PrologTerm term, PrologTerm... terms) {
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

	public final synchronized void operator(int priority, String specifier, String operator) {
		new Query(consultCacheFileAnd + "op(" + priority + "," + specifier + ", " + operator + ")").hasSolution();
	}

	public final synchronized boolean currentPredicate(String functor, int arity) {
		return new Query(consultCacheFileAnd + "current_predicate(" + functor + "/" + arity + ")").hasSolution();
	}

	public final synchronized boolean currentOperator(int priority, String specifier, String operator) {
		return new Query(consultCacheFileAnd + "current_op(" + priority + "," + specifier + ", " + operator + ")")
				.hasSolution();
	}

	public final synchronized Set<PrologOperator> currentOperators() {
		Set<PrologOperator> operators = new HashSet<PrologOperator>();
		String opQuery = consultCacheFileAnd + "findall(P/S/O,current_op(P,S,O)," + KEY + ")";
		query = new Query(opQuery);
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

				OperatorEntry o = new OperatorEntry(p, s, n);
				operators.add(o);
			}
		}
		return operators;
	}

	public final synchronized int getProgramSize() {
		int counter = 0;
		Iterator<?> i = iterator();
		while (i.hasNext()) {
			counter++;
			i.next();
		}
		return counter;
	}

	public final synchronized Set<PrologIndicator> getPredicates() {
		Set<PrologIndicator> pis = predicates();
		Set<PrologIndicator> builtins = getBuiltIns();
		for (PrologIndicator prologIndicator : pis) {
			if (!builtins.contains(prologIndicator)) {
				pis.remove(prologIndicator);
			}
		}
		return pis;
	}

	public final synchronized Set<PrologIndicator> getBuiltIns() {
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

	public final String getLicense() {
		return Licenses.LGPL_V3;
	}

	public final String getVersion() {
		return JPL.version_string();
	}

	public final String getName() {
		return "JPL7";
	}

	public final void dispose() {
		query.close();
		File c = new File(cache);
		c.deleteOnExit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cache == null) ? 0 : cache.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
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
		return true;
	}

	public final synchronized Iterator<PrologClause> iterator() {
		return iterator(location);
	}

	public final String getCache() {
		return cache;
	}

}
