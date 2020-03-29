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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jpl7.Term;

import io.github.prolobjectlink.prolog.ArrayIterator;
import io.github.prolobjectlink.prolog.PrologClauses;
import io.github.prolobjectlink.prolog.PrologError;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
final class JplProgram extends AbstractSet<List<Term>> {

	//
	private final JplParser parser = new JplParser();

	// program initializations goals
	private final List<Term> goals = new LinkedList<Term>();

	// list of directives goals
	private final List<Term> directives = new LinkedList<Term>();

	// program (data base) in read order
	private final LinkedHashMap<String, List<Term>> clauses = new LinkedHashMap<String, List<Term>>();

	private String getKey(Term clause) {
		String key = clause.name();
		key += "/" + clause.arity();
		if (key.equals(":-/2")) {
			key = clause.arg(1).name();
			key += "/";
			key += clause.arg(1).arity();
		}
		return key;
	}

	private String getKey(List<Term> cls) {
		String msg = "Empty clause list";
		if (!cls.isEmpty()) {
			Term t = cls.get(0);
			String key = t.name();
			key += "/" + t.arity();
			return key;
		}
		throw new PrologError(msg);
	}

	public List<Term> get(String key) {
		return clauses.get(key);
	}

	public void add(Term clause) {
		String key = getKey(clause);
		List<Term> family = get(key);
		if (family == null) {
			family = new LinkedList<Term>();
			family.add(clause);
			clauses.put(key, family);
		} else if (!family.contains(clause)) {
			family.add(clause);
		}
	}

	@Override
	public boolean add(List<Term> cls) {
		String key = getKey(cls);
		List<Term> family = get(key);
		if (family != null) {
			family.addAll(cls);
		} else {
			clauses.put(key, cls);
		}
		return true;
	}

	public void add(JplProgram program) {
		goals.addAll(program.getGoals());
		clauses.putAll(program.getClauses());
		directives.addAll(program.getDirectives());
	}

	@Override
	public boolean remove(Object o) {

		if (o instanceof Term) {
			Term c = (Term) o;
			String key = getKey(c);
			List<Term> family = get(key);
			if (family != null) {
				return family.remove(c);
			}
		}

		else if (o instanceof PrologClauses) {
			PrologClauses cs = (PrologClauses) o;
			String key = cs.getIndicator();
			List<Term> oldFamily = clauses.remove(key);
			return oldFamily != null;
		}

		return false;
	}

	public boolean remove(Term o) {

		if (o instanceof Term) {
			String key = getKey(o);
			List<Term> family = get(key);
			if (family != null) {
				return family.remove(o);
			}
		}

		return false;
	}

	public void push(Term clause) {
		String key = getKey(clause);
		List<Term> family = clauses.remove(key);
		List<Term> cs = new LinkedList<Term>();
		if (family != null && !family.contains(clause)) {
			cs.add(clause);
			for (Term term : family) {
				cs.add(term);
			}
		} else {
			cs.add(clause);
		}
		clauses.put(key, cs);
	}

	public void removeAll(String key) {
		clauses.remove(key);
	}

	public void removeAll(String functor, int arity) {
		removeAll(functor + "/" + arity);
	}

	public List<Term> getDirectives() {
		return directives;
	}

	public boolean addDirective(Term directive) {
		return directives.add(directive);
	}

	public boolean removeDirective(Term directive) {
		return directives.remove(directive);
	}

	public List<Term> getGoals() {
		return goals;
	}

	public boolean addGoal(Term goal) {
		return goals.add(goal);
	}

	public boolean removeGoal(Term goal) {
		return goals.remove(goal);
	}

	public Set<String> getIndicators() {
		return clauses.keySet();
	}

	public Map<String, List<Term>> getClauses() {
		return clauses;
	}

	@Override
	public String toString() {

		StringBuilder families = new StringBuilder();

		if (!directives.isEmpty()) {
			Iterator<Term> i = directives.iterator();
			while (i.hasNext()) {
				families.append(":-");
				families.append(i.next());
				families.append('.');
				families.append(i.hasNext() ? "\n" : "\n\n");
			}
		}

		if (!clauses.isEmpty()) {
			Iterator<List<Term>> i = iterator();
			while (i.hasNext()) {
				List<Term> l = i.next();
				Iterator<Term> j = l.iterator();
				while (j.hasNext()) {
					Term term = j.next();
					String key = term.name();
					key += "/" + term.arity();
					if (term.arity() == 2 && key.equals(":-/2")) {
						Term h = term.arg(1);
						Term b = term.arg(2);
						families.append(h);
						families.append(" :- ");
						families.append('\n');
						families.append('\t');
						Term[] array = parser.parseTerms(b);
						Iterator<Term> k = new ArrayIterator<Term>(array);
						while (k.hasNext()) {
							Term item = k.next();
							families.append(item);
							if (k.hasNext()) {
								families.append(',');
								families.append('\n');
								families.append('\t');
							}
						}
					} else {
						families.append(term);
					}
					families.append('.');
					families.append('\n');
				}
				if (i.hasNext()) {
					families.append('\n');
				}
			}
		}

		return "" + families + "";
	}

	@Override
	public Iterator<List<Term>> iterator() {
		return clauses.values().iterator();
	}

	@Override
	public int size() {
		int size = 0;
		Iterator<List<Term>> i = iterator();
		while (i.hasNext()) {
			List<Term> l = i.next();
			Iterator<Term> j = l.iterator();
			while (j.hasNext()) {
				j.next();
				size++;
			}
		}
		return size;
	}

	@Override
	public void clear() {
		goals.clear();
		clauses.clear();
		directives.clear();
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
		JplProgram other = (JplProgram) obj;
		if (!clauses.equals(other.clauses)) {
			return false;
		}
		if (!directives.equals(other.directives)) {
			return false;
		}
		return goals.equals(other.goals);
	}

}
