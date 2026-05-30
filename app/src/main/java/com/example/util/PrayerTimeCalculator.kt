package com.example.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object PrayerTimeCalculator {

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
        MWL(18.0, 17.0),               // Muslim World League
        ISNA(15.0, 15.0),              // Islamic Society of North America
        EGYPT(19.5, 17.5),             // Tunisian/Egyptian General Authority of Survey
        MAKKAH(18.5, 90.0),            // Umm al-Qura, Makkah (Isha is usually 90 min after Maghrib)
        KARACHI(18.0, 18.0)            // University of Islamic Sciences, Karachi
    }

    data class PrayerTimes(
        val fajr: String,
        val suhoorEnd: String,         // Usually same as Fajr
        val suhoorStart: String,       // 30 min before Fajr
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val tahajjudStart: String,     // After Isha (beginning of last third of night)
        val tahajjudEnd: String        // Ends before Fajr
    )

    /**
     * Compute prayer times for a given day, location, and method.
     * All inputs are offline-first and calculated using astronomical equations.
     */
    fun calculateTimes(
        year: Int,
        month: Int,  // 1-12
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZoneOffsetHrs: Double,
        method: CalculationMethod = CalculationMethod.MAKKAH
    ): PrayerTimes {

        // Compute Julian Date
        val jd = getJulianDate(year, month, day)

        // Compute solar declaration properties
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g)))

        val obliq = 23.439 - 0.00000036 * d
        val dec = Math.toDegrees(asin(sin(Math.toRadians(obliq)) * sin(Math.toRadians(l))))
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(obliq)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val rightAscension = fixHour(ra)

        val eqt = (q / 15.0) - rightAscension // Equation of time

        // Midday (Transit)
        val dhuhrHrs = fixHour(12.0 - longitude / 15.0 - eqt + timeZoneOffsetHrs)

        // Sunrise & Sunset Hour Angles (using -0.833 degrees for horizon refraction)
        val sunsetHA = Math.toDegrees(acos(
            (sin(Math.toRadians(-0.833)) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
                    (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        ))
        val sunsetHrs = dhuhrHrs + (sunsetHA / 15.0)
        val sunriseHrs = dhuhrHrs - (sunsetHA / 15.0)

        // Fajr Hour Angle (based on angle parameter)
        val fajrAngleRad = Math.toRadians(-method.fajrAngle)
        val fajrHA = Math.toDegrees(acos(
            (sin(fajrAngleRad) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
                    (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        ))
        val fajrHrs = dhuhrHrs - (fajrHA / 15.0)

        // Isha Hour Angle or offset
        val ishaHrs = if (method == CalculationMethod.MAKKAH) {
            // Makkah: Isha is 1.5 hours (90 minutes) after Maghrib
            sunsetHrs + 1.5
        } else {
            val ishaAngleRad = Math.toRadians(-method.ishaAngle)
            val ishaHA = Math.toDegrees(acos(
                (sin(ishaAngleRad) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
                        (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
            ))
            dhuhrHrs + (ishaHA / 15.0)
        }

        // Asr Hour Angle (Standard calculation: shadow ratio math)
        val t = dec - latitude
        val asrAngleRad = atan(1.0 + tan(Math.toRadians(abs(t))))
        val asrHA = Math.toDegrees(acos(
            (sin(asrAngleRad) - sin(Math.toRadians(latitude)) * sin(Math.toRadians(dec))) /
                    (cos(Math.toRadians(latitude)) * cos(Math.toRadians(dec)))
        ))
        val asrHrs = dhuhrHrs + (asrHA / 15.0)

        // Format to readable strings
        val maghribHrs = sunsetHrs // Sunset is Maghrib

        // Calculate Suhoor Start (30 min before Fajr)
        val suhoorStartHrs = fajrHrs - 0.5
        val suhoorEndHrs = fajrHrs

        // Calculate Tahajjud Window (starts roughly at midnight or the beginning of the last 1/3 of the night)
        // From Maghrib (sunset) to Fajr is the total night duration
        // Last third = (Fajr - Maghrib) * 2/3
        val nightDuration = if (fajrHrs < maghribHrs) {
            (fajrHrs + 24.0) - maghribHrs
        } else {
            fajrHrs - maghribHrs
        }
        val lastThirdStartHrs = fixHour(maghribHrs + nightDuration * (2.0 / 3.0))

        return PrayerTimes(
            fajr = formatTime(fajrHrs),
            suhoorEnd = formatTime(suhoorEndHrs),
            suhoorStart = formatTime(suhoorStartHrs),
            dhuhr = formatTime(dhuhrHrs),
            asr = formatTime(asrHrs),
            maghrib = formatTime(maghribHrs),
            isha = formatTime(ishaHrs),
            tahajjudStart = formatTime(lastThirdStartHrs),
            tahajjudEnd = formatTime(fajrHrs)
        )
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun formatTime(hours: Double): String {
        val totalMinutes = round(hours * 60).toInt()
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
        val amPm = if (h < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", displayH, m, amPm)
    }

    private fun fixAngle(deg: Double): Double {
        var a = deg % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hr: Double): Double {
        var h = hr % 24.0
        if (h < 0) h += 24.0
        return h
    }
}
