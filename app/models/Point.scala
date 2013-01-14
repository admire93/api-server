package models

import anorm._ 
import play.api.Play.current
import play.api.db.DB
import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json._

case class Point(var id: Pk[Long], accountId: Long, typeId: Long, identifier: String, createdAt: Date, updatedAt: Date, referencedAt: Date, data: JsObject)

object Point {
  val Type: Map[String, Long] = Map(
      "user" -> 10,
      "page" -> 20,
      "post" -> 30
    )
  val TypeString: Map[Long, String] = Map(
      10L -> "user",
      20L -> "page",
      30L -> "post"
    )
  def apply(accId: Long, typeString: String, identifier: String, data: JsObject): Point = {
    var typeId: Long = -1
    Point.Type.get(typeString).get match {
      case id: Long => typeId = id
      case _ => throw new Exception("point(identifier=" + identifier + ") type: '" + typeString + "' isn't supported.")
    }

    if(identifier.length > 0){
      this.findOneByTypeIdAndIdentifier(accId, typeId, identifier) match {
        case row: Some[Point] => {
          row.get
        }
        case _ => {
          new Point(anorm.NotAssigned, accId, typeId, identifier, new Date, new Date, new Date, data )
        }
      }
    }else{
      new Point(anorm.NotAssigned, accId, typeId, identifier, new Date, new Date, new Date, data )
    }
  }

  def findOneById(accId: Long, id: Long): Option[Point] = {
    DB.withConnection { implicit conn =>
      val rowStream = SQL("select * from points where id = {id} and accountId = {accId} ").on('id -> id, 'accId -> accId).apply()
      if(rowStream.isEmpty){
        None
      }else{
        val row = rowStream.head
        Some( new Point(row[Pk[Long]]("id"), row[Long]("accountId"), row[Long]("typeId"), row[String]("identifier"), row[Date]("createdAt"), row[Date]("updatedAt"), row[Date]("referencedAt"), Json.obj( "data" -> Json.parse(row[String]("data")) ) ))
      }
    }
  }
  
  def findOneByTypeIdAndIdentifier(accId: Long, typeId: Long, identifier: String): Option[Point] = {
    DB.withConnection { implicit conn =>
      val rowStream = SQL("select * from points where accountId = {accId} and typeId = {typeId} and identifier = {identifier}").on('accId -> accId, 'typeId -> typeId, 'identifier -> identifier).apply()
      if(rowStream.isEmpty){
        None
      }else{
        val row = rowStream.head
        Some( new Point(row[Pk[Long]]("id"), row[Long]("accountId"), row[Long]("typeId"), row[String]("identifier"), row[Date]("createdAt"), row[Date]("updatedAt"), row[Date]("referencedAt"), Json.obj( "data" -> Json.parse(row[String]("data")) ) ))
      }
    }
  }

  def add(point: Point):Option[Long] = {
    DB.withConnection { implicit conn =>
      val id: Option[Long] = SQL(
        """
        insert into points (typeId, accountId, identifier, createdAt, updatedAt, referencedAt, data)
        values ({typeId}, {accountId}, {identifier}, {createdAt}, {updatedAt}, {referencedAt}, {data})
        """
      ).on(
        'typeId         -> point.typeId, 
        'accountId      -> point.accountId, 
        'identifier     -> point.identifier, 
        'createdAt      -> point.createdAt, 
        'updatedAt      -> point.updatedAt, 
        'referencedAt   -> point.referencedAt, 
        'data           -> Json.stringify(point.data)
      ).executeInsert()
      id
    }
  }
}
