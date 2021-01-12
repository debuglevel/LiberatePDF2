export * from './configuration.service';
import { ConfigurationService } from './configuration.service';
export * from './documents.service';
import { DocumentsService } from './documents.service';
export * from './status.service';
import { StatusService } from './status.service';
export * from './transformations.service';
import { TransformationsService } from './transformations.service';
export const APIS = [ConfigurationService, DocumentsService, StatusService, TransformationsService];
