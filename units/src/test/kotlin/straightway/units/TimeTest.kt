/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import straightway.testing.flow._is
import straightway.testing.flow._to
import straightway.testing.flow.equal
import straightway.testing.flow.expect
import java.time.Duration
import java.time.LocalDateTime

class TimeTest {
    @Test fun second_toMinute() = assertEquals(60[second], 1[minute])

    @Test fun second_toHour() = assertEquals(3600[second], 1[hour])
    @Test fun minute_toHour() = assertEquals(60[minute], 1[hour])

    @Test fun second_toDay() = assertEquals(86400[second], 1[day])
    @Test fun minute_toDay() = assertEquals(1.440[kilo(minute)], 1.0[day])
    @Test fun hour_toDay() = assertEquals(24[hour], 1[day])

    @Test fun second_toWeek() = assertEquals(604800[second], 1[week])
    @Test fun minute_toWeek() = assertEquals(10080[minute], 1[week])
    @Test fun hour_toWeek() = assertEquals(168[hour], 1[week])
    @Test fun day_toWeek() = assertEquals(7[day], 1[week])

    @Test fun second_toYear() = assertEquals(31558432.5504[second], 1[year])

    @Test fun conversion() = assertEquals(1[minute], 60[second])

    @Test fun toString_second() = assertEquals("1 s", 1[second].toString())
    @Test fun toString_minute() = assertEquals("1 min", 1[minute].toString())
    @Test fun toString_hour() = assertEquals("1 h", 1[hour].toString())
    @Test fun toString_day() = assertEquals("1 d", 1[day].toString())
    @Test fun toString_week() = assertEquals("1 wk", 1[week].toString())
    @Test fun toString_year() = assertEquals("1 a", 1[year].toString())
    @Test fun toString_scaled() = assertEquals("1 kwk", 1[kilo(week)].toString())

    @Test fun id_minute() = assertEquals(second.id, minute.id)
    @Test fun id_hour() = assertEquals(second.id, hour.id)
    @Test fun id_day() = assertEquals(second.id, day.id)
    @Test fun id_week() = assertEquals(second.id, week.id)
    @Test fun id_year() = assertEquals(second.id, year.id)
    @Test fun id_scaled() = assertEquals(second.id, kilo(week).id)

    @Test
    fun add_toLocalDateTime() = expect(
        LocalDateTime.of(0, 1, 1, 0, 0) + 1[minute] _is equal _to LocalDateTime.of(0, 1, 1, 0, 1))

    @Test
    fun add_toLocalDateTime_chained() = expect(
        LocalDateTime.of(0, 1, 1, 0, 0) + 1[minute] + 1[minute] _is equal _to LocalDateTime.of(0, 1, 1, 0, 2))

    @Test
    fun sub_fromLocalDateTime() = expect(
        LocalDateTime.of(0, 1, 1, 0, 1) - 1[minute] _is equal _to LocalDateTime.of(0, 1, 1, 0, 0))

    @Test
    fun toDuration_int_seconds() =
        expect(1[second].toDuration() _is equal _to Duration.ofSeconds(1))

    @Test
    fun toDuration_int_nanoSeconds() =
        expect(1[nano(second)].toDuration() _is equal _to Duration.ofNanos(1))

    @Test
    fun toDuration_int_irrgeularUnitScale() =
        expect(1[UnitScale(1e-7)(second)].toDuration() _is equal _to Duration.ofNanos(100))

    @Test
    fun toDuration_float() =
        expect(1.000000001[second].toDuration() _is equal _to Duration.ofSeconds(1, 1))

    @Test
    fun toDuration_long_bigDays() {
        val maxDays = 999999999L * 365L + 999999999L / 4L - 20547L * 365L + 20L
        expect(maxDays[day].toDuration() _is equal _to Duration.ofDays(maxDays))
    }

    @Test
    fun fromDuration_seconds() {
        val duration = Duration.ofMinutes(1)
        val time = duration.toTime()
        expect(time _is equal _to 1[minute])
    }

    @Test
    fun fromDuration_nanos() {
        val duration = Duration.ofNanos(10)
        val time = duration.toTime()
        expect(time _is equal _to 10[nano(second)])
    }

    @Test
    fun minus_LocalDateTime() {
        val time1 = LocalDateTime.of(0, 1, 1, 0, 0)
        val time2 = LocalDateTime.of(0, 1, 1, 0, 1)
        val difference = time2 - time1
        expect(difference _is equal _to 1[minute])
    }

    @Test
    fun absolute() =
        expect(1[minute].absolute _is equal _to LocalDateTime.of(0, 1, 1, 0, 1))

    @Test
    fun unitValue() =
        expect(LocalDateTime.of(0, 1, 1, 0, 1).unitValue _is equal _to 1[minute])
}