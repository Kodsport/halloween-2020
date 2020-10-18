package halloween2020.global

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction

object Firmwares: IntIdTable() {
    val team = reference("team", Teams, onDelete = ReferenceOption.RESTRICT)
    val exec = blob("executable")
    val elo = double("elo")

    init {
        index(isUnique = true, team, id)
    }
}

class Firmware(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Firmware>(Firmwares)

    var team by Team referencedOn Firmwares.team
    var exec by Firmwares.exec
    var elo by Firmwares.elo

    val matches
        get() = Match.find {
            (Matches.p1 eq id) or  (Matches.p2 eq id)
        }
}

object Matches: IntIdTable() {
    val p1 = reference("firmware1", Firmwares, onDelete = ReferenceOption.RESTRICT).index()
    val p2 = reference("firmware2", Firmwares, onDelete = ReferenceOption.RESTRICT).index()
    val s1 = integer("score1")
    val s2 = integer("score2")
    val played = datetime("played").defaultExpression(CurrentDateTime())
    val data = text("data")
    val map = varchar("map", 100)
}

class Match(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Match>(Matches)

    var p1 by Firmware referencedOn Matches.p1
    var p2 by Firmware referencedOn Matches.p2
    var s1 by Matches.s1
    var s2 by Matches.s2
    var played by Matches.played
    var data by Matches.data
    var map by Matches.map
}

object Teams: IntIdTable() {
    val name = varchar("name", 100).index(isUnique = true)
    val key = varchar("key", 100).index()
}

class Team(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Team>(Teams)

    var name by Teams.name
    var key by Teams.key
    val members by Member optionalReferrersOn Members.team
    val firmwares by Firmware referrersOn Firmwares.team
}

object Members: IntIdTable() {
    val user = varchar("user", 100).index(isUnique = true)
    val team = reference("team", Teams, onDelete = ReferenceOption.SET_NULL).index().nullable()
}

class Member(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Member>(Members)

    var user by Members.user
    var team by Team optionalReferencedOn Members.team
}

object Db {
    val db: Database = Database.connect(dbUrl(), driver = "org.h2.Driver")

    fun dbUrl(): String {
        Class.forName("org.postgresql.Driver")
        val env = System.getenv("DATABASE_URL")
        return if (env.startsWith("postgres:")) {
            val user = env.substringAfter("postgres://").substringBefore("@")
            val host = env.substringAfter("@").substringBefore("/")
            val db = env.substringAfterLast("/")
            "jdbc:postgresql://${host}/${db}?user=${user.substringBefore(":")}&password=${user.substringAfter(":")}"
        } else {
            "jdbc:$env"
        }
    }

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Teams, Members, Firmwares, Matches)
        }
    }
}

