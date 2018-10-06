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

import static org.logicware.platform.logging.LoggerConstants.DONT_WORRY;
import static org.logicware.platform.logging.LoggerConstants.FILE_NOT_FOUND;
import static org.logicware.platform.logging.LoggerConstants.IO;

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
import org.logicware.platform.Licenses;
import org.logicware.platform.logging.LoggerUtils;
import org.logicware.prolog.AbstractEngine;
import org.logicware.prolog.OperatorEntry;
import org.logicware.prolog.PredicateIndicator;
import org.logicware.prolog.PrologEngine;
import org.logicware.prolog.PrologIndicator;
import org.logicware.prolog.PrologOperator;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologTerm;

public abstract class JplEngine extends AbstractEngine implements PrologEngine {

	protected Query query;

	protected String tmp;
	protected String file;
	protected String location;
	protected List<String> files;

	// used only for findall list result
	protected static final String KEY = "X";

	// JPL use for for fact clause true prolog term
	protected static final Term BODY = new Atom("true");

	// cache file path
	// create at OS temp directory
	private static String cache = null;

	// OS temp directory path
	protected static String temp = null;

	static {

		File file = null;
		try {
			file = File.createTempFile("prolobjectlink-jpi-jpl7-cache-", ".pl");
			temp = file.getParentFile().getCanonicalPath().replace(File.separatorChar, '/');
			cache = file.getCanonicalPath().replace(File.separatorChar, '/');
		} catch (IOException e) {
			LoggerUtils.error(JplEngine.class, IO, e);
		}

		LoggerUtils.info(JplEngine.class, cache);

	}

	protected JplEngine(PrologProvider provider) {
		this(provider, cache);
		clean(cache);
		query = new Query("consult('" + cache + "')");
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

	public final void consult(String path) {
		file = path;
		File pf = new File(file);
		location = pf.getAbsolutePath();
		location = location.toLowerCase();
		location = location.replace(File.separatorChar, '/');
	}

	public final synchronized void persist(String path) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(location);
			out = new FileOutputStream(path);
			copy(in, out);
		} catch (FileNotFoundException e) {
			LoggerUtils.warn(getClass(), FILE_NOT_FOUND + path, e);
			LoggerUtils.info(getClass(), DONT_WORRY + path);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO + path, e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO + path, e);
				}
			}
		}
	}

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

	public String getLicense() {
		return Licenses.LGPL_V3;
	}

	public final String getVersion() {
		return JPL.version_string();
	}

	public String getName() {
		return "JPL7";
	}

	public final void dispose() {
		files.clear();
		if (file.equals(cache)) {
			clean(cache);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
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
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file)) {
			return false;
		}
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files)) {
			return false;
		}
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location)) {
			return false;
		}
		return true;
	}

}
