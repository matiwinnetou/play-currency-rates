package controllers

import play.api.mvc._
import models.{CurrencyData, CurrencyRates}
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action.async {
    val ratesF = CurrencyRates.fetchCurrencyRates(cache = false).recover(recoverFn[CurrencyRates.CurrencyRate])
    val infosF = CurrencyRates.fetchCurrencyNames(cache = false).recover(recoverFn[CurrencyRates.CurrencyInfo])

    for {
      rates <- ratesF
      infos <- infosF
    } yield Ok(views.html.index(toCurrencyData(rates, infos)))
  }

  def toCurrencyData(currencyRates: Seq[CurrencyRates.CurrencyRate], currencyInfos: Seq[CurrencyRates.CurrencyInfo]) = for {
    currencyRate <- currencyRates
    currencyInfo = currencyInfos.filter(info => info.symbol == currencyRate.symbol).headOption
    currencyName = currencyInfo.map(currencyInfo => currencyInfo.name)
  } yield CurrencyData(currencyRate.symbol, currencyName, rate = currencyRate.rate)

  def recoverFn[T]: PartialFunction[Throwable, List[T]] = {
    case t: Throwable => List()
  }

}
