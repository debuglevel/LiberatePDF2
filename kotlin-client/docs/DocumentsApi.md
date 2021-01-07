# DocumentsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**downloadUnrestricted**](DocumentsApi.md#downloadUnrestricted) | **GET** /api/v1/documents/{documentId} |
[**downloadZip**](DocumentsApi.md#downloadZip) | **GET** /api/v1/documents/zip |
[**uploadAndRemoveRestrictions**](DocumentsApi.md#uploadAndRemoveRestrictions) | **POST** /api/v1/documents |

<a name="downloadUnrestricted"></a>

# **downloadUnrestricted**

> kotlin.Any downloadUnrestricted(documentId)

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val documentId : java.util.UUID = 38400000-8cf0-11bd-b23e-10b96e4ef00d // java.util.UUID | 
try {
    val result : kotlin.Any = apiInstance.downloadUnrestricted(documentId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DocumentsApi#downloadUnrestricted")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DocumentsApi#downloadUnrestricted")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
**documentId** | [**java.util.UUID**](.md)|  |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="downloadZip"></a>

# **downloadZip**

> kotlin.Any downloadZip(id)

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val id : java.util.UUID = 38400000-8cf0-11bd-b23e-10b96e4ef00d // java.util.UUID | 
try {
    val result : kotlin.Any = apiInstance.downloadZip(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DocumentsApi#downloadZip")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DocumentsApi#downloadZip")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
**id** | [**java.util.UUID**](.md)|  | [optional]

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="uploadAndRemoveRestrictions"></a>

# **uploadAndRemoveRestrictions**

> kotlin.String uploadAndRemoveRestrictions(file, password)

### Example

```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val file : java.io.File = BINARY_DATA_HERE // java.io.File | 
val password : kotlin.String = password_example // kotlin.String | 
try {
    val result : kotlin.String = apiInstance.uploadAndRemoveRestrictions(file, password)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DocumentsApi#uploadAndRemoveRestrictions")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DocumentsApi#uploadAndRemoveRestrictions")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
**file** | **java.io.File**|  | [optional]
**password** | **kotlin.String**|  | [optional]

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: multipart/form-data
- **Accept**: text/plain

