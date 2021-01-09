# DocumentsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**downloadZip**](DocumentsApi.md#downloadZip) | **GET** /api/v1/documents/zip | 
[**getOne**](DocumentsApi.md#getOne) | **GET** /api/v1/documents/{documentId} | 
[**postOne**](DocumentsApi.md#postOne) | **POST** /api/v1/documents | 


<a name="downloadZip"></a>
# **downloadZip**
> kotlin.Any downloadZip(ids)



### Example
```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val ids : java.util.UUID = 38400000-8cf0-11bd-b23e-10b96e4ef00d // java.util.UUID | 
try {
    val result : kotlin.Any = apiInstance.downloadZip(ids)
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
 **ids** | [**java.util.UUID**](.md)|  | [optional]

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/zip

<a name="getOne"></a>
# **getOne**
> kotlin.Any getOne(documentId)



### Example
```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val documentId : java.util.UUID = 38400000-8cf0-11bd-b23e-10b96e4ef00d // java.util.UUID | 
try {
    val result : kotlin.Any = apiInstance.getOne(documentId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DocumentsApi#getOne")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DocumentsApi#getOne")
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

<a name="postOne"></a>
# **postOne**
> kotlin.String postOne(file, password)



### Example
```kotlin
// Import classes:
//import de.debuglevel.liberatepdf2.client.infrastructure.*
//import de.debuglevel.liberatepdf2.client.models.*

val apiInstance = DocumentsApi()
val file : java.io.File = BINARY_DATA_HERE // java.io.File | 
val password : kotlin.String = password_example // kotlin.String | 
try {
    val result : kotlin.String = apiInstance.postOne(file, password)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DocumentsApi#postOne")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DocumentsApi#postOne")
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

