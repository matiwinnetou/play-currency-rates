package controllers

import play.api.mvc._
import models.CurrencyRates
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {

  def index = Action.async {
      rateForCurrencySymbol(List("PLN", "GBP"))
      .map(currencies => Ok(views.html.index(currencies)))
  }

  def rateForCurrencySymbol(currencySymbol: String): Future[Seq[CurrencyRates.CurrencyRate]]  = rateForCurrencySymbol(List[String](currencySymbol))

  def rateForCurrencySymbol(currencySymbol: List[String]): Future[Seq[CurrencyRates.CurrencyRate]] = {
    for {
      r <- CurrencyRates.fetch(cache = true)
    } yield r.filter(r => currencySymbol.contains(r.symbol))
  }

}
