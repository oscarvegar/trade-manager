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
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TradestrategyStatus;
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

public class BreakEvenStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(BreakEvenStrategy.class);

	private Integer openPositionOrderKey = null;
	
	private PersistentModel tradePersistentModel;

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

	public BreakEvenStrategy(BrokerModel brokerManagerModel,
			StrategyData strategyData, Integer idTradestrategy) {
		super(brokerManagerModel, strategyData, idTradestrategy);
	}

	/**
	 * Method runStrategy. Note the current candle is just forming Enter a tier
	 * 1-3 gap in first 5min bar direction, with a 3R target and stop @ 5min
	 * high/low.
	 * 
	 * TradeOrders create TradePositions that are associated to Contracts.
	 * TradeOrders are associated to TradePositions and the Tradestrategy that
	 * created them. A TradePosition may have TradeOrders from multiple
	 * Tradestrategies.
	 * 
	 * TradePositions are created when there is no open TradePosition and a
	 * TradeOrder is either filled or partially filled.
	 * 
	 * Note TradePositions are closed when the open quantity is zero. The new
	 * TradePosition is associated to the Contract with the 1 to 1 relationship
	 * from Contract to TradePosition. The TradeOrder that opened the
	 * TradePosition is marked as the open order @see
	 * org.trade.persistent.dao.TradeOrder.getIsOpenPosition()
	 * 
	 * TradePosition will have the Side set to either BOT/SLD i.e. Long/Short.
	 * If an open position changes from Long to Short dues to an over Sell/Buy
	 * order the side will switch.
	 * 
	 * 
	 * @param candleSeries
	 *            CandleSeries the series of candles that has been updated.
	 * @param newBar
	 *            boolean has a new bar just started.
	 * @see org.trade.strategy.AbstractStrategyRule#runStrategy(CandleSeries,
	 *      boolean)
	 */
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		try {
			// Get the current candle
			CandleItem currentCandleItem = this.getCurrentCandle();
			ZonedDateTime startPeriod = currentCandleItem.getPeriod()
					.getStart();

			/*
			 * Position is open kill this Strategy as its job is done. In this
			 * example we would manage the position with a strategy manager.
			 * This strategy is just used to create the order that would open
			 * the position.
			 */
			if (this.isThereOpenPosition()) {
				_log.info("Strategy complete open position filled symbol: "
						+ getSymbol() + " startPeriod: " + startPeriod);
				/*
				 * If the order is partial filled check if the risk goes beyond
				 * 1 risk unit. If it does cancel the openPositionOrder this
				 * will cause it to be marked as filled.
				 */
				if (OrderStatus.PARTIALFILLED.equals(this
						.getOpenPositionOrder().getStatus())) {
					if (isRiskViolated(currentCandleItem.getClose(), this
							.getTradestrategy().getRiskAmount(), this
							.getOpenPositionOrder().getQuantity(), this
							.getOpenPositionOrder().getAverageFilledPrice())) {
						this.cancelOrder(this.getOpenPositionOrder());
					}
				}
				this.cancel();
				return;
			}

			/*
			 * Open position order was cancelled kill this Strategy as its job
			 * is done.
			 */
			if (null != openPositionOrderKey
					&& !this.getTradeOrder(openPositionOrderKey).isActive()) {
				_log.info("Strategy complete open position cancelled symbol: "
						+ getSymbol() + " startPeriod: " + startPeriod);
				updateTradestrategyStatus(TradestrategyStatus.CANCELLED);
				this.cancel();
				return;
			}

			/*
			 * Create code here to create orders based on your conditions/rules.
			 */
			if (startPeriod.equals(this.getTradestrategy().getTradingday().getOpen().plusMinutes(10))) {
				Candle prevDayCandle = getPreviousDayCandleFromDb(candleSeries, startPeriod);
				Candle currCandle = currentCandleItem.getCandle();
				
				if(prevDayCandle.getLow().doubleValue() == currCandle.getLow().doubleValue()) {

					if (!this.isThereOpenPosition()) {

						int openQuantity = 1000000;

						String action = Action.BUY;
						Money limitPrice = new Money(prevDayCandle.getLow()).add(new Money(0.04));
						Money auxPrice = new Money(prevDayCandle.getLow());
						if (Side.BOT.equals(prevDayCandle.getSide())) {
							action = Action.SELL;
							limitPrice = new Money(prevDayCandle.getLow()).subtract(new Money(0.04));
							auxPrice = new Money(prevDayCandle.getLow());
						}
						TradeOrder tradeOrder = this.createOrder(this.getTradestrategy().getContract(), action, OrderType.STPLMT,
								limitPrice, auxPrice, openQuantity, false, true);

						// tradeOrder = this.submitOrder(this.getTradestrategy().getContract(), tradeOrder);
//						TradeOrder stopAndTargetOrder = this.createStopAndTargetOrder(this.getOpenPositionOrder(), 1, new Money(
//								0.04), 1, new Money(0.04), openQuantity, true);
					}

				} else {
					this.cancel();
				}
			}

			if (startPeriod.isBefore(this.getTradestrategy().getTradingday()
					.getOpen().minusMinutes(35))
					&& startPeriod.isAfter(this.getTradestrategy()
							.getTradingday().getOpen().plusMinutes(10))) {
				
				Candle prevDayCandle = getPreviousDayCandleFromDb(candleSeries, startPeriod);

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
				
				if(currentCandleItem.getLow() > prevCandleItem.getLow() 
						&& currentCandleItem.getLow() >= addAPercentToANumber(prevDayCandle.getLow().doubleValue(), 50)) {
					Money stopPrice = addPennyAndRoundStop(this
							.getOpenPositionOrder().getAverageFilledPrice()
							.doubleValue(), getOpenTradePosition()
							.getSide(), Action.BUY, 0.04);
					moveStopOCAPrice(stopPrice, true);
				}
				/*
				if (Side.BOT.equals(getOpenTradePosition().getSide())) {
					if (currentCandleItem.getVwap() < prevCandleItem.getVwap()) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.SELL, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ prevCandleItem.getVwap() + " Curr Vwap: "
								+ currentCandleItem.getVwap());
					}
				} else {

					if (currentCandleItem.getVwap() > prevCandleItem.getVwap()) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.BUY, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ prevCandleItem.getVwap() + " Curr Vwap: "
								+ currentCandleItem.getVwap());
					}
				}
				*/
			}

			/*
			 * Close any opened positions with a market order at day end minus
			 * one bar.
			 */
			if (!currentCandleItem.getLastUpdateDate().isBefore(
					this.getTradestrategy()
							.getTradingday()
							.getClose()
							.minusMinutes(
									this.getTradestrategy().getBarSize() / 60))) {
				cancelOrdersClosePosition(true);
				_log.info("Rule 15:55:00 close all open positions: "
						+ getSymbol() + " Time: " + startPeriod);
				this.cancel();
			}
		} catch (StrategyRuleException | PersistentModelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException ex) {
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
		
		List<Candle> candleList = this.tradePersistentModel.findCandlesByContractDateRangeBarSize(
				candleSeries.getContract().getIdContract(), startPeriod.minusDays(days).minusMinutes(10),
				startPeriod.minusDays(days).minusMinutes(10), candleSeries.getBarSize());
		
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
		
		percentOfNumber = number * (percent / 100);
		
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
		
		numberPlusPercent = number * (1 + (percent / 100));
		
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
		
		numberMinusPercent = number * (1 - (percent / 100));
		
		return numberMinusPercent;
	}
}
