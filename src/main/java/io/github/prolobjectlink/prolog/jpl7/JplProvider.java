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

import static org.jpl7.JPL.JFALSE;
import static org.jpl7.JPL.JNULL;
import static org.jpl7.JPL.JTRUE;
import static org.jpl7.JPL.JVOID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpl7.Term;
import org.jpl7.Util;

import io.github.prolobjectlink.prolog.AbstractProvider;
import io.github.prolobjectlink.prolog.PrologAtom;
import io.github.prolobjectlink.prolog.PrologConverter;
import io.github.prolobjectlink.prolog.PrologDouble;
import io.github.prolobjectlink.prolog.PrologFloat;
import io.github.prolobjectlink.prolog.PrologInteger;
import io.github.prolobjectlink.prolog.PrologJavaConverter;
import io.github.prolobjectlink.prolog.PrologList;
import io.github.prolobjectlink.prolog.PrologLogger;
import io.github.prolobjectlink.prolog.PrologLong;
import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologStructure;
import io.github.prolobjectlink.prolog.PrologTerm;
import io.github.prolobjectlink.prolog.PrologVariable;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
public abstract class JplProvider extends AbstractProvider implements PrologProvider {

	static final PrologLogger logger = new JplLogger();

	protected JplProvider(PrologConverter<Term> adapter) {
		super(adapter);
	}

	public final boolean isCompliant() {
		return true;
	}

	public final PrologTerm prologNil() {
		return new JplNil(this);
	}

	public final PrologTerm prologCut() {
		return new JplCut(this);
	}

	public final PrologTerm prologFail() {
		return new JplFail(this);
	}

	public final PrologTerm prologTrue() {
		return new JplTrue(this);
	}

	public final PrologTerm prologFalse() {
		return new JplFalse(this);
	}

	public final PrologTerm prologEmpty() {
		return new JplEmpty(this);
	}

	public final PrologTerm prologInclude(String file) {
		return newStructure("consult", newAtom(file));
	}

	public final PrologTerm parseTerm(String term) {
		return toTerm(Util.textToTerm(term), PrologTerm.class);
	}

	public final PrologTerm[] parseTerms(String stringTerms) {
		PrologTerm[] a = new PrologTerm[0];
		Term ptr = Util.textToTerm(stringTerms);
		List<PrologTerm> terms = new ArrayList<PrologTerm>();
		while (ptr.isCompound() && ptr.hasFunctor(",", 2)) {
			terms.add(toTerm(ptr.arg(1), PrologTerm.class));
			ptr = ptr.arg(2);
		}
		terms.add(toTerm(ptr, PrologTerm.class));
		return terms.toArray(a);
	}

	public final PrologAtom newAtom(String functor) {
		return new JplAtom(this, functor);
	}

	public final PrologFloat newFloat(Number value) {
		return new JplFloat(this, value);
	}

	public final PrologDouble newDouble(Number value) {
		return new JplDouble(this, value);
	}

	public final PrologInteger newInteger(Number value) {
		return new JplInteger(this, value);
	}

	public final PrologLong newLong(Number value) {
		return new JplLong(this, value);
	}

	public final PrologVariable newVariable(int position) {
		return new JplVariable(this);
	}

	public final PrologVariable newVariable(String name, int position) {
		return new JplVariable(this, name);
	}

	public final PrologList newList() {
		return new JplList(this);
	}

	public final PrologList newList(PrologTerm[] arguments) {
		return new JplList(this, arguments);
	}

	public final PrologList newList(PrologTerm head, PrologTerm tail) {
		return new JplList(this, head, tail);
	}

	public final PrologList newList(PrologTerm[] arguments, PrologTerm tail) {
		return new JplList(this, arguments, tail);
	}

	public final PrologStructure newStructure(String functor, PrologTerm... arguments) {
		return new JplStructure(this, functor, arguments);
	}

	public final PrologTerm newStructure(PrologTerm left, String operator, PrologTerm right) {
		return new JplStructure(this, left, operator, right);
	}

	public final PrologTerm newEntry(PrologTerm key, PrologTerm value) {
		return new JplEntry(this, key, value);
	}

	public final PrologTerm newEntry(Object key, Object value) {
		PrologJavaConverter transformer = getJavaConverter();
		PrologTerm keyTerm = transformer.toTerm(key);
		PrologTerm valueTerm = transformer.toTerm(value);
		return new JplEntry(this, keyTerm, valueTerm);
	}

	public final PrologTerm newMap(Map<PrologTerm, PrologTerm> map) {
		return new JplMap(this, map);
	}

	public final PrologTerm newMap(int initialCapacity) {
		return new JplMap(this, initialCapacity);
	}

	public final PrologTerm newMap() {
		return new JplMap(this);
	}

	public final PrologTerm newReference(Object reference) {
		return new JplReference(this, reference);
	}

	public final PrologTerm falseReference() {
		return new JplReference(this, JFALSE);
	}

	public final PrologTerm trueReference() {
		return new JplReference(this, JTRUE);
	}

	public final PrologTerm nullReference() {
		return new JplReference(this, JNULL);
	}

	public final PrologTerm voidReference() {
		return new JplReference(this, JVOID);
	}

	public final PrologLogger getLogger() {
		return logger;
	}

}
