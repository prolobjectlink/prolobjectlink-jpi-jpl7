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
package io.github.prolobjectlink.prolog.jpl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.jpl7.Variable;

import io.github.prolobjectlink.prolog.AbstractEngine;
import io.github.prolobjectlink.prolog.AbstractIterator;
import io.github.prolobjectlink.prolog.AbstractQuery;
import io.github.prolobjectlink.prolog.PrologError;
import io.github.prolobjectlink.prolog.PrologQuery;
import io.github.prolobjectlink.prolog.PrologTerm;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
final class JplQuery extends AbstractQuery implements PrologQuery {

	private String stringQuery;
	private Map<String, PrologTerm>[] solutions;
	private Iterator<Map<String, PrologTerm>> iter;

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

	JplQuery(AbstractEngine engine, String file, String stringQuery) {
		super(engine);

		if (stringQuery != null && stringQuery.length() > 0) {
			this.stringQuery = stringQuery;

			// saving variable order
			enumerateVariables(variables, Util.textToTerm(stringQuery));

			try {

				Query.hasSolution("consult('" + file + "')");
				Query query = new Query(stringQuery);
				Map<String, Term>[] solve = query.allSolutions();
				solutions = toTermMapArray(solve, PrologTerm.class);
				iter = new JplQueryIter(solutions);

			} catch (PrologException e) {
				// getLogger().error(getClass(), PrologLogger.RUNTIME_ERROR, e)
				Map<String, PrologTerm> m = new HashMap<String, PrologTerm>();
				JplReference prologexception = new JplReference(getProvider(), e);
				Term error = e.term();
				Term exception = error.arg(1);
				Term ref = exception.arg(1);
				if (ref.isJRef()) {
					Object object = ref.object();
					if (object instanceof Throwable) {
						Throwable k = (Throwable) object;
						// getLogger().error(getClass(), PrologLogger.RUNTIME_ERROR, k)
						JplReference javaexception = new JplReference(getProvider(), k);
						m.put("PrologException", prologexception);
						m.put("JavaException", javaexception);
						solutions = new Map[] { m };
					}
				}
			}
		}

	}

	public boolean hasSolution() {
		return iter != null && iter.hasNext();
	}

	public boolean hasMoreSolutions() {
		return iter != null && iter.hasNext();
	}

	public PrologTerm[] oneSolution() {
		int index = 0;
		Map<String, PrologTerm> solution = oneVariablesSolution();
		PrologTerm[] array = new PrologTerm[solution.size()];
		for (Iterator<String> i = variables.iterator(); i.hasNext();) {
			array[index++] = solution.get(i.next());
		}
		return array;
	}

	public Map<String, PrologTerm> oneVariablesSolution() {
		return solutions.length > 0 ? solutions[0] : new HashMap<String, PrologTerm>();
	}

	public PrologTerm[] nextSolution() {
		int index = 0;
		Map<String, PrologTerm> solution = nextVariablesSolution();
		PrologTerm[] array = new PrologTerm[solution.size()];
		for (Iterator<String> i = variables.iterator(); i.hasNext();) {
			array[index++] = solution.get(i.next());
		}
		return array;
	}

	public Map<String, PrologTerm> nextVariablesSolution() {
		return iter.next();
	}

	public PrologTerm[][] nSolutions(int n) {
		if (n > 0) {
			// m:solutionSize
			int m = 0;
			int index = 0;
			ArrayList<PrologTerm[]> all = new ArrayList<PrologTerm[]>();
			while (hasNext() && index < n) {
				PrologTerm[] solution = nextSolution();
				m = solution.length > m ? solution.length : m;
				index++;
				all.add(solution);
			}

			PrologTerm[][] allSolutions = new PrologTerm[n][m];
			for (int i = 0; i < n; i++) {
				PrologTerm[] solution = all.get(i);
				for (int j = 0; j < m; j++) {
					allSolutions[i][j] = solution[j];
				}
			}
			return allSolutions;
		}
		throw new PrologError("Impossible find " + n + " solutions");
	}

	public Map<String, PrologTerm>[] nVariablesSolutions(int n) {
		return Arrays.copyOf(solutions, n);
	}

	public PrologTerm[][] allSolutions() {
		// n:solutionCount, m:solutionSize
		int n = 0;
		int m = 0;
		ArrayList<PrologTerm[]> all = new ArrayList<PrologTerm[]>();
		while (hasMoreSolutions()) {
			PrologTerm[] solution = nextSolution();
			m = solution.length > m ? solution.length : m;
			n++;
			all.add(solution);
		}

		PrologTerm[][] allSolutions = new PrologTerm[n][m];
		for (int i = 0; i < n; i++) {
			PrologTerm[] solution = all.get(i);
			for (int j = 0; j < m; j++) {
				allSolutions[i][j] = solution[j];
			}
		}
		return allSolutions;
	}

	public Map<String, PrologTerm>[] allVariablesSolutions() {
		return solutions;
	}

	public void dispose() {
		iter = null;
		variables.clear();
		int l = solutions.length;
		for (int i = 0; i < l; i++) {
			solutions[i].clear();
			solutions[i] = null;
		}
		solutions = null;
	}

	public List<Map<String, PrologTerm>> all() {
		List<Map<String, PrologTerm>> l = new ArrayList<Map<String, PrologTerm>>();
		for (Map<String, PrologTerm> map : solutions) {
			l.add(map);
		}
		return l;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + stringQuery.hashCode();
		result = prime * result + variables.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
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
		return variables.equals(other.variables);
	}

	@Override
	public String toString() {
		return stringQuery;
	}

	private class JplQueryIter extends AbstractIterator<Map<String, PrologTerm>>
			implements Iterator<Map<String, PrologTerm>> {

		private int nextIndex;
		private final Map<String, PrologTerm>[] maps;

		private JplQueryIter(Map<String, PrologTerm>[] maps) {
			this.maps = maps;
		}

		public boolean hasNext() {
			return nextIndex < maps.length;
		}

		public Map<String, PrologTerm> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return maps[nextIndex++];
		}

	}

}
