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

import static org.prolobjectlink.prolog.PrologTermType.LIST_TYPE;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jpl7.Compound;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.prolobjectlink.AbstractIterator;
import org.prolobjectlink.prolog.PrologList;
import org.prolobjectlink.prolog.PrologProvider;
import org.prolobjectlink.prolog.PrologTerm;

public class JplList extends JplTerm implements PrologList {

	public static final Term EMPTY;

	static {

		Query query = new Query("X=[]");
		query.open();
		EMPTY = query.getSolution().get("X");
		query.close();

	}

	protected JplList(PrologProvider provider) {
		super(LIST_TYPE, provider, EMPTY);
	}

	protected JplList(PrologProvider provider, Term[] arguments) {
		super(LIST_TYPE, provider);
		value = EMPTY;
		for (int i = arguments.length - 1; i >= 0; --i) {
			value = new Compound("[|]", new Term[] { arguments[i], value });
		}
	}

	protected JplList(PrologProvider provider, PrologTerm[] arguments) {
		super(LIST_TYPE, provider);
		value = EMPTY;
		for (int i = arguments.length - 1; i >= 0; --i) {
			value = new Compound("[|]", new Term[] { unwrap(arguments[i], JplTerm.class).value, value });
		}
	}

	protected JplList(PrologProvider provider, PrologTerm head, PrologTerm tail) {
		super(LIST_TYPE, provider);
		Term h = unwrap(head, JplTerm.class).value;
		Term t = unwrap(tail, JplTerm.class).value;
		value = new Compound("[|]", new Term[] { h, t });
	}

	protected JplList(PrologProvider provider, PrologTerm[] arguments, PrologTerm tail) {
		super(LIST_TYPE, provider);
		value = fromTerm(tail, Term.class);
		for (int i = arguments.length - 1; i >= 0; --i) {
			Term[] args = { fromTerm(arguments[i], Term.class), value };
			value = new Compound("[|]", args);
		}
	}

	public int size() {
		return Util.listToLength(value);
	}

	public void clear() {
		value = EMPTY;
	}

	public boolean isEmpty() {
		return value.isListNil();
	}

	public Iterator<PrologTerm> iterator() {
		return new SwiPrologListIter(value);
	}

	public PrologTerm getHead() {
		Compound list = (Compound) value;
		return provider.toTerm(list.arg(1), PrologTerm.class);
	}

	public PrologTerm getTail() {
		Compound list = (Compound) value;
		return provider.toTerm(list.arg(2), PrologTerm.class);
	}

	public int getArity() {
		return value.arity();
	}

	public String getFunctor() {
		return ".";
	}

	public String getIndicator() {
		return getFunctor() + "/" + getArity();
	}

	public boolean hasIndicator(String functor, int arity) {
		return getFunctor().equals(functor) && getArity() == arity;
	}

	public PrologTerm[] getArguments() {
		return toTermArray(value.toTermArray(), PrologTerm[].class);
	}

	public String toString() {
		StringBuilder string = new StringBuilder("[");
		Iterator<PrologTerm> i = iterator();
		if (i.hasNext()) {
			string.append(i.next());
		}
		while (i.hasNext()) {
			string.append(", ");
			string.append(i.next());
		}
		return string.append("]").toString();
	}

	private class SwiPrologListIter extends AbstractIterator<PrologTerm> implements Iterator<PrologTerm> {

		private Term ptr;
		private int index;
		private int length;

		private SwiPrologListIter(Term l) {
			ptr = l;
			if (l.isListPair()) {
				length = Util.listToLength(l);
			}
		}

		public boolean hasNext() {
			return index < length;
		}

		public PrologTerm next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			PrologTerm term = toTerm(ptr.arg(1), PrologTerm.class);
			ptr = ptr.arg(2);
			index++;
			return term;
		}

	}

}
