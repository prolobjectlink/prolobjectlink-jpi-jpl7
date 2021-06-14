/*-
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2020 - 2021 Prolobjectlink Project
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

import java.util.Map.Entry;

import io.github.prolobjectlink.prolog.AbstractCompounds;
import io.github.prolobjectlink.prolog.PrologEntry;
import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologTerm;
import io.github.prolobjectlink.prolog.PrologTermType;

/**
 * PrologEntry is key-value pair of PrologTerm. Is an implementation of
 * {@link Entry} and {@link PrologTerm}.
 * 
 * @author Jose Zalacain
 * @since 1.1
 */
public final class JplEntry extends AbstractCompounds implements PrologEntry {

	private final PrologTerm key;
	private PrologTerm value;

	JplEntry(PrologProvider provider, PrologTerm key, PrologTerm value) {
		super(PrologTermType.MAP_ENTRY_TYPE, provider);
		this.value = value;
		this.key = key;
	}

	public PrologTerm getKey() {
		return key;
	}

	public PrologTerm getValue() {
		return value;
	}

	public PrologTerm setValue(PrologTerm value) {
		this.value = value;
		return value;
	}

	public boolean isList() {
		return false;
	}

	public boolean isStructure() {
		return true;
	}

	public boolean isEmptyList() {
		return false;
	}

	public String getFunctor() {
		return "-";
	}

	public int getArity() {
		return 2;
	}

	public PrologTerm[] getArguments() {
		return new PrologTerm[] { key, value };
	}

	@Override
	public int hashCode() {
		int result = 0;
		final int prime = 31;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JplEntry other = (JplEntry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "" + key + "-" + value + "";
	}

}
