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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jpl7.Compound;
import org.jpl7.Query;
import org.jpl7.Term;
import org.logicware.jpi.PrologList;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologTerm;

public class JplList extends JplTerm implements PrologList {

    public static final Term EMPTY;

    static {

	Query query4 = new Query("X=[]");
	query4.open();
	EMPTY = query4.getSolution().get("X");
	query4.close();

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
	return ((Compound) value).listLength();
    }

    public void clear() {
	value = EMPTY;
    }

    public boolean isEmpty() {
	return value.isListNil();
    }

    public Iterator<PrologTerm> iterator() {
	Term list = (Term) value;
	return new SwiPrologListIter(list);
    }

    public PrologTerm getHead() {
	Compound list = (Compound) value;
	return provider.toTerm(list.arg(1), PrologTerm.class);
    }

    public PrologTerm getTail() {
	Compound list = (Compound) value;
	return provider.toTerm(list.arg(2), PrologTerm.class);
    }

    @Override
    public int getArity() {
	return value.arity();
    }

    @Override
    public String getFunctor() {
	// return value.name();
	return ".";
    }

    @Override
    public String getIndicator() {
	return getFunctor() + "/" + getArity();
    }

    @Override
    public boolean hasIndicator(String functor, int arity) {
	return getFunctor().equals(functor) && getArity() == arity;
    }

    @Override
    public PrologTerm[] getArguments() {
	return toTermArray(value.toTermArray(), PrologTerm[].class);
    }

    @Override
    public String toString() {
	String string = "[";
	Iterator<PrologTerm> i = iterator();
	if (i.hasNext()) {
	    string += i.next();
	}
	while (i.hasNext()) {
	    string += ", " + i.next();
	}
	return string += "]";
    }

    @Override
    public PrologTerm clone() {
	PrologTerm[] array = getArguments();
	return new JplList(provider, array);
    }

    private class SwiPrologListIter implements Iterator<PrologTerm> {

	private Term ptr;
	private int index;
	private int length;

	private SwiPrologListIter(Term l) {
	    ptr = l;
	    if (l.isListPair()) {
		length = l.listLength();
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

	public void remove() {
	    // TODO
	}

    }

}
