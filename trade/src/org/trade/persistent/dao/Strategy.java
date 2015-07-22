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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.trade.core.dao.Aspect;
import org.trade.strategy.data.IndicatorSeries;

/**
 * Strategy generated by hbm2java
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
@Entity
@Table(name = "strategy")
public class Strategy extends Aspect implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5704206226348654910L;

	@NotNull
	private String className;
	@NotNull
	private String name;
	private String description;
	private Boolean marketData = new Boolean(false);
	private Strategy strategy;
	private List<Tradestrategy> tradestrategies = new ArrayList<Tradestrategy>(
			0);
	private List<Rule> rules = new ArrayList<Rule>(0);
	private List<IndicatorSeries> indicators = new ArrayList<IndicatorSeries>(0);
	private List<Strategy> strategies = new ArrayList<Strategy>(0);

	public Strategy() {
	}

	/**
	 * Constructor for Strategy.
	 * 
	 * @param name
	 *            String
	 */
	public Strategy(String name) {
		this.name = name;
		this.className = name;
	}

	/**
	 * Constructor for Strategy.
	 * 
	 * @param name
	 *            String
	 * 
	 * @param className
	 *            String
	 */
	public Strategy(String name, String className) {
		this.name = name;
		this.className = className;
	}

	/**
	 * Constructor for Strategy.
	 * 
	 * @param name
	 *            String
	 * @param description
	 *            String
	 * @param marketData
	 *            Boolean
	 * @param className
	 *            String
	 * @param tradestrategies
	 *            List<Tradestrategy>
	 * @param rules
	 *            List<Rule>
	 * @param strategies
	 *            List<Strategy>
	 */
	public Strategy(String name, String description, Boolean marketData,
			String className, List<Tradestrategy> tradestrategies,
			List<Rule> rules, List<Strategy> strategies) {
		this.name = name;
		this.description = description;
		this.marketData = marketData;
		this.tradestrategies = tradestrategies;
		this.rules = rules;
		this.strategies = strategies;
		this.className = className;
	}

	/**
	 * Method getIdStrategy.
	 * 
	 * @return Integer
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "idStrategy", unique = true, nullable = false)
	public Integer getIdStrategy() {
		return this.id;
	}

	/**
	 * Method setIdStrategy.
	 * 
	 * @param idStrategy
	 *            Integer
	 */
	public void setIdStrategy(Integer idStrategy) {
		this.id = idStrategy;
	}

	/**
	 * Method getName.
	 * 
	 * @return String
	 */
	@Column(name = "name", unique = true, nullable = false, length = 45)
	public String getName() {
		return this.name;
	}

	/**
	 * Method setName.
	 * 
	 * @param name
	 *            String
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Method getClassName.
	 * 
	 * @return String
	 */
	@Column(name = "className", unique = false, nullable = false, length = 100)
	public String getClassName() {
		return this.className;
	}

	/**
	 * Method setClassName.
	 * 
	 * @param className
	 *            String
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Method getDescription.
	 * 
	 * @return String
	 */
	@Column(name = "description", length = 240)
	public String getDescription() {
		return this.description;
	}

	/**
	 * Method setDescription.
	 * 
	 * @param description
	 *            String
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Method getMarketData.
	 * 
	 * @return Boolean
	 */
	@Column(name = "marketData", length = 1)
	public Boolean getMarketData() {
		return this.marketData;
	}

	/**
	 * Method setMarketData.
	 * 
	 * @param marketData
	 *            Boolean
	 */
	public void setMarketData(Boolean marketData) {
		this.marketData = marketData;
	}

	/**
	 * Method getVersion.
	 * 
	 * @return Integer
	 */
	@Version
	@Column(name = "version")
	public Integer getVersion() {
		return this.version;
	}

	/**
	 * Method setVersion.
	 * 
	 * @param version
	 *            Integer
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * Method getTradestrategies.
	 * 
	 * @return List<Tradestrategy>
	 */
	@OneToMany(mappedBy = "strategy", fetch = FetchType.LAZY)
	public List<Tradestrategy> getTradestrategies() {
		return this.tradestrategies;
	}

	/**
	 * Method setTradestrategies.
	 * 
	 * @param tradestrategies
	 *            List<Tradestrategy>
	 */
	public void setTradestrategies(List<Tradestrategy> tradestrategies) {
		this.tradestrategies = tradestrategies;
	}

	/**
	 * Method getRules.
	 * 
	 * @return List<Rule>
	 */
	@OneToMany(mappedBy = "strategy", fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<Rule> getRules() {
		return this.rules;
	}

	/**
	 * Method setRules.
	 * 
	 * @param rules
	 *            List<Rule>
	 */
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	/**
	 * Method add.
	 * 
	 * @param rule
	 *            Rule
	 */
	public void add(Rule rule) {
		this.rules.add(rule);
	}

	/**
	 * Method getIndicatorSeries.
	 * 
	 * 
	 * @return List<IndicatorSeries>
	 */
	@OneToMany(mappedBy = "strategy", fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.ALL })
	public List<IndicatorSeries> getIndicatorSeries() {
		return this.indicators;
	}

	/**
	 * Method setIndicatorSeries.
	 * 
	 * @param indicators
	 *            List<IndicatorSeries>
	 */
	public void setIndicatorSeries(List<IndicatorSeries> indicators) {
		this.indicators = indicators;
	}

	/**
	 * Method getStrategies.
	 * 
	 * @return List<Strategy>
	 */
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "idStrategyManager")
	public List<Strategy> getStrategies() {
		return this.strategies;
	}

	/**
	 * Method setStrategies.
	 * 
	 * @param strategies
	 *            List<Strategy>
	 */
	public void setStrategies(List<Strategy> strategies) {
		this.strategies = strategies;
	}

	/**
	 * Method getStrategyManager.
	 * 
	 * @return Strategy
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "idStrategyManager", insertable = true, updatable = true, nullable = true)
	public Strategy getStrategyManager() {
		return this.strategy;
	}

	/**
	 * Method setStrategyManager.
	 * 
	 * @param strategy
	 *            Strategy
	 */
	public void setStrategyManager(Strategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Method getStrategyManager.
	 * 
	 * @return boolean
	 */
	public boolean hasStrategyManager() {
		if (null == getStrategyManager())
			return false;
		return true;
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	@Transient
	public boolean isDirty() {
		for (IndicatorSeries item : this.getIndicatorSeries()) {
			if (item.isDirty())
				return true;
		}
		return super.isDirty();
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {

		Strategy strategy = (Strategy) super.clone();
		List<Tradestrategy> tradestrategies = new ArrayList<Tradestrategy>(0);
		strategy.setTradestrategies(tradestrategies);
		strategy.setIndicatorSeries(this.getIndicatorSeries());
		return strategy;
	}

	/**
	 * Method hashCode.
	 * 
	 * For every field tested in the equals-Method, calculate a hash code c by:
	 * 
	 * If the field f is a boolean: calculate * (f ? 0 : 1); If the field f is a
	 * byte, char, short or int: calculate (int)f;
	 * 
	 * If the field f is a long: calculate (int)(f ^ (f >>> 32));
	 * 
	 * If the field f is a float: calculate Float.floatToIntBits(f);
	 * 
	 * If the field f is a double: calculate Double.doubleToLongBits(f) and
	 * handle the return value like every long value;
	 * 
	 * If the field f is an object: Use the result of the hashCode() method or 0
	 * if f == null;
	 * 
	 * If the field f is an array: See every field as separate element and
	 * calculate the hash value in a recursive fashion and combine the values as
	 * described next.
	 * 
	 * @return int
	 */
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash + (this.getName() == null ? 0 : this.getName().hashCode());
		hash = hash
				+ (this.getClassName() == null ? 0 : this.getClassName()
						.hashCode());
		return hash;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * Method equals.
	 * 
	 * @param objectToCompare
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {
		return super.equals(objectToCompare);
	}
}
