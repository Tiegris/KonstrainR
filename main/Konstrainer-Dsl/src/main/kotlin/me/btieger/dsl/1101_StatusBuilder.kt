package me.btieger.dsl

class Status(val code: Int, val message: String)
class StatusBuilder {

    @DslMarkerVerb5
    var code: Int by setOnce(403)
    @DslMarkerVerb5
    var message: String by setOnce("Denied by Konstrainer webhook")

    internal fun build() = Status(code, message)
}