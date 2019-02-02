/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2019 Prolobjectlink Project
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
package org.prolobjectlink.prolog.jpl7;

import static org.prolobjectlink.prolog.PrologTermType.DOUBLE_TYPE;

import org.jpl7.Float;
import org.prolobjectlink.prolog.PrologDouble;
import org.prolobjectlink.prolog.PrologProvider;

public final class JplDouble extends JplFloat implements PrologDouble {

	public JplDouble(PrologProvider provider) {
		super(DOUBLE_TYPE, provider, new Float(0));
	}

	public JplDouble(PrologProvider provider, Number value) {
		super(DOUBLE_TYPE, provider, new Float(value.doubleValue()));
	}

}
