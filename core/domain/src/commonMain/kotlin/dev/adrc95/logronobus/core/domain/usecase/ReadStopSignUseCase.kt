/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.adrc95.logronobus.core.domain.usecase

import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.scan.findMarkedStop
import dev.adrc95.logronobus.core.domain.scan.howManyAreRead
import dev.adrc95.logronobus.core.domain.scan.keepingTheGoodReading
import dev.adrc95.logronobus.core.domain.scan.resolveDirection
import dev.adrc95.logronobus.core.domain.scan.toScannedSign
import dev.adrc95.logronobus.core.model.ScannedSign
import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.core.model.StopSignReading
import org.koin.core.annotation.Factory

@Factory
class ReadStopSignUseCase(
    private val catalogue: CatalogueRepository,
) {
    suspend operator fun invoke(
        pieces: List<ScannedText>,
        previous: StopSignReading?,
    ): StopSignReading? {
        val sign = pieces.toScannedSign()
        val line = sign.line ?: return null
        val marked = sign.stops.maxByOrNull { it.mark }?.takeIf { it.isMarked }
        logDebug(
            "scan cartel linea=$line sentidos=${sign.directions} paradas=${sign.stops.size} " +
                "marcada=${marked?.let { "${it.name}(${it.mark})" }}",
        )
        val reading = sign.reading() ?: return null
        logDebug(
            "scan encontrado linea=${reading.line} sentido=${reading.direction} " +
                "parada=${reading.stop?.longName} candidatas=${reading.candidates.size}",
        )
        return reading.keepingTheGoodReading(previous)
    }

    private suspend fun ScannedSign.reading(): StopSignReading? {
        val codes = lines.ifEmpty { listOfNotNull(line?.trim()) }
        val routes = catalogue.getRoutes()
        var best: StopSignReading? = null
        var bestMatches = 0
        codes.forEach { code ->
            val route = routes[code] ?: return@forEach
            val stopsByDirection = catalogue.getStopsForRoute(route.shortName)
            val everyStop = stopsByDirection.flatten()
            val read = howManyAreRead(everyStop)
            if (read <= bestMatches) return@forEach
            val ends =
                catalogue
                    .getDirectionOrigins(route.shortName)
                    .takeIf { names -> names.size == 2 }
                    .orEmpty()
            val direction = resolveDirection(this, stopsByDirection, ends)
            best =
                StopSignReading(
                    line = route.shortName,
                    longName = route.longName,
                    origin = direction?.let { ends.getOrNull(1 - it) },
                    destination = direction?.let { ends.getOrNull(it) },
                    direction = direction,
                    stop = findMarkedStop(this, everyStop),
                    candidates =
                        when (direction) {
                            null -> everyStop
                            else -> stopsByDirection.getOrNull(direction).orEmpty()
                        },
                    readStops = stops.size,
                )
            bestMatches = read
        }
        return best?.takeIf { bestMatches >= DomainConstants.SCAN_MIN_READ_STOPS }
    }
}
