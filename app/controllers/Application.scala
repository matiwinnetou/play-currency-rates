package controllers

import play.api.mvc._
import play.api.libs.ws.WS
import scala.xml.XML.loadString
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.cache.{EhCachePlugin, Cache}
import scala.concurrent.{Promise, Future}
import play.api.Play.current
import scala.concurrent.duration._
import com.github.mumoshu.play2.memcached.MemcachedPlugin
import models.CurrencyRate

object Application extends Controller {

  def index = Action.async {
    rates().map(currencies => Ok(views.html.index(currencies)))
  }

  def index2 = Action.async {
    ratesXml.map(response => Ok(response.body).as("application/xml"))
  }

  def fetchRates() = ratesXml.map(response => currencies(response.body))

  def ratesXml() = WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").get()

  def rates(): Future[Seq[CurrencyRate]] = {
    val p = Promise[Seq[CurrencyRate]]

    val currenciesFromCache = play.api.Play.current.plugin[EhCachePlugin].get.api.get("rates")
    currenciesFromCache match {
      case Some(currencies) => {
        //println("using cache")
        p.success(currencies.asInstanceOf[Seq[CurrencyRate]])
      }
      case None => {
        println("fetching")
        for {
          currencies <- fetchRates()
        } yield {
          play.api.Play.current.plugin[EhCachePlugin].get.api.set("rates", currencies, 3600)
          p.success(currencies)
        }
      }
    }

    p.future
  }

  def currencies(xml: String) = (loadString(xml) \ "Cube" \ "Cube" \ "Cube")
    .map(n => CurrencyRate((n \ "@currency").text, (n \ "@rate").text.toFloat))

}
