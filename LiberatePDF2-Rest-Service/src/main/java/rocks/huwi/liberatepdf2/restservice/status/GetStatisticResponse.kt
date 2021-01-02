package rocks.huwi.liberatepdf2.restservice.status

class GetStatisticResponse(
    val storedItems: Long,
    val processedItems: Long,
    val failedItems: Long
)
