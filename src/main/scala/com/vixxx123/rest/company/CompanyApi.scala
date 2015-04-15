package com.vixxx123.rest.company

import akka.actor.Props
import akka.routing.RoundRobinPool
import com.vixxx123.database.DatabaseAccess
import com.vixxx123.logger.Logging
import com.vixxx123.rest.BaseResourceApi
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

/**
 * Person API main class
 *
 * trait HttpService - for spray routing
 * trait BaseResourceApi - for initialization
 * trait DatabaseAccess - for db access
 *
 */
trait CompanyApi extends HttpService with BaseResourceApi with DatabaseAccess with Logging {

  val companyCreateHandler = actorRefFactory.actorOf(RoundRobinPool(2).props(Props[CreateActor]), s"${TableName}CreateRouter")
  val companyPutHandler = actorRefFactory.actorOf(RoundRobinPool(5).props(Props[UpdateActor]), s"${TableName}PutRouter")
  val companyGetHandler = actorRefFactory.actorOf(RoundRobinPool(20).props(Props[GetActor]), s"${TableName}GetRouter")
  val companyDeleteHandler = actorRefFactory.actorOf(RoundRobinPool(20).props(Props[DeleteActor]), s"${TableName}DeleteRouter")

  override val logTag: String = getClass.getName

  override def init() = {
    connectionPool withSession {
      implicit session =>
        L.debug("initializing persons")
        if (MTable.getTables(TableName).list.isEmpty) {
          Companies.ddl.create
        }
    }
  }

  val companyRoute =
    pathPrefix(TableName) {
      pathEnd {
        get {
          ctx => companyGetHandler ! GetMessage(ctx, None)
        } ~
        post {
          entity(as[Company]) {
            user =>
              ctx => companyCreateHandler ! CreateMessage(ctx, user)
          }
        }
      } ~
      pathPrefix (IntNumber){
        entityId => {
          pathEnd {
            get {
              ctx => companyGetHandler ! GetMessage(ctx, Some(entityId))
            } ~ put {
              entity(as[Company]) { entity =>
                ctx => companyPutHandler ! PutMessage(ctx, entity.copy(id = Some(entityId)))
              }
            } ~ delete {
              ctx => companyDeleteHandler ! DeleteMessage(ctx, entityId)
            } ~ patch {
              ctx => companyPutHandler ! PatchMessage(ctx, entityId)
            }
          }
        }
      }
    }
}
