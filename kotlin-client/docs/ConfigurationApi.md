# ConfigurationApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getConfiguration**](ConfigurationApi.md#getConfiguration) | **GET** /api/v1/configuration |

<a name="getConfiguration"></a>

# **getConfiguration**

> GetConfigurationResponse getConfiguration()

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = ConfigurationApi()
try {
    val result : GetConfigurationResponse = apiInstance.getConfiguration()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ConfigurationApi#getConfiguration")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ConfigurationApi#getConfiguration")
    e.printStackTrace()
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**GetConfigurationResponse**](GetConfigurationResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

