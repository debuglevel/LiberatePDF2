# StatusApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**maximumUploadSize**](StatusApi.md#maximumUploadSize) | **GET** /api/v1/status/maximum-upload-size |
[**ping**](StatusApi.md#ping) | **GET** /api/v1/status/ping |
[**statistics**](StatusApi.md#statistics) | **GET** /api/v1/status/statistics |

<a name="maximumUploadSize"></a>

# **maximumUploadSize**

> kotlin.Long maximumUploadSize()

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = StatusApi()
try {
    val result: kotlin.Long = apiInstance.maximumUploadSize()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling StatusApi#maximumUploadSize")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling StatusApi#maximumUploadSize")
    e.printStackTrace()
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**kotlin.Long**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="ping"></a>

# **ping**

> kotlin.String ping()

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = StatusApi()
try {
    val result: kotlin.String = apiInstance.ping()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling StatusApi#ping")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling StatusApi#ping")
    e.printStackTrace()
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="statistics"></a>

# **statistics**

> GetStatisticResponse statistics()

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = StatusApi()
try {
    val result: GetStatisticResponse = apiInstance.statistics()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling StatusApi#statistics")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling StatusApi#statistics")
    e.printStackTrace()
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**GetStatisticResponse**](GetStatisticResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

