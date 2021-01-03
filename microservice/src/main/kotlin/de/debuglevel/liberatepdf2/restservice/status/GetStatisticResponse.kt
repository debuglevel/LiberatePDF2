package de.debuglevel.liberatepdf2.restservice.status

class GetStatisticResponse(
    val storedItems: Long,
    val processedItems: Long,
    val failedItems: Long,
    val successfulItems: Long
)
