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
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.trade.persistent.dao.TradeOrder;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem; 

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class ConsolidationStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	
	private static Map<String, CandleItem> stocksInRevision = new ConcurrentHashMap<String, CandleItem>();
	private static Map<String, CandlePivote> stocksPivoting = new ConcurrentHashMap<String, CandlePivote>();
	private static int cantidadCompra = 1;
	private final static Logger _log = LoggerFactory
			.getLogger(ConsolidationStrategy.class);
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

	public ConsolidationStrategy(BrokerModel brokerManagerModel,
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
				_log.info("La posicion esta abierta para el symbol: "
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
			}else{
				try{
					CandlePivote pivote = stocksPivoting.get( getSymbol() );
					
					if( pivote == null ){
						System.out.println("PRIMER ACCESO>>>>");
						stocksPivoting.put( getSymbol() , new CandlePivote( currentCandleItem ));
						System.out.println(" * ******************************************** *");
						return;
					}
					
					CandleItem pivoteCandle = pivote.getPivote();
					System.out.println(" * PIVOTE *");
					System.out.println( pivoteCandle.getCandle().getContract() + " : " + 
							pivoteCandle.getPeriod() );
					System.out.println("LOW: "+ pivoteCandle.getLow()	);
					System.out.println("HIGH: "+ pivoteCandle.getHigh() );
					System.out.println("CLOSE: "+ pivoteCandle.getClose() );
					System.out.println(" * CURRENT *");
					System.out.println( getSymbol() + " : " + currentCandleItem.getPeriod() );
					System.out.println("LOW: "+ currentCandleItem.getLow()	);
					System.out.println("HIGH: "+ currentCandleItem.getHigh() );
					System.out.println("CLOSE: "+ currentCandleItem.getClose() );
					System.out.println(" * DATA *");
					long diff = currentCandleItem.getPeriod().getFirstMillisecond() -
							pivoteCandle.getPeriod().getFirstMillisecond();
					long diffMinutes = diff / (60 * 1000) % 60;
					double diffLows =  Math.abs(currentCandleItem.getLow() - pivoteCandle.getLow());
					double diffHighs =  Math.abs(currentCandleItem.getHigh() - pivoteCandle.getHigh());
					System.out.println(getSymbol() + " :: Diff Low: "+ diffLows );
					System.out.println(getSymbol() + " :: Diff High: "+ diffHighs );
					System.out.println(getSymbol() + " :: TimeElapsed: "+ diffMinutes + " min. ");
					
					if( pivote.isCandidato() ){
						if( diffLows < 0.05 && diffHighs < 0.15 ){
							// Esta dentro del rango de valores deseados, checamos el tiempo que ha pasado
							// 180 minutos =  3 hrs
							System.out.println(getSymbol() + " sigue siendo candidato");
							if( diffMinutes > 180 ){
								// Si el tiempo es mayor a 3 horas, eliminamos el monitoreo
								System.out.println(getSymbol() + " ya sobrepaso las 3 hrs");
								pivote.setCandidato(false);
								stocksPivoting.put( getSymbol() , pivote);
							}
							
						}else{
							// Salio fuera del rango, verificamos si hacemos un buy o un sell
							System.out.println(getSymbol() + " es candidato y salio fuera del rango");
							if( diffLows > 0.20 ){
								// El stock sobrepaso los 20 centavos a la baja
								System.out.println(getSymbol() + " sobrepaso 20 centavos a la baja. SELL");
								Money limitPrice = new Money( currentCandleItem.getLow() );
								Money auxStopPrice = new Money(currentCandleItem.getLow()).add(new Money(0.10));
								System.out.println(getSymbol() + " LIMIT PRICE " + limitPrice);
								System.out.println(getSymbol() + " STOP PRICE " + auxStopPrice);
								TradeOrder tradeOrder = this.createOrder(this.getTradestrategy().getContract(),
										Action.SELL, OrderType.STPLMT, limitPrice, auxStopPrice, cantidadCompra, false, true);	// Creamos y transmitimos una orden BUY, STPLMT = LOW - 4c
								this.reFreshPositionOrders();
								System.out.println(" * ******************************************** *");
								return;
							}
							
							if( diffHighs > 0.20 ){
								System.out.println(getSymbol() + " sobrepaso 20 centavos hacia arriba. COMPRAR");
								Money limitPrice = new Money( currentCandleItem.getHigh() );
								Money auxStopPrice = new Money(currentCandleItem.getHigh()).subtract(new Money(0.10));
								System.out.println(getSymbol() + " LIMIT PRICE " + limitPrice);
								System.out.println(getSymbol() + " STOP PRICE " + auxStopPrice);
								TradeOrder tradeOrder = this.createOrder(this.getTradestrategy().getContract(),
										Action.BUY, OrderType.STPLMT, limitPrice, auxStopPrice, cantidadCompra, false, true);	// Creamos y transmitimos una orden BUY, STPLMT = LOW - 4c
								this.reFreshPositionOrders();
								System.out.println(" * ******************************************** *");
								return;
							}
							
						}
						System.out.println(" * ******************************************** *");
						return;
					}
					
					if( diffLows < 0.05 && diffHighs < 0.15 ){
						// Esta en el rango de valores deseados
						if( diffMinutes > 20 ){
							// Ya han pasado mas de 20 minutos y el valor se sigue conservando
							// Definimos al pivote como candidato para ser comprado o vendido
							pivote.setCandidato(true);
							stocksPivoting.put( getSymbol() , pivote);
						}else{
							//Conservamos el pivote, aun no se llega al tiempo limite
							System.out.println("CONSERVANDO PIVOTE, SIGUE EN OBSERVACION ");
							pivote.setCandidato(false);
							stocksPivoting.put( getSymbol() , pivote);
						}
					}else{
						// Esta fuera del rango de valores deseados, cambiamos el pivote al currentCandle
						System.out.println("CAMBIANDO PIVOTE");
						pivote.setPivote(currentCandleItem);
						pivote.setCandidato(false);
						stocksPivoting.put( getSymbol() , pivote);
					}
					
					System.out.println(" * ******************************************** *");
				
				}catch(Exception ex) {
					_log.error("Error  runRule exception: " + ex.getMessage(), ex);
					error(1, 10, "Error  runRule exception: " + ex.getMessage());
				}
				
			}

			/*
			 * Create code here to create orders based on your conditions/rules.
			 */

			//getCandle(startPeriod)
			
//			
//			if (startPeriod.equals(this.getTradestrategy().getTradingday()
//					.getOpen()
//					.plusMinutes(this.getTradestrategy().getBarSize() / 60))
//					&& newBar) {
//				
//				/*
//				 * Example On start of the second (9:35) candle check the 9:30
//				 * candle and buy over under in the direction of the bar.
//				*/
//				
//				
//			}

			/*
			 * Close any opened positions with a market order at day end minus
			 * one bar.
			
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
		} catch (StrategyRuleException ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		}
	}
	
	private static synchronized void process(String symbol, CandleItem item){
		
	}
	
	private static class CandlePivote {
		
		private CandleItem pivote;
		private Integer minutos;
		private boolean candidato;
		
		public CandlePivote(CandleItem currentCandleItem) {
			// TODO Auto-generated constructor stub
			this.pivote = currentCandleItem;
			this.candidato = false;
		}

		public CandleItem getPivote() {
			return pivote;
		}
		
		public void setPivote(CandleItem pivote) {
			this.pivote = pivote;
		}
		
		public Integer getMinutos() {
			return minutos;
		}
		
		public void setMinutos(Integer minutos) {
			this.minutos = minutos;
		}

		public boolean isCandidato() {
			return candidato;
		}

		public void setCandidato(boolean candidato) {
			this.candidato = candidato;
		}
		
	}
	
	
}



