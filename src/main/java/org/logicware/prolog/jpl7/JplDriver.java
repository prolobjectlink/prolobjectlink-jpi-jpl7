/*
 * #%L
 * prolobjectlink-db-jtrolog
 * %%
 * Copyright (C) 2012 - 2018 Logicware Project
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.logicware.pdb.jdbc.AbstractDriver;
import org.logicware.pdb.prolog.PrologConverter;
import org.logicware.pdb.prolog.PrologDriver;

public class JplDriver extends AbstractDriver implements PrologDriver {

	public JplDriver(PrologConverter<?> converter) {
		super(converter);
	}

	public Connection connect(String url, Properties info) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
