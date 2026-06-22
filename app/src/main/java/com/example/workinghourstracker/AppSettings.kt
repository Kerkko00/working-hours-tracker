package com.example.workinghourstracker

private const val defaultWeeklyThreshold = 37.5
private const val defaultPastWeeksToCheck = 4

data class AppSettings(
    val weeklyThreshold: Double = defaultWeeklyThreshold,
    val pastWeeksToCheck: Int = defaultPastWeeksToCheck
)

fun getDefaultWeeklyThreshold(): Double = defaultWeeklyThreshold
fun getDefaultPastWeeksToCheck(): Int = defaultPastWeeksToCheck