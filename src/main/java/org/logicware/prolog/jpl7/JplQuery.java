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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.jpl7.Variable;
import org.logicware.pdb.RuntimeError;
import org.logicware.pdb.prolog.AbstractEngine;
import org.logicware.pdb.prolog.AbstractQuery;
import org.logicware.pdb.prolog.PrologQuery;
import org.logicware.pdb.prolog.PrologTerm;

public final class JplQuery extends AbstractQuery implements PrologQuery {

	protected final String file;
	private final List<String> files;
	private final String stringQuery;

	private Query query;
	private Query consult;

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

	public JplQuery(AbstractEngine engine, String file, List<String> files, String stringQuery) {
		super(engine);

		this.file = file;
		this.files = files;
		this.stringQuery = stringQuery;

		// saving variable order
		enumerateVariables(variables, Util.textToTerm(stringQuery));

		this.consult = new Query("consult('" + file + "')");
		this.query = new Query(stringQuery);

		this.consult.hasSolution();
		this.query.hasSolution();

	}

	public synchronized boolean hasSolution() {
		return query.hasSolution();
	}

	public synchronized boolean hasMoreSolutions() {
		return query.hasNext();
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
		Map<String, Term> swiSolution = query.oneSolution();
		return toTermMap(swiSolution, PrologTerm.class);
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
		if (query.hasMoreSolutions()) {
			Map<String, Term> swiSolution = query.nextSolution();
			return toTermMap(swiSolution, PrologTerm.class);
		}
		return new HashMap<String, PrologTerm>();
	}

	public synchronized PrologTerm[][] nSolutions(int n) {
		if (n > 0) {
			// m:solutionSize
			int m = 0;
			int index = 0;
			ArrayList<PrologTerm[]> all = new ArrayList<PrologTerm[]>();
			PrologTerm[] solution = nextSolution();
			while (solution.length > 0 && index < n) {
				m = solution.length > m ? solution.length : m;
				index++;
				all.add(solution);
				solution = nextSolution();
			}

			PrologTerm[][] allSolutions = new PrologTerm[n][m];
			for (int i = 0; i < n; i++) {
				solution = all.get(i);
				for (int j = 0; j < m; j++) {
					allSolutions[i][j] = solution[j];
				}
			}
			return allSolutions;
		}
		throw new RuntimeError("Impossible find " + n + " solutions");
	}

	public synchronized Map<String, PrologTerm>[] nVariablesSolutions(int n) {
		Map<String, Term>[] swiSolutions = query.nSolutions(n);
		return toTermMapArray(swiSolutions, PrologTerm.class);
	}

	public synchronized PrologTerm[][] allSolutions() {
		// n:solutionCount, m:solutionSize
		int n = 0;
		int m = 0;
		ArrayList<PrologTerm[]> all = new ArrayList<PrologTerm[]>();
		PrologTerm[] solution = nextSolution();
		while (solution.length > 0) {
			m = solution.length > m ? solution.length : m;
			n++;
			all.add(solution);
			solution = nextSolution();
		}

		PrologTerm[][] allSolutions = new PrologTerm[n][m];
		for (int i = 0; i < n; i++) {
			solution = all.get(i);
			for (int j = 0; j < m; j++) {
				allSolutions[i][j] = solution[j];
			}
		}
		return allSolutions;
	}

	public synchronized Map<String, PrologTerm>[] allVariablesSolutions() {
		Map<String, Term>[] swiSolutions = query.allSolutions();
		return toTermMapArray(swiSolutions, PrologTerm.class);
	}

	public synchronized List<Map<String, PrologTerm>> all() {
		List<Map<String, PrologTerm>> v = null;
		Map<String, Term>[] s = query.allSolutions();
		v = new ArrayList<Map<String, PrologTerm>>(s.length);
		for (int i = 0; i < s.length; i++) {
			Map<String, Term> map = s[i];
			v.add(toTermMap(map, PrologTerm.class));
		}
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hashCode(file);
		result = prime * result + Objects.hashCode(files);
		result = prime * result + Objects.hashCode(stringQuery);
		result = prime * result + Objects.hashCode(variables);
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
		JplQuery other = (JplQuery) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (stringQuery == null) {
			if (other.stringQuery != null)
				return false;
		} else if (!stringQuery.equals(other.stringQuery))
			return false;
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
