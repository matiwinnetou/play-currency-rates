package models

import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.cache.Cache
import scala.xml.XML._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.libs.json.Json

object CurrencyRates {

  case class CurrencyRate(symbol: String, rate: Float)
  case class CurrencyInfo(symbol: String, name: String)

  def fetchCurrencyRates(cache: Boolean = false, expirationInSecs: Int = 3600): Future[Seq[CurrencyRate]] =
    if (cache) Cache.getOrElse("currency.rates", expirationInSecs) { getRates() }
      else getRates()

  def fetchCurrencyNames(cache: Boolean = false, expirationInSecs: Int = 3600): Future[Seq[CurrencyInfo]] =
    if (cache) Cache.getOrElse("currency.names", expirationInSecs) { getNames() }
      else getNames()

  private[models] def getNames() = fetchNamesJson().map(response => {
      val set = for {
        (currencySymbol, value) <- Json.parse(response.body).asInstanceOf[play.api.libs.json.JsObject].fieldSet
        currencyName = (value.asInstanceOf[play.api.libs.json.JsObject] \ "name").toString()
      } yield CurrencyInfo(currencySymbol, currencyName)

       set.toSeq
    })

  private[models] def getRates() = fetchRatesXml.map(response => {
    val node = loadString(response.body)
    (node \ "Cube" \ "Cube" \ "Cube")
      .map(node => CurrencyRate((node \ "@currency").text, (node \ "@rate").text.toFloat))
  })

  private[models] def fetchRatesXml() = WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").get()

  private[models] def fetchNamesJson() = WS.url("https://gist.github.com/Fluidbyte/2973986/raw/9ead0f85b6ee6071d018564fa5a314a0297212cc/Common-Currency.json").get

}
