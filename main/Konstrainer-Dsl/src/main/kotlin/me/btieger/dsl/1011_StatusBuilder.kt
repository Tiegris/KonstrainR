package me.btieger.dsl

class Status(val code: Int, val message: String)
class StatusBuilder {

    @DslMarkerVerb5
    var code: Int by setExactlyOnce(403)
    @DslMarkerVerb5
    var message: String by setExactlyOnce("Denied by Konstrainer webhook")

    internal fun build() = Status(code, message)
}