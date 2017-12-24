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
package org.logicware.jpi.jpl7;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jpl7.Atom;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.jpi.Licenses;
import org.logicware.jpi.NativeEngine;
import org.logicware.jpi.OperatorEntry;
import org.logicware.jpi.PredicateIndicator;
import org.logicware.jpi.PrologEngine;
import org.logicware.jpi.PrologIndicator;
import org.logicware.jpi.PrologOperator;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologQuery;
import org.logicware.jpi.PrologTerm;

public abstract class JplEngine extends NativeEngine implements PrologEngine {

	protected Query query;

	protected String file;
	protected String location;
	protected List<String> files;

	// used only for findall list result
	protected static final String KEY = "X";

	// JPL use for for fact clause true prolog term
	protected static final Term BODY = new Atom("true");

	// cache file path
	// create at OS temp directory
	private static String CACHE = null;

	// OS temp directory path
	protected static String TEMP = null;

	static {

		try {
			File file = File.createTempFile("prolobjectlink-jpi-jpl7-cache-", ".pl");
			TEMP = file.getParentFile().getCanonicalPath().replace(File.separatorChar, '/');
			CACHE = file.getCanonicalPath().replace(File.separatorChar, '/');
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(TEMP);
		System.out.println(CACHE);

	}

	protected JplEngine(PrologProvider provider) {
		this(provider, CACHE);
		query = new Query("consult('" + CACHE + "')");
		clean(CACHE);
	}

	protected JplEngine(PrologProvider provider, String file) {
		super(provider);
		this.file = file;
		File pf = new File(file);
		this.files = new ArrayList<String>();
		location = pf.getAbsolutePath();
		location = location.toLowerCase();
		location = location.replace(File.separatorChar, '/');
		query = new Query("consult('" + file + "')");
	}

	public final void include(String file) {
		files.add(file);
	}

	public final void consult(String path) {
		file = path;
		File pf = new File(file);
		location = pf.getAbsolutePath();
		location = location.toLowerCase();
		location = location.replace(File.separatorChar, '/');
	}

	public final synchronized void persist(String path) {
		InputStream in;
		OutputStream out;
		try {
			in = new FileInputStream(location);
			out = new FileOutputStream(path);
			copy(in, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public abstract void abolish(String functor, int arity);

	public final synchronized void asserta(String stringClause) {
		asserta(Util.textToTerm(stringClause));
	}

	public final synchronized void asserta(PrologTerm head, PrologTerm... body) {
		asserta(fromTerm(head, body, Term.class));
	}

	public abstract void asserta(Term term);

	public final synchronized void assertz(String stringClause) {
		assertz(Util.textToTerm(stringClause));
	}

	public final synchronized void assertz(PrologTerm head, PrologTerm... body) {
		assertz(fromTerm(head, body, Term.class));
	}

	public abstract void assertz(Term term);

	public final synchronized boolean clause(String stringClause) {
		return clause(Util.textToTerm(stringClause));
	}

	public final synchronized boolean clause(PrologTerm head, PrologTerm... body) {
		return clause(fromTerm(head, body, Term.class));
	}

	public abstract boolean clause(Term t);

	public final synchronized void retract(String stringClause) {
		retract(Util.textToTerm(stringClause));
	}

	public final synchronized void retract(PrologTerm head, PrologTerm... body) {
		retract(provider.fromTerm(head, body, Term.class));
	}

	public abstract void retract(Term t);

	public abstract PrologQuery query(String stringQuery);

	public abstract PrologQuery query(PrologTerm... terms);

	public final void operator(int priority, String specifier, String operator) {
		new Query("op(" + priority + "," + specifier + ", " + operator + ")").hasSolution();
	}

	public final boolean currentPredicate(String functor, int arity) {
		return new Query("current_predicate(" + functor + "/" + arity + ")").hasSolution();
	}

	public final boolean currentOperator(int priority, String specifier, String operator) {
		return new Query("current_op(" + priority + "," + specifier + ", " + operator + ")").hasSolution();
	}

	public final Set<PrologIndicator> currentPredicates() {
		Set<PrologIndicator> indicators = new HashSet<PrologIndicator>();
		String opQuery = "findall(X/Y,current_predicate(X/Y)," + KEY + ")";
		query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (int i = 0; i < termArray.length; i++) {

				Term t = termArray[i];

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

	public final Set<PrologOperator> currentOperators() {
		Set<PrologOperator> operators = new HashSet<PrologOperator>();
		String opQuery = "findall(P/S/O,current_op(P,S,O)," + KEY + ")";
		query = new Query(opQuery);
		if (query.hasSolution()) {
			Term term = query.oneSolution().get(KEY);
			Term[] termArray = term.toTermArray();
			for (int i = 0; i < termArray.length; i++) {

				Term t = termArray[i];

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

	public abstract int getProgramSize();

	public String getLicense() {
		return Licenses.BSD_3;
	}

	public final String getVersion() {
		return JPL.version_string();
	}

	public String getName() {
		return "JPL7";
	}

	public final void dispose() {
		files.clear();
		if (file.equals(CACHE)) {
			clean(CACHE);
		}
	}

}
