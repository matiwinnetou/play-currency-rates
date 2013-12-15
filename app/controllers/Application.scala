package controllers

import play.api.mvc._
import play.api.libs.ws.WS
import scala.xml.XML.loadString
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.cache.Cache
import scala.concurrent.{Promise, Future}
import play.api.Play.current
import scala.concurrent.duration._
import com.github.mumoshu.play2.memcached.MemcachedPlugin

object Application extends Controller {

  def index = Action.async {
    rates().map(currencies => Ok(views.html.index(currencies)))
  }

  def index2 = Action.async {
    ratesXml.map(response => Ok(response.body).as("application/xml"))
  }

  def fetchRates() = ratesXml.map(response => currencies(response.body))

  def ratesXml() = WS.url("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").get()

  def rates(): Future[Seq[Currency]] = {
    val p = Promise[Seq[Currency]]

    val currenciesFromCache = play.api.Play.current.plugin[MemcachedPlugin].get.api.get("rates")

    currenciesFromCache match {
      case Some(currs) => {
        println("using cache")
        p.success(currs.asInstanceOf[Seq[Currency]])
      }
      case None => {
        println("fetching")
        fetchAndCacheSet(p)
      }
    }

    p.future
  }

  def fetchAndCacheSet(p: Promise[Seq[Currency]]) = {
    for {
      currencies <- fetchRates()
    } yield {
      play.api.Play.current.plugin[MemcachedPlugin].get.api.set("rates", currencies, 3600)
      //Cache.set("rates", currencies, 1 minutes)
      p.success(currencies)
    }
  }

  case class Currency(symbol: String, rate: Float)

  def currencies(xml: String) = {
    (loadString(xml) \ "Cube" \ "Cube" \ "Cube")
      .map(n => Currency((n \ "@currency").text, (n \ "@rate").text.toFloat))
  }

}
