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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class RDReversalStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(RDReversalStrategy.class);
	private PersistentModel tradePersistentModel;
	private ZonedDateTime fechaApertura2PM;
	private CandleItem candlePositionA;
	private CandleItem candlePositionB;
	private boolean isStrategyInValid;
	private boolean checkingForC;
	private static Candle prevCandle;
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

	public RDReversalStrategy(BrokerModel brokerManagerModel,
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
			Object paramStock = this.getTradestrategy().getValueCode("stockSharesQuantity");
			int cantidadCompra = 1;
			if( paramStock != null ){
				cantidadCompra = ((Long)paramStock).intValue();
			}
			
			if (this.isThereOpenPosition()) {
				_log.info("Strategy complete open position filled symbol: "
						+ getSymbol() + " startPeriod: " + startPeriod);
			}else{
				// Si la estrategia es invalida, ya no sera necesario continuar el flujo
				if(isStrategyInValid) {
					return;
				}
				
				// Si la estrategia esta en el punto donde solo debemos de vigilar el punto C
				if( checkingForC ){
					//Si la cotizacion actual esta en el rango +- 0.1 del punto B, compramos
					if( currentCandleItem.getClose() >= (candlePositionB.getClose() + 0.1) ){
						System.out.println("SE ENCONTRO EL PUNTO C EN " + currentCandleItem.getPeriod());
						Money limitPrice = new Money( currentCandleItem.getClose() * 1.1 );
						Money auxStopPrice = new Money((currentCandleItem.getClose() * 0.9 ));
						this.createOrder(this.getTradestrategy().getContract(),
								Action.BUY, OrderType.STPLMT, limitPrice, auxStopPrice, cantidadCompra, false, true);
						this.reFreshPositionOrders();
						_log.info(" * ******************************************** *");
					}
					return;
				}
				
				_log.info("Strategy is closed: " + getSymbol() + " startPeriod: " + startPeriod);
				ZonedDateTime fechaApertura = this.getTradestrategy().getTradingday().getOpen();
				System.out.println("Fecha de apertura:: " + fechaApertura );
				if(fechaApertura2PM == null) {
					fechaApertura2PM = fechaApertura.plusHours(4);
					fechaApertura2PM = fechaApertura2PM.plusMinutes(30);
				}
				
				System.out.println("Fecha de rango inicial:: " + fechaApertura );
				System.out.println("Fecha de rango final:: " + fechaApertura2PM );
				
				// Si la cotizacion esta en el rango de tiempos definidos entre 9:30am y 2:00pm 
				if( startPeriod.isAfter(fechaApertura) && startPeriod.isBefore(fechaApertura2PM) ){
					//Si aun no encotramos el puntoA checamos el candle actual
					if( candlePositionA == null ){
						//Recuperamos si no exite el candle mas bajo del dia anterior
						if( prevCandle == null ){
							System.out.println("El candle anterior no existe, loe recuperamos de la BD");
							prevCandle = getPreviousDayCandleFromDb(candleSeries, startPeriod);
						}
						System.out.println("Candle mas bajo del dia anterior: " + prevCandle.getClose());
						if( currentCandleItem.getClose() >= (prevCandle.getClose().doubleValue() - 0.1) && 
								currentCandleItem.getClose() <= (prevCandle.getClose().doubleValue() + 0.1)){
							System.out.println("SE ENCONTRO EL PUNTO A EN " + currentCandleItem.getPeriod());
							this.candlePositionA = currentCandleItem;
						}
					}else if( candlePositionA != null && candlePositionB == null ){
						// Si la posicion actual sube con respecto al punto A, se cancela la estrategia
						if( currentCandleItem.getClose() > candlePositionA.getClose() ){
							isStrategyInValid = true;
							return;
						}
						// 
						if( new BigDecimal(currentCandleItem.getClose()).setScale(2, RoundingMode.HALF_UP).doubleValue()
								== prevCandle.getClose().setScale(2, RoundingMode.HALF_UP).doubleValue() ){
							System.out.println("SE ENCONTRO EL PUNTO B EN " + currentCandleItem.getPeriod());
							checkingForC = true;
						}
					}
					
				}
				
			}
		} catch (Exception ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		}
	}
			
	public Candle getPreviousDayCandleFromDb(CandleSeries candleSeries, ZonedDateTime startPeriod)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException, IOException, PersistentModelException, StrategyRuleException {
		Candle prevCandle = null;
		if(tradePersistentModel == null){
			tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
		}

		long days = 1;
		if(DayOfWeek.MONDAY.equals((startPeriod.getDayOfWeek()))) {
			days = 3;
		}
		
		ZonedDateTime newStartPeriod = this.getTradestrategy().getTradingday().getOpen();
		List<Candle> candleList = tradePersistentModel.findCandlesByContractDateRangeBarSize(
				candleSeries.getContract().getIdContract(), newStartPeriod.minusDays(days),
				newStartPeriod.minusDays(days), candleSeries.getBarSize());
		
		for( Candle candle : candleList ){
			if( prevCandle == null ) {
				prevCandle = candle;
				continue;
			}
			if( candle.getClose().compareTo(prevCandle.getClose()) < 0 ){
				//El cierre del candle actual es menor al cierre del candle anterior
				prevCandle = candle;
			}
		}
		
		return prevCandle;
	}
}
