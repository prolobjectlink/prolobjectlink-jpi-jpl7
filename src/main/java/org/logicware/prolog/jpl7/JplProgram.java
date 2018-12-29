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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.logicware.prolog.PrologClause;
import org.logicware.prolog.PrologClauses;
import org.logicware.prolog.PrologGoal;
import org.logicware.prolog.PrologProgram;

public final class JplProgram extends AbstractSet<PrologClauses> implements PrologProgram {

	// program initializations goals
	private final List<PrologGoal> goals = new LinkedList<PrologGoal>();

	// list of directives goals
	private final List<PrologGoal> directives = new LinkedList<PrologGoal>();

	// program (data base) in read order
	private final LinkedHashMap<String, PrologClauses> clauses = new LinkedHashMap<String, PrologClauses>();

	public PrologClauses get(String key) {
		return clauses.get(key);
	}

	public void add(PrologClause clause) {
		String key = clause.getIndicator();
		PrologClauses family = get(key);
		if (family == null) {
			clauses.put(key, new JplClauses(key, clause));
		} else {
			family.add(clause);
		}
	}

	@Override
	public boolean add(PrologClauses cls) {
		String key = cls.getIndicator();
		PrologClauses family = get(key);
		if (family != null) {
			family.addAll(cls);
		} else {
			clauses.put(key, cls);
		}
		return true;
	}

	public void add(PrologProgram program) {
		goals.addAll(program.getGoals());
		clauses.putAll(program.getClauses());
		directives.addAll(program.getDirectives());
	}

	@Override
	public boolean remove(Object o) {

		if (o instanceof PrologClause) {
			PrologClause c = (PrologClause) o;
			String key = c.getIndicator();
			PrologClauses family = get(key);
			if (family != null) {
				return family.remove(c);
			}
		}

		else if (o instanceof PrologClauses) {
			PrologClauses cs = (PrologClauses) o;
			String key = cs.getIndicator();
			PrologClauses oldFamily = clauses.remove(key);
			return oldFamily != null;
		}

		return false;
	}

	public void push(PrologClause clause) {
		String key = clause.getIndicator();
		PrologClauses family = clauses.remove(key);
		PrologClauses cs = new JplClauses(key, clause);
		if (family != null) {
			for (PrologClause prologClause : family) {
				cs.add(prologClause);
			}
		}
		clauses.put(key, cs);
	}

	public void removeAll(String key) {
		clauses.remove(key);
	}

	public void removeAll(String functor, int arity) {
		removeAll(functor + "/" + arity);
	}

	public List<PrologGoal> getDirectives() {
		return directives;
	}

	public boolean addDirective(PrologGoal directive) {
		return directives.add(directive);
	}

	public boolean removeDirective(PrologGoal directive) {
		return directives.remove(directive);
	}

	public List<PrologGoal> getGoals() {
		return goals;
	}

	public boolean addGoal(PrologGoal goal) {
		return goals.add(goal);
	}

	public boolean removeGoal(PrologGoal goal) {
		return goals.remove(goal);
	}

	public void markDynamic(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).markDynamic();
		}
	}

	public void unmarkDynamic(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).unmarkDynamic();
		}
	}

	public boolean isDynamic(String functor, int arity) {
		return get(functor + "/" + arity).isDynamic();
	}

	public void markMultifile(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).markMultifile();
		}
	}

	public void unmarkMultifile(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).unmarkMultifile();
		}
	}

	public boolean isMultifile(String functor, int arity) {
		return get(functor + "/" + arity).isMultifile();
	}

	public void markDiscontiguous(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).markDiscontiguous();
		}
	}

	public void unmarkDiscontiguous(String functor, int arity) {
		PrologClauses cls = get(functor + "/" + arity);
		if (cls instanceof JplClauses) {
			((JplClauses) cls).unmarkDiscontiguous();
		}
	}

	public boolean isDiscontiguous(String functor, int arity) {
		return get(functor + "/" + arity).isDiscontiguous();
	}

	public Set<String> getIndicators() {
		return clauses.keySet();
	}

	public Map<String, PrologClauses> getClauses() {
		return clauses;
	}

	@Override
	public String toString() {
		StringBuilder families = new StringBuilder();

		if (!directives.isEmpty()) {
			Iterator<PrologGoal> i = directives.iterator();
			while (i.hasNext()) {
				families.append(":-");
				families.append(i.next());
				families.append(i.hasNext() ? "\n" : "\n\n");
			}
		}

		if (!clauses.isEmpty()) {
			Iterator<PrologClauses> i = iterator();
			while (i.hasNext()) {
				families.append(i.next());
				families.append("\n");
			}
		}

		return "" + families + "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + clauses.hashCode();
		result = prime * result + directives.hashCode();
		result = prime * result + goals.hashCode();
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
		JplProgram o = (JplProgram) obj;
		return clauses.equals(o.clauses) &&

				directives.equals(o.directives) &&

				goals.equals(o.goals);
	}

	@Override
	public Iterator<PrologClauses> iterator() {
		return clauses.values().iterator();
	}

	@Override
	public void clear() {
		goals.clear();
		clauses.clear();
		directives.clear();
	}

	@Override
	public int size() {
		int size = 0;
		Iterator<PrologClauses> i = iterator();
		while (i.hasNext()) {
			size += i.next().size();
		}
		return size;
	}

}
