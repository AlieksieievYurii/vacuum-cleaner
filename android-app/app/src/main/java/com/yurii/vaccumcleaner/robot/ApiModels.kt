package com.yurii.vaccumcleaner.robot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.lang.IllegalStateException

@JsonClass(generateAdapter = true)
data class GeneralSystemInfo(
    val platform: String,
    @Json(name = "platform_release") val platformRelease: String,
    @Json(name = "platform_version") val platformVersion: String,
    val architecture: String,
    val hostname: String,
    @Json(name = "ip_address") val ipAddress: String,
    @Json(name = "mac_address") val macAddress: String,
    val processor: String
)

@JsonClass(generateAdapter = true)
data class RobotInputData(
    @Json(name = "el") val leftBumperHit: Boolean,
    @Json(name = "er") val rightBumperHit: Boolean,
    @Json(name = "eb") val isDustBoxInserted: Boolean,
    @Json(name = "ec") val isLidClosed: Boolean,
    @Json(name = "lrs") val leftDistanceRange: Int,
    @Json(name = "crs") val centerDistanceRange: Int,
    @Json(name = "rrs") val rightDistanceRange: Int,
    @Json(name = "bv") val batteryVoltage: Float,
    @Json(name = "bc") val batteryCapacity: Int,
    @Json(name = "cs") val chargingState: Int, // NO CHARGING = 0, CHARGING = 1, CHARGED = 2
    @Json(name = "rws") val rightWheelSpeed: Int, // Sm per minute
    @Json(name = "lws") val leftWheelSpeed: Int, // Sm per minute
)

@JsonClass(generateAdapter = true)
data class PidSettings(
    val proportional: Float,
    val integral: Float,
    val derivative: Float
)

@JsonClass(generateAdapter = true)
data class AlgorithmParameter(
    val name: String,
    @Json(name = "value_type") val valueType: String,
    @Json(name = "current_value") val currentValue: Any,
    @Json(name = "default_value") val defaultValue: Any
) {
    companion object {
        private val rangeTypeRegex = Regex("(\\d+)..(\\d+)")
        private val choiceTypeRegex = Regex("([^,]+)")
    }

    sealed class Type {
        data class Text(val value: String, val default: String) : Type()
        data class Integer(val value: Int, val default: Int) : Type()
        data class Floating(val value: Float, val default: Float) : Type()
        data class IntRange(val from: Int, val to: Int, val default: Int) : Type()
        data class TextChoice(val values: List<String>, val default: String) : Type()
        data class Bool(val value: Boolean, val default: Boolean) : Type()
    }

    fun getType(): Type = when {
        valueType == "boolean" -> Type.Bool(currentValue as Boolean, defaultValue as Boolean)
        valueType == "string" -> Type.Text(currentValue as String, defaultValue as String)
        valueType == "integer" -> Type.Integer(currentValue.toString().toFloat().toInt(), defaultValue.toString().toFloat().toInt())
        valueType == "floating" -> Type.Floating(currentValue.toString().toFloat(), defaultValue.toString().toFloat())
        rangeTypeRegex.containsMatchIn(valueType) -> {
            val (from, to) = rangeTypeRegex.find(valueType)!!.destructured
            Type.IntRange(from.toInt(), to.toInt(), defaultValue.toString().toFloat().toInt())
        }
        choiceTypeRegex.containsMatchIn(valueType) -> {
            val choices = choiceTypeRegex.findAll(valueType).toList().map { it.value }
            Type.TextChoice(choices, defaultValue.toString())
        }

        else -> throw IllegalStateException("Unhandled argument $this")
    }
}

@JsonClass(generateAdapter = true)
data class ArgumentValue(
    val name: String,
    val value: Any
)

@JsonClass(generateAdapter = true)
data class AlgorithmScript(
    val name: String,
    val description: String,
    val parameters: List<AlgorithmParameter>
)

@JsonClass(generateAdapter = true)
data class AlgorithmList(
    @Json(name = "current_algorithm") val currentAlgorithmName: String,
    val algorithms: List<AlgorithmScript>
)

@JsonClass(generateAdapter = true)
data class Algorithm(
    @Json(name = "algorithm_name") val name: String,
    val arguments: List<ArgumentValue>
)

enum class CleaningStatusEnum(val value: String) {
    @Json(name = "none")
    NONE("none"),

    @Json(name = "running")
    RUNNING("running"),

    @Json(name = "paused")
    PAUSED("paused")
}

@JsonClass(generateAdapter = true)
data class CleaningExecutionInfo(
    @Json(name = "algorithm_name") val algorithmName: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class CleaningStatus(
    val status: CleaningStatusEnum,
    @Json(name = "cleaning_info") val cleaningInfo: CleaningExecutionInfo?
)