# de.debuglevel.liberatepdf2.client - Kotlin client library for LiberatePDF2 Microservice

## Requires

* Kotlin 1.3.61
* Gradle 4.9

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs.
* Supports collection formats for query parameters: csv, tsv, ssv, pipes.
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in OpenAPI definitions.
* Implementation of ApiClient is intended to reduce method counts, specifically to benefit Android targets.

<a name="documentation-for-api-endpoints"></a>

## Documentation for API Endpoints

All URIs are relative to *http://localhost*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*ConfigurationApi* | [**getConfiguration**](docs/ConfigurationApi.md#getconfiguration) | **GET** /api/v1/configuration |
*DocumentsApi* | [**downloadUnrestricted**](docs/DocumentsApi.md#downloadunrestricted) | **
GET** /api/v1/documents/{documentId} |
*DocumentsApi* | [**downloadZip**](docs/DocumentsApi.md#downloadzip) | **GET** /api/v1/documents/zip |
*DocumentsApi* | [**uploadAndRemoveRestrictions**](docs/DocumentsApi.md#uploadandremoverestrictions) | **
POST** /api/v1/documents |
*StatusApi* | [**maximumUploadSize**](docs/StatusApi.md#maximumuploadsize) | **
GET** /api/v1/status/maximum-upload-size |
*StatusApi* | [**ping**](docs/StatusApi.md#ping) | **GET** /api/v1/status/ping |
*StatusApi* | [**statistics**](docs/StatusApi.md#statistics) | **GET** /api/v1/status/statistics |

<a name="documentation-for-models"></a>

## Documentation for Models

- [de.debuglevel.liberatepdf2.client.models.GetConfigurationResponse](docs/GetConfigurationResponse.md)
- [de.debuglevel.liberatepdf2.client.models.GetStatisticResponse](docs/GetStatisticResponse.md)

<a name="documentation-for-authorization"></a>

## Documentation for Authorization

All endpoints do not require authorization.
