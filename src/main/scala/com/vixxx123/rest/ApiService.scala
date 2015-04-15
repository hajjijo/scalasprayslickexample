package com.vixxx123.rest

import akka.actor._
import com.vixxx123.logger.{Logging, ConsoleLogger, Logger}
import com.vixxx123.rest.company.CompanyApi
import com.vixxx123.rest.person.PersonApi

import spray.routing._
import spray.util.LoggingContext

/**
 * Main Api service class
 */
class ApiService extends Actor with Api with Logging {

  override val logTag: String = getClass.getName

  override implicit def actorRefFactory: ActorContext = context

  override def receive = runRoute(handleExceptions(new RestExceptionHandler().exceptionHandler){routing})

}

object ApiService {
  val ActorName = "api-root"

  def props() = Props(classOf[ApiService])
}


trait Api extends HttpService with PersonApi with CompanyApi {
  init()
  val routing = userRoute ~ companyRoute
}

trait BaseResourceApi {
  def init(): Unit = {}
}