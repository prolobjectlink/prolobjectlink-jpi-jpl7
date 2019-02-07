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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.jpl7.Variable;
import org.prolobjectlink.prolog.AbstractEngine;
import org.prolobjectlink.prolog.AbstractQuery;
import org.prolobjectlink.prolog.PrologQuery;
import org.prolobjectlink.prolog.PrologTerm;

public final class JplQuery extends AbstractQuery implements PrologQuery {

	protected String file;
	private String stringQuery;

	private Query query;
	private Query consult;

	private boolean succes;

	private final List<String> variables = new ArrayList<String>();

	private void enumerateVariables(List<String> vector, Term term) {
		if (!(term instanceof Variable)) {
			Term[] terms = term.args();
			for (Term t : terms) {
				enumerateVariables(vector, t);
			}
		} else if (!vector.contains(term.name())) {
			vector.add(term.name());
		}
	}

	private PrologTerm[][] matrix(Map<String, Term>[] s, int n, int m) {
		PrologTerm[][] matrix = new PrologTerm[n][m];
		for (int i = 0; i < s.length; i++) {
			int index = 0;
			for (Iterator<String> iter = variables.iterator(); iter.hasNext();) {
				matrix[i][index++] = toTerm(s[i].get(iter.next()), PrologTerm.class);
			}
		}
		return matrix;
	}

	JplQuery(AbstractEngine engine, String file, String stringQuery) {
		super(engine);

		if (stringQuery != null && stringQuery.length() > 0) {
			this.file = file;
			this.stringQuery = stringQuery;

			// saving variable order
			enumerateVariables(variables, Util.textToTerm(stringQuery));

			try {

				consult = new Query("consult('" + file + "')");
				query = new Query(stringQuery);

				consult.hasSolution();
				succes = query.hasSolution();

			} catch (PrologException e) {
				succes = false;
			}
		}

	}

	public synchronized boolean hasSolution() {
		return succes && query != null && query.hasSolution();
	}

	public synchronized boolean hasMoreSolutions() {
		return succes && query != null && query.hasMoreSolutions();
	}

	public synchronized PrologTerm[] oneSolution() {
		int index = 0;
		Map<String, PrologTerm> solution = oneVariablesSolution();
		PrologTerm[] array = new PrologTerm[solution.size()];
		for (Iterator<String> i = variables.iterator(); i.hasNext();) {
			array[index++] = solution.get(i.next());
		}
		return array;
	}

	public synchronized Map<String, PrologTerm> oneVariablesSolution() {
		if (query != null && query.hasSolution()) {
			Map<String, Term> swiSolution = query.oneSolution();
			return toTermMap(swiSolution, PrologTerm.class);
		}
		return new HashMap<String, PrologTerm>();
	}

	public synchronized PrologTerm[] nextSolution() {
		Map<String, PrologTerm> solution = nextVariablesSolution();
		PrologTerm[] array = new PrologTerm[solution.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = solution.get(variables.get(i));
		}
		return array;
	}

	public synchronized Map<String, PrologTerm> nextVariablesSolution() {
		if (query != null /* && query.hasMoreSolutions() */) {
			Map<String, Term> swiSolution = query.nextSolution();
			return toTermMap(swiSolution, PrologTerm.class);
		}
		return new HashMap<String, PrologTerm>();
	}

	public synchronized PrologTerm[][] nSolutions(int n) {
		if (query != null && query.hasSolution()) {
			Map<String, Term>[] s = query.nSolutions(n);
			if (s != null && s.length > 0) {
				int m = s[0].size();
				return matrix(s, n, m);
			}
		}
		return new PrologTerm[0][0];
	}

	public synchronized Map<String, PrologTerm>[] nVariablesSolutions(int n) {
		if (query != null && query.hasSolution()) {
			Map<String, Term>[] swiSolutions = query.nSolutions(n);
			return toTermMapArray(swiSolutions, PrologTerm.class);
		}
		return new HashMap[0];
	}

	public synchronized PrologTerm[][] allSolutions() {
		if (query != null && query.hasSolution()) {
			Map<String, Term>[] s = query.allSolutions();
			if (s != null && s.length > 0) {
				int n = s.length;
				int m = s[0].size();
				return matrix(s, n, m);
			}
		}
		return new PrologTerm[0][0];
	}

	public synchronized Map<String, PrologTerm>[] allVariablesSolutions() {
		if (query != null && query.hasSolution()) {
			Map<String, Term>[] swiSolutions = query.allSolutions();
			return toTermMapArray(swiSolutions, PrologTerm.class);
		}
		return new HashMap[0];
	}

	public synchronized List<Map<String, PrologTerm>> all() {
		if (query != null && query.hasSolution()) {
			Map<String, Term>[] s = query.allSolutions();
			List<Map<String, PrologTerm>> v = new ArrayList<Map<String, PrologTerm>>(s.length);
			for (Map<String, Term> map : s) {
				v.add(toTermMap(map, PrologTerm.class));
			}
			return v;
		}
		return new ArrayList<Map<String, PrologTerm>>();
	}

	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hashCode(file);
		result = prime * result + Objects.hashCode(stringQuery);
		result = prime * result + Objects.hashCode(variables);
		return result;
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JplQuery other = (JplQuery) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		return Objects.equals(variables, other.variables);
	}

	@Override
	public synchronized String toString() {
		return "" + query + "";
	}

	public synchronized void dispose() {
		consult.close();
		query.close();
	}

}
