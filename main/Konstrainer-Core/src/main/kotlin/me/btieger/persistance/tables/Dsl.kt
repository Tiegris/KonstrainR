package me.btieger.persistance.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

class Dsl(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, Dsl>(Dsls)

    var name by Dsls.name
    var buildStatus by Dsls.buildStatus
    var jobSecret by Dsls.jobSecret
    var buildSubmissionTime by Dsls.buildSubmissionTime
    var errorMessage by Dsls.errorMessage
    var file by Dsls.file
    var jar by Dsls.jar
    var serverStatus by Dsls.serverStatus

    var hasMonitors by Dsls.hasMonitors
    var hasWebhooks by Dsls.hasWebhooks
}

object Dsls : IntIdTable() {
    val name = varchar("name", 128)
    val file = blob("file")
    val buildStatus = enumeration("buildStatus", BuildStatus::class)
    val jobSecret = binary("job_secret", 64).nullable()
    val buildSubmissionTime = datetime("build_submission").nullable()
    val errorMessage = text("error").nullable()
    val jar = blob("jar").nullable()
    val serverStatus = enumeration("serverStatus", ServerStatus::class).default(ServerStatus.Down)

    val hasMonitors = bool("hasAggregators").nullable()
    val hasWebhooks = bool("hasWebhooks").nullable()
}

enum class ServerStatus {
    Spawning,
    Up,
    Down,
    Error
}

enum class BuildStatus {
    Building,
    Ready,
    Failed,
}