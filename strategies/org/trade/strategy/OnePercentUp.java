/* ===========================================================
 * Smart Trade System: An application to trade strategies for the Java(tm) platform
 * ===========================================================
 * ELITEWARE IO. Daniel Morales.
 *
 * Changes
 * -------
 *
 */
package org.trade.strategy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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

public class OnePercentUp extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(BreakEvenStrategy.class);

	private Integer openPositionOrderKey = null;
	
	private PersistentModel tradePersistentModel;

	private static int QuantityShares;
	
	private boolean StrategyOK = false;
	
	private Double LastAuxStopPrice;

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

	public OnePercentUp(BrokerModel brokerManagerModel,
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
	@SuppressWarnings("unused")
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		_log.info("Inside BreakEvenStrategy.runStrategy::" + this.getSymbol() + "_at_"
				+ this.getCurrentCandle().getPeriod().getStart());
		
		try {
			// Get the current candle
			CandleItem currentCandleItem = this.getCurrentCandle();
			ZonedDateTime startPeriod = currentCandleItem.getPeriod()
					.getStart();
//			QuantityShares = this.getTradestrategy().getCodeValues().get(0).getCodeValue();
//			QuantityShares = this.getTradestrategy().getCodeValues().get(0).getCodeAttribute().getDefaultValue();
			QuantityShares = ((Long) this.getTradestrategy().getValueCode("stockSharesQuantity")).intValue();

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
				// this.cancel();
				return;
			} else 

			/*
			 * Open position order was cancelled kill this Strategy as its job
			 * is done.
			 */
			if (null != openPositionOrderKey
					&& !this.getTradeOrder(openPositionOrderKey).isActive()) {
				_log.info("Strategy complete open position cancelled symbol: "
						+ getSymbol() + " startPeriod: " + startPeriod);
				updateTradestrategyStatus(TradestrategyStatus.CANCELLED);
				// this.cancel();
				return;
			} else {
			
			/*
			 * Create code here to create orders based on your conditions/rules.
			 *
			 *
			 * Validamos que la estrategia solo se ejecute dentro del periodo de 9:30am a 15:00pm
			 */
			/*
			if (startPeriod.isAfter(this.getTradestrategy().getTradingday().getOpen().minusSeconds(1))
					&& startPeriod.isBefore(this.getTradestrategy().getTradingday().getClose().plusSeconds(1))
					) {	// && newBar
			 */
				/*
				 * Example On start of the second (9:35) candle check the 9:30
				 * candle and buy over under in the direction of the bar.
				 *
				 *
				 * Validamos que no hayan transcurrido 25min (despues de la apertura) y que la estrategia
				 * no se haya cumplido
				 **/
				if(!StrategyOK) {
					
					Candle currentCandle = currentCandleItem.getCandle();
					Candle prevDayCandle = getPreviousDayCandleFromDb(candleSeries, startPeriod);	// Obtenemos el punto P, es decir el punto de apertura del día anterior
					DecimalFormat df = new DecimalFormat("#.00");
					BigDecimal currentClose = new BigDecimal(currentCandle.getClose().doubleValue()).setScale(2, RoundingMode.HALF_EVEN);
					BigDecimal preDayClose = new BigDecimal(prevDayCandle.getClose().doubleValue()).setScale(2, RoundingMode.HALF_EVEN);
					CandleItem openCandle = this.getCandle(this.getTradestrategy()
							.getTradingday().getOpen());
					
					Double onePercentFromOpen = openCandle.getOpen()*1.01;
					
					if(currentClose.doubleValue() >= onePercentFromOpen) {	// Validamos que el valor actual de LOW sea igual al de P
						StrategyOK = Boolean.TRUE;
						
						if(!this.isThereOpenPosition()) {	// Siempre que no haya una orden abierta ...
							//Money auxStopPrice = new Money(prevDayCandle.getLow());
							Money auxStopPrice = new Money(prevDayCandle.getClose()).subtract(new Money(0.04));
							Money limitPrice = new Money(prevDayCandle.getClose());
							LastAuxStopPrice = auxStopPrice.doubleValue();
							
							TradeOrder tradeOrder = this.createOrder(this.getTradestrategy().getContract(),
									Action.BUY, OrderType.STPLMT, limitPrice, auxStopPrice, QuantityShares, false, true);	// Creamos y transmitimos una orden BUY, STPLMT = LOW - 4c
						}
						
						_log.info("StrategyOK::" + StrategyOK + ", LastAuxStopPrice::" + LastAuxStopPrice + ", Symbol::" + this.getSymbol());
						
						/*
						TradeOrder tradeOrder = new TradeOrder(this.getTradestrategy(),
								Action.BUY, OrderType.STPLMT, 100, price,
								price.add(new BigDecimal(0.02)),
								TradingCalendar.getDateTimeNowMarketTimeZone());
						//tradeOrder.setClientId(clientId);
						tradeOrder.setTransmit(new Boolean(true));
						//tradeOrder.setStatus(OrderStatus.UNSUBMIT);
						this.submitOrder(this.getTradestrategy().getContract(), tradeOrder);
						*/
					}
					
				} else if(StrategyOK) {	// Si se ejecuto la estrategia...
					
					// Candle prevDayCandle = getPreviousDayCandleFromDb(candleSeries, startPeriod);

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

					this.reFreshPositionOrders();
					// double lastAuxStopPrice = this.getOpenPositionOrder().getAuxPrice().doubleValue();
					if(currentCandleItem.getClose() > prevCandleItem.getClose() 
							&& currentCandleItem.getClose() >= addAPercentToANumber(LastAuxStopPrice, 50)) {
						/*
						 * Validamos que haya un incremento en LOW entre la
						 * posicion actual y la posicion anterior del dia; y
						 * además, que el incremento de LOW del dia sea mayor o
						 * igual a un 50% de LOW del dia anterior
						 */
						
						/*
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.BUY, 0.04);
						moveStopOCAPrice(stopPrice, true);
						*/

						Money auxStopPrice = new Money(addAPercentToANumber(LastAuxStopPrice, 50))
								.subtract(new Money(0.04));
						Money limitPrice = new Money(addAPercentToANumber(LastAuxStopPrice, 50));
						LastAuxStopPrice = auxStopPrice.doubleValue();
						
						TradeOrder tradeOrder = this.updateOrder(this.getOpenPositionOrder().getOrderKey(),
								Action.BUY, OrderType.STPLMT, limitPrice, auxStopPrice, QuantityShares,
								false, true);	// Creamos y transmitimos una orden BUY, STPLMT = (LOW + 50%) - 4c
						this.reFreshPositionOrders();
					}
					_log.info("StrategyOK::" + StrategyOK + ", LastAuxStopPrice::" + LastAuxStopPrice + ", Symbol::" + this.getSymbol());
				}
				
			}

			/*
			 * Close any opened positions with a market order at day end minus
			 * one bar.
			 *
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
             */
		} catch (StrategyRuleException | PersistentModelException | ClassNotFoundException
				| InstantiationException | IllegalAccessException | NoSuchMethodException
				| InvocationTargetException | IOException ex) {
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
