/*-
 * #%L
 * prolobjectlink-jpi
 * %%
 * Copyright (C) 2020 - 2021 Prolobjectlink Project
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Prolobjectlink Project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package io.github.prolobjectlink.prolog.jpl7;

import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologTerm;
import io.github.prolobjectlink.prolog.PrologTypedResult;
import io.github.prolobjectlink.prolog.PrologVariable;

public class JplTypedResult extends JplResult implements PrologTypedResult {

	private final PrologTerm kind;

	JplTypedResult(PrologProvider provider, PrologTerm name, PrologTerm kind) {
		super(provider, name);
		this.kind = kind;
	}

	JplTypedResult(PrologProvider provider, String kind, int position) {
		super(provider, provider.newVariable(position));
		this.kind = provider.newAtom(kind);
	}

	JplTypedResult(PrologProvider provider, String name, String kind, int position) {
		super(provider, provider.newVariable(name, position));
		this.kind = provider.newAtom(kind);
	}

	public final int getArity() {
		return 2;
	}

	public final String getFunctor() {
		return "-";
	}

	@Override
	public PrologTerm[] getArguments() {
		return new PrologTerm[] { getKey(), getValue() };
	}

	@Override
	public PrologTerm getKey() {
		return getNameTerm();
	}

	@Override
	public PrologTerm getValue() {
		return kind;
	}

	@Override
	public PrologTerm setValue(PrologTerm value) {
		// this.type = value.getFunctor()
		getLogger().debug(getClass(), "No value setting allow");
		return kind;
	}

	public PrologTerm getKindTerm() {
		return getValue();
	}

	public String getKind() {
		PrologVariable v = (PrologVariable) kind;
		return v.getName();
	}

}
