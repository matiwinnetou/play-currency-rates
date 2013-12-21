package models

import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.cache.Cache
import scala.xml.XML._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

object CurrencyRates {

  case class CurrencyRate(symbol: String, rate: Float)

  def fetch(cache: Boolean = false, expirationInSecs: Int = 3600): Future[Seq[CurrencyRate]] = {
    if (cache) {
      Cache.getOrElse("currencyrates.rates", expirationInSecs) {
        fetchAndConvert()
      }
    } else {
      fetchAndConvert()
    }
  }

  private[models] def fetchAndConvert() = fetchRatesXml.map(response => convert(response.body))

  private[models] def fetchRatesXml() = WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").get()

  private[models] def convert(xml: String) = (loadString(xml) \ "Cube" \ "Cube" \ "Cube")
    .map(node => CurrencyRate((node \ "@currency").text, (node \ "@rate").text.toFloat))

}
