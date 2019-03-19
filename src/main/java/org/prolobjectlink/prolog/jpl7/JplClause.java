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

import org.prolobjectlink.prolog.AbstractClause;
import org.prolobjectlink.prolog.PrologClause;
import org.prolobjectlink.prolog.PrologIndicator;
import org.prolobjectlink.prolog.PrologProvider;
import org.prolobjectlink.prolog.PrologTerm;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
public final class JplClause extends AbstractClause implements PrologClause {

	private final PrologIndicator indicator;

	protected JplClause(PrologProvider provider, PrologTerm head, boolean dynamic, boolean multifile,
			boolean discontiguous) {
		super(provider, head, dynamic, multifile, discontiguous);
		this.indicator = new JplIndicator(head.getFunctor(), head.getArity());
	}

	protected JplClause(PrologProvider provider, PrologTerm head, PrologTerm body, boolean dynamic, boolean multifile,
			boolean discontiguous) {
		super(provider, head, body, dynamic, multifile, discontiguous);
		this.indicator = new JplIndicator(head.getFunctor(), head.getArity());
	}

	public PrologIndicator getPrologIndicator() {
		return indicator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((indicator == null) ? 0 : indicator.hashCode());
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
		JplClause other = (JplClause) obj;
		if (indicator == null) {
			if (other.indicator != null)
				return false;
		} else if (!indicator.equals(other.indicator)) {
			return false;
		}
		return true;
	}
}
