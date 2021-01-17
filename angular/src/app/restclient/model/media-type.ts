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
import { Charset } from './charset';


export interface MediaType { 
    name?: string;
    type?: string;
    subtype?: string;
    extension?: string;
    parameters?: Array<string>;
    quality?: string;
    qualityAsNumber?: number;
    version?: string;
    charset?: Charset;
    textBased?: boolean;
}

