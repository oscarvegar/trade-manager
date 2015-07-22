/* ===========================================================
 * Smart Trade System: a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.dictionary.valuetype;

import java.util.Iterator;

import org.trade.core.valuetype.DAODecode;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.persistent.dao.Portfolio;

/**
 */
public class DAOPortfolio extends DAODecode {

	private static final long serialVersionUID = -5381026427696898592L;
	public static final String DECODE = "PORTFOLIO";
	public static final String _TABLE = "_TABLE";
	public static final String _TABLE_ID = "_TABLE_ID";
	public static final String _COLUMN = "_COLUMN";

	public DAOPortfolio() {
		super(DECODE);
	}

	/**
	 * Method newInstance.
	 * 
	 * @param value
	 *            String
	 * @return DAOPortfolio
	 */
	public static DAOPortfolio newInstance(String displayName) {
		final DAOPortfolio returnInstance = new DAOPortfolio();
		returnInstance.setDisplayName(displayName);
		return returnInstance;
	}

	/**
	 * Method newInstance.
	 * 
	 * @return DAOPortfolio
	 */
	public static DAOPortfolio newInstance() {

		try {
			final DAOPortfolio returnInstance = new DAOPortfolio();
			DAOPortfolio code = null;
			for (Iterator<Decode> iterCodes = returnInstance.getCodesDecodes()
					.iterator(); iterCodes.hasNext();) {
				code = (DAOPortfolio) iterCodes.next();
				Portfolio portfolio = (Portfolio) code.getObject();
				if (portfolio.getIsDefault())
					return code;
			}
			if (null == code) {
				code = returnInstance;
			}
			return code;
		} catch (ValueTypeException e) {
			return null;
		}
	}

	/**
	 * Method convertToUppercase.
	 * 
	 * @return boolean
	 */
	protected boolean convertToUppercase() {
		return false;
	}
}