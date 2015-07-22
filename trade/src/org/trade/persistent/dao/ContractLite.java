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
package org.trade.persistent.dao;

// Generated Feb 21, 2011 12:43:33 PM by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.trade.core.dao.Aspect;

/**
 * Contract generated by hbm2java
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
@Entity
@Table(name = "contract")
public class ContractLite extends Aspect implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5691902477608387034L;

	private TradePosition tradePosition;

	public ContractLite() {
	}

	public ContractLite(Integer id) {
		this.id = id;
	}

	/**
	 * Method getIdContract.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idContract", unique = true, nullable = false)
	public Integer getIdContract() {
		return this.id;
	}

	/**
	 * Method setIdContract.
	 * 
	 * @param idContract
	 *            Integer
	 */
	public void setIdContract(Integer idContract) {
		this.id = idContract;
	}

	/**
	 * Method getTradePosition.
	 * 
	 * @return TradePosition
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idTradePosition", insertable = false, updatable = true, nullable = true)
	public TradePosition getTradePosition() {
		return this.tradePosition;
	}

	/**
	 * Method setTradePosition.
	 * 
	 * @param tradePosition
	 *            TradePosition
	 */
	public void setTradePosition(TradePosition tradePosition) {
		this.tradePosition = tradePosition;
	}

}
