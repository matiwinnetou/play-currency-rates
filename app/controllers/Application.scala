package controllers

import play.api.mvc._
import play.api.libs.ws.WS
import scala.xml.XML.loadString
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.cache.Cache
import scala.concurrent.Future
import play.api.Play.current

object Application extends Controller {

  def index = Action.async {
    rates.map(currencies => Ok(views.html.index(currencies)))
  }

  def index2 = Action.async { request =>
    ratesXml.map(response => Ok(response.body).as("application/xml"))
  }

  def fetchRates() = ratesXml.map(response => currencies(response.body))

  def ratesXml = {
    WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
      .get()
  }

  def rates() = {
    Cache.getOrElse[Future[Seq[Currency]]](key = "rates", expiration = 10 * 60) {
      println("exec")
      fetchRates
    }
  }

  case class Currency(symbol: String, rate: Float)

  def currencies(xml: String) = {
    (loadString(xml) \ "Cube" \ "Cube" \ "Cube")
      .map(n => Currency((n \ "@currency").text, (n \ "@rate").text.toFloat))
  }

}
