package controllers

import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.CurrencyRates.{CurrencyRate, CurrencyInfo}
import models.{CurrencyData, CurrencyRates}

object Application extends Controller {

  def index = Action.async {
    val ratesFuture: Future[Seq[CurrencyRate]] = CurrencyRates.fetchCurrencyRates(cache = true)
      .recover(recoverFn[CurrencyRate])

    val infosFuture: Future[Seq[CurrencyInfo]] = CurrencyRates.fetchCurrencyNames(cache = true)
      .recover(recoverFn[CurrencyInfo])

    for {
      rates <- ratesFuture
      infos <- infosFuture
    } yield Ok(views.html.index(toCurrencyData(rates, infos)))
  }

  def toCurrencyData(currencyRates: Seq[CurrencyRate], currencyInfos: Seq[CurrencyInfo]) = for {
    currencyRate <- currencyRates
    currencyInfo = currencyInfos.filter(info => info.symbol == currencyRate.symbol).headOption
    currencyName = currencyInfo.map(currencyInfo => currencyInfo.name)
  } yield CurrencyData(currencyRate.symbol, currencyName, rate = currencyRate.rate)

  def recoverFn[T]: PartialFunction[Throwable, List[T]] = {
    case t: Throwable => List[T]()
  }

}
