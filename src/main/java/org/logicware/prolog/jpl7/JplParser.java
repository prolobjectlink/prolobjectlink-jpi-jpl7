package org.logicware.prolog.jpl7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.logging.LoggerConstants;
import org.logicware.logging.LoggerUtils;
import org.logicware.prolog.PrologClause;
import org.logicware.prolog.PrologList;
import org.logicware.prolog.PrologParser;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologStructure;
import org.logicware.prolog.PrologTerm;

public final class JplParser extends JplConverter implements PrologParser {

	public PrologList parseList(String stringList) {
		return (PrologList) parseTerm(stringList);
	}

	public PrologStructure parseStructure(String stringStructure) {
		return (PrologStructure) parseTerm(stringStructure);
	}

	public PrologTerm parseTerm(String term) {
		return toTerm(Util.textToTerm(term), PrologTerm.class);
	}

	public PrologTerm[] parseTerms(String stringTerms) {
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

	public JplClause parseClause(String clause) {
		Term clauseTerm = null;
		if (clause.lastIndexOf('.') == clause.length() - 1) {
			String c = clause.substring(0, clause.length() - 1);
			clauseTerm = Util.textToTerm(c);
		} else {
			clauseTerm = Util.textToTerm(clause);
		}
		// TODO Auto-generated method stub
		return null;
	}

	public Set<PrologClause> parseProgram(String file) {
		return parseProgram(new File(file));
	}

	public Set<PrologClause> parseProgram(File in) {
		try {
			return parseProgram(new FileReader(in));
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), LoggerConstants.FILE_NOT_FOUND, e);
		}
		return new LinkedHashSet<PrologClause>();
	}

	public Set<PrologClause> parseProgram(Reader in) {
		// TODO Auto-generated method stub
		return new LinkedHashSet<PrologClause>();
	}

	public PrologProvider createProvider() {
		return provider;
	}

}
