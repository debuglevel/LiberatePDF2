/**
 * LiberatePDF2 Microservice
 * Microservice for LiberatePDF2
 *
 * The version of the OpenAPI document: 0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface PostTransformationResponse { 
    id?: string;
    originalFilename?: string;
    finished?: boolean;
    failed?: boolean | null;
    errorMessage?: string | null;
}

