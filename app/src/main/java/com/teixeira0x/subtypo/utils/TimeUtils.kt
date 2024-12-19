/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.utils

/**
 * Functions for dealing with times.
 *
 * @author Felipe Teixeira
 */
object TimeUtils {
  const val TIME_UNIT_HOUR = 3600000
  const val TIME_UNIT_MINUTE = 60000
  const val TIME_UNIT_SECOND = 1000

  /**
   * Converts the given milliseconds to a String in the format hh:mm:ss,SSS.
   *
   * @return The time String in the format hh:mm:ss,SSS.
   */
  fun Long.getFormattedTime(): String {
    val hours = this / TIME_UNIT_HOUR
    val minutes = (this % TIME_UNIT_HOUR) / TIME_UNIT_MINUTE
    val seconds = (this % TIME_UNIT_MINUTE) / TIME_UNIT_SECOND
    val millis = this % TIME_UNIT_SECOND

    return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
  }

  /**
   * Converts the given hh:mm:ss,SSS format time string to milliseconds.
   *
   * @return The milliseconds obtained from the time String.
   */
  fun String.getMilliseconds(): Long {
    require(isValidTime(this)) { "Time format must be `hh:mm:ss,SSS`" }

    val timeParts = this.split(":")
    val hours = timeParts[0].toLong()
    val minutes = timeParts[1].toLong()

    val secondsAndMillis = timeParts[2]
    val secParts = secondsAndMillis.split(",")

    val seconds = secParts[0].toLong()
    val millis = secParts[1].toLong()

    val totalMillis =
      (hours * TIME_UNIT_HOUR) +
        (minutes * TIME_UNIT_MINUTE) +
        (seconds * TIME_UNIT_SECOND) +
        millis

    return totalMillis
  }

  /**
   * Checks if the time given in the format hh:mm:ss,SSS is valid.
   *
   * @param time The time to check.
   * @return If the time provided is valid.
   */
  fun isValidTime(time: String): Boolean {
    val timeParts = time.split(":")
    return runCatching {
        require(timeParts.size == 3) { "Time format must be `hh:mm:ss,SSS`" }

        val hours = timeParts[0]
        val minutes = timeParts[1]
        val secondsAndMillis = timeParts[2]
        val secParts = secondsAndMillis.split(",")
        require(secParts.size == 2) { "Seconds time format must be `ss,SSS`" }
        val seconds = secParts[0]
        val millis = secParts[1]

        val isValid =
          isInRange(hours, 0, 99, 2) &&
            isInRange(minutes, 0, 59, 2) &&
            isInRange(seconds, 0, 59, 2) &&
            isInRange(millis, 0, 999, 3)

        isValid
      }
      .getOrDefault(false)
  }

  /**
   * Checks if the given String has the expected length and if its numeric value
   * is within the specified bounds.
   *
   * @param value The String to be checked.
   * @param min The minimum numeric value (inclusive).
   * @param max The maximum numeric value (inclusive).
   * @param expectedLength The expected length of the String.
   * @return `true` if the numeric value of the String is within the bounds and
   *   its length matches the expected length, `false` otherwise.
   */
  fun isInRange(
    value: String,
    min: Int,
    max: Int,
    expectedLength: Int,
  ): Boolean {
    return value.length == expectedLength &&
      value.toIntOrNull()?.let { it in min..max } ?: false
  }
}
