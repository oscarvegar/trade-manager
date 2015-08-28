/* ===========================================================
 * Smart Trade System: An application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.fff
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
package org.trade.strategy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.factory.ClassFactory;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.TradeOrder;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class PosMgrDoubleBottomStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrDoubleBottomStrategy.class);
	
	private PersistentModel tradePersistentModel;

	private static int QuantityShares;
	
	// private boolean StrategyOK = false;
	// private CandleItem CandlePositionA;
	// private CandleItem CandlePositionB;
	// private CandleItem CandlePositionBC;
	// private CandleItem CandlePositionC;
	
	// private Double LastAuxStopPrice;
	// private Double LastLowDecrease;
	// private Double LastLowIncrease;

	/**
	 * Default Constructor Note if you use class variables remember these will
	 * need to be initialized if the strategy is restarted i.e. if they are
	 * created on startup under a constraint you must find a way to populate
	 * that value if the strategy were to be restarted and the constraint is not
	 * met.
	 * 
	 * @param brokerManagerModel
	 *            BrokerModel
	 * @param strategyData
	 *            StrategyData
	 * @param idTradestrategy
	 *            Integer
	 */

	public PosMgrDoubleBottomStrategy(BrokerModel brokerManagerModel,
			StrategyData strategyData, Integer idTradestrategy) {
		super(brokerManagerModel, strategyData, idTradestrategy);
	}

	/*
	 * Note the current candle is just forming Enter a tier 1-3 gap in first
	 * 5min bar direction, with a 3R target and stop @ 5min high/low
	 * 
	 * @param candleSeries the series of candles that has been updated.
	 * 
	 * @param newBar has a new bar just started.
	 */
	/**
	 * Method runStrategy.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @param newBar
	 *            boolean
	 * @see org.trade.strategy.StrategyRule#runStrategy(CandleSeries, boolean)
	 */
	@SuppressWarnings("unused")
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		_log.info("Inside PosMgrDoubleBottomStrategy.runStrategy::" + this.getSymbol() + "_at_"
				+ this.getCurrentCandle().getPeriod().getStart());

		try {
			// Get the current candle
			CandleItem currentCandleItem = (CandleItem) candleSeries
					.getDataItem(getCurrentCandleCount());
			ZonedDateTime startPeriod = currentCandleItem.getPeriod()
					.getStart();
			QuantityShares = ((Long) this.getTradestrategy().getValueCode("stockSharesQuantity")).intValue();

			// _log.info(getTradestrategy().getStrategy().getClassName()
			// + " symbol: " + getSymbol() + " startPeriod: "
			// + startPeriod);

			if (!this.isThereOpenPosition()) {
				_log.info("No open position so Cancel Strategy Mgr Symbol: "
						+ getSymbol() + " Time:" + startPeriod);
				// this.cancel();
				return;
			} else {

			/* Is it the the 9:35 candle?
			 * 
			 * 
			 * Validamos que la estrategia se ejecute en el periodo de 9:00am a 15:00pm
			 */
			/*
			if (startPeriod.isAfter(this.getTradestrategy().getTradingday().getOpen().minusSeconds(1))
					&& startPeriod.isBefore(this.getTradestrategy().getTradingday().getClose().plusSeconds(1))
					) {	// && newBar
			 */
				/*
				 * Is the candle in the direction of the Tradestrategy side i.e.
				 * a long play should have a green 5min candle
				 */
				CandleItem prevCandleItem = null;
				if (getCurrentCandleCount() > 0) {
					prevCandleItem = (CandleItem) candleSeries
							.getDataItem(getCurrentCandleCount() - 1);
					// AbstractStrategyRule
					// .logCandle(this, prevCandleItem.getCandle());
				}
				
				double lastAuxStopPrice = this.getOpenPositionOrder().getAuxPrice().doubleValue();
				if(currentCandleItem.getClose() > prevCandleItem.getClose()
						&& currentCandleItem.getClose() >= addAPercentToANumber(lastAuxStopPrice, 50)) {

					Money auxStopPrice = new Money(addAPercentToANumber(lastAuxStopPrice, 50))
							.subtract(new Money(0.04));
					Money limitPrice = new Money(addAPercentToANumber(lastAuxStopPrice, 50));
					
					TradeOrder tradeOrder = this.updateOrder(this.getOpenPositionOrder().getOrderKey(),
							Action.BUY, OrderType.STPLMT, limitPrice, auxStopPrice, QuantityShares,
							false, true);
					this.reFreshPositionOrders();
					
				}
				
			}

			/*
			 * Close any opened positions with a market order at the end of the
			 * day.
			 *
			if (!currentCandleItem.getLastUpdateDate().isBefore(
					this.getTradestrategy().getTradingday().getClose()
							.minusMinutes(2))) {
				cancelOrdersClosePosition(true);
				_log.info("Rule 15:58:00 close all open positions: "
						+ getSymbol() + " Time: " + startPeriod);
				this.cancel();
			}
			 */
		} catch (StrategyRuleException | PersistentModelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		} catch (Exception ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @param candleSeries
	 * @param startPeriod
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws PersistentModelException
	 * @throws StrategyRuleException
	 */
	public Candle getPreviousDayCandleFromDb(CandleSeries candleSeries, ZonedDateTime startPeriod)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException, IOException, PersistentModelException, StrategyRuleException {
		Candle prevCandle = null;

		this.tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel,
						this);

		long days = 1;
		if(DayOfWeek.MONDAY.equals((startPeriod.getDayOfWeek()))) {
			days = 3;
		}
		
		ZonedDateTime newStartPeriod = this.getTradestrategy().getTradingday().getOpen();
		List<Candle> candleList = this.tradePersistentModel.findCandlesByContractDateRangeBarSize(
				candleSeries.getContract().getIdContract(), newStartPeriod.minusDays(days),
				newStartPeriod.minusDays(days), candleSeries.getBarSize());
		
		if(candleList.size() > 0) {
			prevCandle = candleList.get(0);
		}
		
		return prevCandle;
	}
	
	/**
	 * 
	 * @param number
	 * @param percent
	 * @return
	 */
	public double getPercentOfANumber(double number, int percent) {
		double percentOfNumber = 0.0;
		
		percentOfNumber = number * (percent / (double) 100);
		
		return percentOfNumber;
	}
	
	/**
	 * 
	 * @param number
	 * @param percent
	 * @return
	 */
	public double addAPercentToANumber(double number, int percent) {
		double numberPlusPercent = 0.0;
		
		numberPlusPercent = number * ((double) 1 + (percent / (double) 100));
		
		return numberPlusPercent;
	}
	
	/**
	 * 
	 * @param number
	 * @param percent
	 * @return
	 */
	public double substractAPercentToANumber(double number, int percent) {
		double numberMinusPercent = 0.0;
		
		numberMinusPercent = number * ((double) 1 - (percent / (double) 100));
		
		return numberMinusPercent;
	}
}
