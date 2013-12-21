package controllers

import play.api.mvc._
import models.{CurrencyData, CurrencyRates}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action.async {
    val currList = List("PLN", "GBP", "NOK")
    for {
      rates <- ratesForCurrencySymbol(currList)
      infos <- namesForCurrencySymbol(currList)
    } yield Ok(views.html.index(toCurrencyData(rates, infos)))
  }

  def all = Action.async {
    for {
      rates <- CurrencyRates.fetchCurrencyRates(cache = true)
      infos <- CurrencyRates.fetchCurrencyNames(cache = true)
    } yield Ok(views.html.index(toCurrencyData(rates, infos)))
  }

  def toCurrencyData(rates: Seq[CurrencyRates.CurrencyRate], infos: Seq[CurrencyRates.CurrencyInfo]) = for {
    rate <- rates
    info = infos.filter(info => info.symbol == rate.symbol).headOption
  } yield CurrencyData(rate.symbol, info.map(info => info.name).getOrElse("?"), rate = rate.rate)

  def name(currencySymbol: String): Future[Seq[CurrencyRates.CurrencyRate]] = ratesForCurrencySymbol(List[String](currencySymbol))

  def rateForCurrencySymbol(currencySymbol: String): Future[Seq[CurrencyRates.CurrencyRate]] = ratesForCurrencySymbol(List[String](currencySymbol))

  def nameForCurrencySymbol(currencySymbol: String): Future[Seq[CurrencyRates.CurrencyRate]] = ratesForCurrencySymbol(List[String](currencySymbol))

  def ratesForCurrencySymbol(currencySymbol: List[String]): Future[Seq[CurrencyRates.CurrencyRate]] = {
    for {
      rates: Seq[CurrencyRates.CurrencyRate] <- CurrencyRates.fetchCurrencyRates(cache = true)
    } yield rates.filter(rate => currencySymbol.contains(rate.symbol))
  }

  def namesForCurrencySymbol(currencySymbol: List[String]): Future[Seq[CurrencyRates.CurrencyInfo]] = {
    for {
      infos: Seq[CurrencyRates.CurrencyInfo] <- CurrencyRates.fetchCurrencyNames(cache = true)
    } yield infos.filter(info => currencySymbol.contains(info.symbol))
  }

}
