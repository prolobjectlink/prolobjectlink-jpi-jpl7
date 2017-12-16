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

import org.jpl7.Query;
import org.jpl7.Term;
import org.logicware.jpi.AbstractTerm;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologTerm;
import org.logicware.jpi.UnknownTermError;

public abstract class JplTerm extends AbstractTerm implements PrologTerm {

    protected Term value;

    protected static final String SIMPLE_ATOM_REGEX = ".|[a-z][A-Za-z0-9_]*";

    protected JplTerm(int type, PrologProvider provider) {
	super(type, provider);
    }

    protected JplTerm(int type, PrologProvider provider, Term value) {
	super(type, provider);
	this.value = value;
    }

    public final boolean isAtom() {
	return value.isAtom();
    }

    public final boolean isNumber() {
	return isFloat() || isDouble() || isInteger() || isLong();
    }

    public final boolean isFloat() {
	return type == FLOAT_TYPE && value.isFloat();
    }

    public final boolean isDouble() {
	return type == DOUBLE_TYPE;
    }

    public final boolean isInteger() {
	return type == INTEGER_TYPE && value.isInteger();
    }

    public final boolean isLong() {
	return type == LONG_TYPE;
    }

    public final boolean isVariable() {
	return value.isVariable();
    }

    public final boolean isList() {
	return value.isListNil() || value.isListPair();
    }

    public final boolean isStructure() {
	return isCompound() && !isList();
    }

    public final boolean isNil() {
	if (!isVariable() && !isNumber()) {
	    return value.hasFunctor("nil", 0);
	}
	return false;
    }

    public final boolean isEmptyList() {
	return value.isListNil();
    }

    public final boolean isEvaluable() {
	if (!isVariable() && !isList() && !isNumber() && getArity() == 2) {
	    String key = "LIST";
	    String opQuery = "findall(OP,current_op(_,_,OP)," + key + ")";
	    Query query = new Query(opQuery);
	    if (query.hasSolution()) {
		Term term = query.oneSolution().get(key);
		Term[] termArray = term.toTermArray();
		for (int i = 0; i < termArray.length; i++) {
		    if (termArray[i].name().equals(getFunctor())) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    public final boolean isAtomic() {
	return !isCompound() && !isList();
    }

    public final boolean isCompound() {
	return value.isCompound();
    }

    public abstract String getIndicator();

    public abstract boolean hasIndicator(String functor, int arity);

    public abstract int getArity();

    public abstract String getFunctor();

    public abstract PrologTerm[] getArguments();

    public final boolean unify(PrologTerm o) {

	if (!(o instanceof JplTerm)) {
	    throw new UnknownTermError(o);
	}

	Term term = ((JplTerm) o).value;
	Query query = new Query("unify_with_occurs_check(" + value + "," + term + ")");
	boolean result = query.hasSolution();
	query.close();

	return result;

    }

    public final int compareTo(PrologTerm o) {

	if (!(o instanceof JplTerm)) {
	    throw new UnknownTermError(o);
	}

	String key = "Order";
	Term term = ((JplTerm) o).value;
	String arguments = key + "," + value + "," + term;
	Query query = new Query("compare(" + arguments + ")");

	query.open();
	Term order = query.getSolution().get(key);
	query.close();

	if (order.hasFunctor("<", 0)) {
	    return -1;
	} else if (order.hasFunctor(">", 0)) {
	    return 1;
	}

	return 0;

    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + type;
	// Term not implement hashCode()
	// result = prime * result + ((value == null) ? 0 : value.hashCode());
	result = prime * result + ((value == null) ? 0 : value.toString().hashCode());
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
	JplTerm other = (JplTerm) obj;
	if (type != other.type)
	    return false;
	if (value == null) {
	    if (other.value != null)
		return false;
	} else if (!value.equals(other.value))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "" + value + "";
    }

    @Override
    public abstract PrologTerm clone();

}
