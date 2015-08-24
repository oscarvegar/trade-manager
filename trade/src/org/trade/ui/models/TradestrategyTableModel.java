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
package org.trade.ui.models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Vector;

import javax.swing.Timer;

import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.DAOStrategyManager;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.ui.base.TableModel;

/**
 */
public class TradestrategyTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String TRADE = "Trade";
	private static final String SYMBOL = "Symbol*";
	private static final String DATE = "Date*";
	private static final String SIDE = "Side";
	private static final String TIER = "Tier";
	private static final String STRATEGY = "   Strategy*   ";
	private static final String STRATEGY_MGR = " Strategy Mgr  ";
	private static final String PORTFOLIO = "Portfolio*";
	private static final String BAR_SIZE = "Bar Size*";
	private static final String CHART_DAYS = "Chart Days*";
	private static final String RISK_AMOUNT = "Risk Amt";
	private static final String PERCENTCHGFRCLOSE = "%Chg Close";
	private static final String PERCENTCHGFROPEN = "%Chg Open";
	private static final String STATUS = "     Status     ";
	private static final String CURRENCY = "Currency*";
	private static final String EXCHANGE = "Exchange*";
	private static final String SEC_TYPE = "SEC Type*";
	private static final String EXPIRY = "Expiry";

	private static final String[] columnHeaderToolTip = {
			"<html>Tradingday<br>"
					+ "Tradestrategies are unique based on Tradingday/Contract/Portfolio/Strategy/BarSize</html>",
			"If checked the Tradestrategy will trade",
			"<html>Contract symbol<br>"
					+ "Contracts are unique based on Symbol/SECType/Exchange/Currency/Expiry date<br>"
					+ "Note the default on add is set in the config.properties (<b>trade.tradingtab.default.add</b>)</html>",
			"<html>Your dirctional bias for this contract.<br>"
					+ "Note this is only needed if your strategy uses it.</html>",
			"<html>For gaps the grade<br>"
					+ "See gap rules for tier grading criteria.<br>"
					+ "Note this is only needed if your strategy uses it.</html>",
			"<html>The strategy to trade with<br>"
					+ "Note the default is set in the config.properties (<b>trade.strategy.default</b>)</html>",
			null,
			"Portfolio",
			"<html>Bar size for strategy. Note Chart Days/BarSize combinations for IB:<br/>"
					+ "Note the default is set in the config.properties (<b>trade.backfill.barsize</b>)</html>",
			"<html>Historical data to pull in i.e 2D is today + yesterday<br>"
					+ "Note the default is set in the config.properties (<b>trade.backfill.duration</b>)</html>",
			"<html>Risk amount for trade used to calculate position size<br>"
					+ "Note the default is set in the config.properties (<b>trade.risk</b>)</html>",
			"% Change from close",
			"% Change from open",
			"<html>Tradestrategy status<br>"
					+ "Note this is updated by the application</html>",
			null,
			null,
			null,
			"<html>Expiry date for future contracts<br>"
					+ "Format MM/YYYY</html>" };

	private Tradingday m_data = null;
	private Timer timer = null;

	public TradestrategyTableModel() {
		super(columnHeaderToolTip);
		//columnNames = new String[18];
		columnNames = new String[16];
		columnNames[0] = DATE;
		columnNames[1] = TRADE;
		columnNames[2] = SYMBOL;
		columnNames[3] = SIDE;
		//columnNames[4] = TIER;
		columnNames[4] = STRATEGY;
		//columnNames[5] = STRATEGY_MGR;
		columnNames[5] = RISK_AMOUNT;
		columnNames[6] = PERCENTCHGFRCLOSE;
		columnNames[7] = PERCENTCHGFROPEN;
		columnNames[8] = STATUS;
		columnNames[9] = CURRENCY;
		columnNames[10] = SEC_TYPE;
		columnNames[11] = EXPIRY;
		columnNames[12] = PORTFOLIO;
		columnNames[13] = EXCHANGE;
		columnNames[14] = BAR_SIZE;
		columnNames[15] = CHART_DAYS;
		/*
		 * Create a 5sec timer to refresh the data this is used for the % chg,
		 * strategy and status fields.
		 */
		timer = new Timer(5000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < getRowCount(); i++) {
					//fireTableCellUpdated(i, 5);  es strategy column
					fireTableCellUpdated(i, 4);
					//fireTableCellUpdated(i, 6);
					
					//fireTableCellUpdated(i, 11);
					fireTableCellUpdated(i, 6);
					
					//fireTableCellUpdated(i, 12);
					fireTableCellUpdated(i, 7);
					
					//fireTableCellUpdated(i, 13);
					fireTableCellUpdated(i, 8);
				}
			}
		});
	}

	/**
	 * Method getData.
	 * 
	 * @return Tradingday
	 */
	public Tradingday getData() {
		return this.m_data;
	}

	/**
	 * Method isCellEditable.
	 * 
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return boolean
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) {
		System.out.println("isCellEditTable...");
		Tradestrategy element = getData().getTradestrategies().get(row);
		if (null != element) {
			if (!element.getTradeOrders().isEmpty()) {
				return false;
			}
		}

		if ((columnNames[column] == DATE)
				//|| (columnNames[column] == STRATEGY_MGR)
				|| (columnNames[column] == PERCENTCHGFRCLOSE)
				|| (columnNames[column] == PERCENTCHGFROPEN)
				|| (columnNames[column] == STATUS)) {
			return false;
		}
		return true;
	}

	/**
	 * Method setValueAt.
	 * 
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int column) {
		System.out.println("setValueAt");
		System.out.println("column: " + column + " row " + row + " value: " + value);
		if (null != value && !value.equals(super.getValueAt(row, column))) {
			this.populateDAO(value, row, column);
			Vector<Object> dataRow = rows.get(row);
			dataRow.setElementAt(value, column);
			fireTableCellUpdated(row, column);
		}
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            Tradingday
	 */
	public void setData(Tradingday data) {
		if (timer.isRunning())
			timer.stop();
		this.m_data = data;
		this.clearAll();
		if (null != getData() && null != getData().getTradestrategies()
				&& !getData().getTradestrategies().isEmpty()) {
			for (final Tradestrategy element : getData().getTradestrategies()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
		timer.start();
	}

	/**
	 * Method populateDAO.
	 * 
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 */
	public void populateDAO(Object value, int row, int column) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>< populateDAO");
		Tradestrategy element = getData().getTradestrategies().get(row);

		switch (column) {
		case 0: {
			element.getTradingday().setOpen(((Date) value).getZonedDateTime());
			break;
		}
		case 1: {
			element.setTrade(new Boolean(((YesNo) value).getCode()));
			break;
		}
		case 2: {
			element.getContract().setSymbol(
					((String) value).trim().toUpperCase());
			break;
		}
		case 3: {
			element.setSide(((Side) value).getCode());
			break;
		}
		case 4: {
//			if (!Decode.NONE.equals(((Tier) value).getDisplayName())) {
//				element.setTier(((Tier) value).getCode());
//			} else {
//				element.setTier(null);
//			}
			final Strategy strategy = (Strategy) ((DAOStrategy) value)
					.getObject();
			element.setStrategy(strategy);

			if (strategy.hasStrategyManager()) {
				this.setValueAt(DAOStrategyManager.newInstance(strategy
						.getStrategyManager().getName()), row, column + 1);
			} else {
				this.setValueAt(DAOStrategyManager.newInstance(Decode.NONE),
						row, column + 1);
			}
			break;
		}
		case 5: {
//			final Strategy strategy = (Strategy) ((DAOStrategy) value)
//					.getObject();
//			element.setStrategy(strategy);
//
//			if (strategy.hasStrategyManager()) {
//				this.setValueAt(DAOStrategyManager.newInstance(strategy
//						.getStrategyManager().getName()), row, column + 1);
//			} else {
//				this.setValueAt(DAOStrategyManager.newInstance(Decode.NONE),
//						row, column + 1);
//			}
			element.setRiskAmount(((Money) value).getBigDecimalValue());
			break;
		}
		case 6: {
//			element.getStrategy().setStrategyManager(
//					(Strategy) ((DAOStrategyManager) value).getObject());
			break;
		}
		case 7: {
//			Portfolio portfolio = (Portfolio) ((DAOPortfolio) value)
//					.getObject();
//			element.setPortfolio(portfolio);
			break;
		}
		case 8: {
			//element.setBarSize(new Integer(((BarSize) value).getCode()));
			element.setStatus(((TradestrategyStatus) value).getCode());
			break;
		}
		case 9: {
			//element.setChartDays(new Integer(((ChartDays) value).getCode()));
			element.getContract().setCurrency(((Currency) value).getCode());
			break;
		}
		case 10: {
			//element.setRiskAmount(((Money) value).getBigDecimalValue());
			element.getContract().setSecType(((SECType) value).getCode());
			break;
		}
		case 11:{
			ZonedDateTime zonedDateTime = ((Date) value).getZonedDateTime();
			zonedDateTime = zonedDateTime.plusMonths(1);
			zonedDateTime = zonedDateTime.minusDays(1);
			element.getContract().setExpiry(zonedDateTime);
			break;
		}
		case 12: {
			Portfolio portfolio = (Portfolio) ((DAOPortfolio) value).getObject();
			element.setPortfolio(portfolio);
			break;
		}
		case 13: {
//			element.setStatus(((TradestrategyStatus) value).getCode());
			element.getContract().setExchange(((Exchange) value).getCode());
			break;
		}
		case 14: {
//			element.getContract().setCurrency(((Currency) value).getCode());
			element.setBarSize(new Integer(((BarSize) value).getCode()));
			break;
		}
		case 15: {
			element.getContract().setExchange(((Exchange) value).getCode());
			element.setChartDays(new Integer(((ChartDays) value).getCode()));
			break;
		}
//		case 16: {
//			element.getContract().setSecType(((SECType) value).getCode());
//			
//			break;
//		}
//		case 17: {
//			ZonedDateTime zonedDateTime = ((Date) value).getZonedDateTime();
//			zonedDateTime = zonedDateTime.plusMonths(1);
//			zonedDateTime = zonedDateTime.minusDays(1);
//			element.getContract().setExpiry(zonedDateTime);
//			
//			break;
//		}
		default: {
		}
		}
		element.setLastUpdateDate(TradingCalendar
				.getDateTimeNowMarketTimeZone());
		element.setDirty(true);
	}

	/**
	 * Method deleteRow.
	 * 
	 * @param selectedRow
	 *            int
	 */
	public void deleteRow(int selectedRow) {
		System.out.println("delete Row");
		String symbol = ((String) this.getValueAt(selectedRow, 2)).trim()
				.toUpperCase();
		final Strategy strategy = (Strategy) ((DAOStrategy) this.getValueAt(
				selectedRow, 4)).getObject();
		Portfolio portfolio = (Portfolio) ((DAOPortfolio) this.getValueAt(
				selectedRow, 12)).getObject();
		Integer barSize = new Integer(
				((BarSize) this.getValueAt(selectedRow, 14)).getCode());
		String currency = ((Currency) this.getValueAt(selectedRow, 9))
				.getCode();
		String exchange = ((Exchange) this.getValueAt(selectedRow, 13))
				.getCode();
		String secType = ((SECType) this.getValueAt(selectedRow, 10)).getCode();

		for (final Tradestrategy element : getData().getTradestrategies()) {
			if (null != barSize && barSize == 1) {
				long daySeconds = TradingCalendar.getDurationInSeconds(element
						.getTradingday().getOpen(), element.getTradingday()
						.getClose());
				barSize = (int) daySeconds * barSize;
			}
			if ((CoreUtils.nullSafeComparator(
					element.getContract().getSymbol(), symbol) == 0 && null == symbol)
					|| (CoreUtils.nullSafeComparator(element.getContract()
							.getSymbol(), symbol) == 0
							&& element.getStrategy().getName()
									.equals(strategy.getName())
							&& element.getPortfolio().getName()
									.equals(portfolio.getName())
							&& element.getBarSize().equals(barSize)
							&& element.getContract().getCurrency()
									.equals(currency)
							&& element.getContract().getExchange()
									.equals(exchange) && element.getContract()
							.getSecType().equals(secType))) {
				getData().getTradestrategies().remove(element);
				getData().setDirty(true);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				this.fireTableRowsDeleted(selectedRow, selectedRow);
				break;
			}
		}
	}

	public void addRow() {
		System.out.println("Add Row ...");
		Tradingday tradingday = getData();
		Tradestrategy tradestrategy = null;
		String strategyName = null;
		Strategy strategy = (Strategy) DAOStrategy.newInstance().getObject();
		Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
				.getObject();
		Integer chartDays = ChartDays.TWO_DAYS;
		Integer barSize = BarSize.FIVE_MIN;
		Integer riskAmount = new Integer(0);
		if (null != tradingday) {
			try {

				chartDays = ConfigProperties
						.getPropAsInt("trade.backfill.duration");
				if (!ChartDays.newInstance(chartDays).isValid())
					chartDays = new Integer(2);

				barSize = ConfigProperties
						.getPropAsInt("trade.backfill.barsize");
				if (!BarSize.newInstance(barSize).isValid())
					barSize = new Integer(300);

				riskAmount = ConfigProperties.getPropAsInt("trade.risk");
				strategyName = ConfigProperties
						.getPropAsString("trade.strategy.default");
				if (!DAOStrategy.newInstance(strategyName).isValid())
					strategyName = DAOStrategy.newInstance().getCode();

				if (null != strategyName) {
					strategy = (Strategy) DAOStrategy.newInstance(strategyName)
							.getObject();
				}
				tradestrategy = Tradingdays.parseContractLine(ConfigProperties
						.getPropAsString("trade.tradingtab.default.add"));

			} catch (Exception e) {
				// Do nothing
			}

			if (null == tradestrategy) {
				tradestrategy = new Tradestrategy(new Contract(SECType.STOCK,
						"", Exchange.SMART, Currency.USD, null, null),
						tradingday, strategy, portfolio, new BigDecimal(
								riskAmount), null, null, true, chartDays,
						barSize);
			} else {
				tradestrategy.setTradingday(tradingday);
			}

			tradestrategy.setRiskAmount(new BigDecimal(riskAmount));
			tradestrategy.setBarSize(barSize);
			tradestrategy.setChartDays(chartDays);
			tradestrategy.setTrade(true);
			tradestrategy.setDirty(true);
			tradestrategy.setStrategy(strategy);
			tradestrategy.setPortfolio(portfolio);

			getData().getTradestrategies().add(tradestrategy);
			Vector<Object> newRow = new Vector<Object>();

			getNewRow(newRow, tradestrategy);
			rows.add(newRow);

			// Tell the listeners a new table has arrived.
			this.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
		}
	}

	/**
	 * Method getNewRow.
	 * 
	 * @param newRow
	 *            Vector<Object>
	 * @param element
	 *            Tradestrategy
	 */
	public void getNewRow(Vector<Object> newRow, Tradestrategy element) {
		System.out.println("getNewRow ...");
		newRow.addElement(new Date(element.getTradingday().getOpen()));
		newRow.addElement(YesNo.newInstance(element.getTrade()));
		newRow.addElement(element.getContract().getSymbol());
		if (null == element.getSide()) {
			newRow.addElement(new Side());
		} else {
			newRow.addElement(Side.newInstance(element.getSide()));
		}
		newRow.addElement(DAOStrategy.newInstance(element.getStrategy().getName()));
		newRow.addElement(new Money(element.getRiskAmount()));
		/*
		 * TODO If the id is null then this element has not been saved and so
		 * the DatasetContainer cannot be created. This is due to an issue with
		 * hibernate and Eager fetch.
		 */
		if (null != element.getStrategyData()) {
			newRow.addElement(element.getStrategyData().getBaseCandleSeries()
					.getPercentChangeFromClose());
			newRow.addElement(element.getStrategyData().getBaseCandleSeries()
					.getPercentChangeFromOpen());
		} else {
			newRow.addElement(new Percent(0));
			newRow.addElement(new Percent(0));
		}
		newRow.addElement(element.getTradestrategyStatus());
		newRow.addElement(Currency.newInstance(element.getContract().getCurrency()));
		newRow.addElement(SECType.newInstance(element.getContract().getSecType()));
		if (null == element.getContract().getExpiry()) {
			newRow.addElement(new Date());
		} else {
			newRow.addElement(new Date(element.getContract().getExpiry()));
		}
		newRow.addElement(DAOPortfolio.newInstance(element.getPortfolio().getName()));
		newRow.addElement(Exchange.newInstance(element.getContract().getExchange()));
		newRow.addElement(BarSize.newInstance(element.getBarSize()));
		newRow.addElement(ChartDays.newInstance(element.getChartDays()));
		
		
		
		/*
		if (null == element.getTier()) {
			newRow.addElement(Tier.newInstance(Decode.NONE));
		} else {
			newRow.addElement(Tier.newInstance(element.getTier()));
		}
		if (element.getStrategy().hasStrategyManager()) {
			newRow.addElement(DAOStrategyManager.newInstance(element
					.getStrategy().getStrategyManager().getName()));
		} else {
			newRow.addElement(DAOStrategyManager.newInstance(Decode.NONE));
		}
		 */
		
	}
}
