package me.btieger.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@DslMarker
annotation class DslMarkerBlock
@DslMarker
annotation class DslMarkerConstant
@DslMarker
annotation class DslMarkerVerb5


abstract class DslException(message: String) : Exception(message)

class MultipleSetException(message: String) : DslException(message)
class FieldNotSetException(message: String) : DslException(message)
class InvalidUnitException(message: String) : DslException(message)

class setExactlyOnce<T : Any>() : ReadWriteProperty<Any, T> {
    constructor(default: T?) : this() {
        _value = default
    }

    private var _alreadySet = false
    private var _value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return _value ?: throw FieldNotSetException("Property ${property.name} not set.")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (_alreadySet)
            throw MultipleSetException("Property ${property.name} can only be set once.")
        _alreadySet = true
        _value = value;
    }
}

class setMaxOnce<T>() : ReadWriteProperty<Any, T?> {
    constructor(default: T?) : this() {
        _value = default
    }

    private var _alreadySet = false
    private var _value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return _value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        if (_alreadySet)
            throw MultipleSetException("Property ${property.name} can only be set once.")
        _alreadySet = true
        _value = value;
    }
}
