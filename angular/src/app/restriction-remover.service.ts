import { TransferFile } from './transfer-file';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { SettingsService } from './settings.service';
import {
  StatusService,
  ConfigurationService,
  DocumentsService,
  TransformationsService,
} from './restclient/api/api';

@Injectable({
  providedIn: 'root',
})
export class RestrictionRemoverService {
  constructor(
    private transformationsService: TransformationsService,
    private configurationService: ConfigurationService,
    private statusService: StatusService,
    private http: HttpClient,
    private settingsService: SettingsService
  ) {}

  getMaximumFileSize(): Promise<number> {
    console.debug('Querying maximum upload size...');

    return this.configurationService
      .getConfiguration()
      .toPromise()
      .then((configuration) => {
        console.debug(
          `Queried maximum upload size: ${configuration.maximumMultipartUploadSize}`
        );
        return configuration.maximumMultipartUploadSize;
      })
      .catch(this.handleError);
  }

  getStatistics(): Promise<any> {
    console.debug('Querying statistics...');

    return this.statusService
      .statistics()
      .toPromise()
      .then((statistics) => {
        console.debug(`Queried statistics:`);
        console.debug(statistics);
        return statistics;
      })
      .catch(this.handleError);
  }

  uploadDocument(transferFile: TransferFile) {
    console.debug('Uploading document...');

    this.transformationsService
      .postOneTransformation(transferFile.file, transferFile.password)
      .toPromise()
      .then((postTransformationResponse: any) => {
        console.debug('Got response on POST request:');
        console.debug(postTransformationResponse);

        console.log('Success on uploading file');
        transferFile.id = postTransformationResponse.id;
        transferFile.status = 'uploaded';
        transferFile.statusText = 'uploaded';
      })
      .catch((error: any) => {
        console.error('Error occurred on uploading file');
        console.error(error);
        transferFile.status = 'upload-failed';
        transferFile.statusText = 'upload failed';
      });
  }

  checkFile(transferFile: TransferFile): Promise<void> {
    console.debug(`Checking file ${transferFile.id}...`);

    console.debug(`Sending HEAD request...`);
    return this.http
      .head(
        this.settingsService.settings.apiUrl +
          '/v1/documents/' +
          transferFile.id,
        { observe: 'response' }
      )
      .toPromise()
      .then(
        (successResponse: any) => {
          console.debug(`Received successful response:`);
          console.debug(successResponse);
          if (successResponse.status === 200) {
            console.debug(`File is done; ready for download`);
            transferFile.status = 'done';
            transferFile.statusText = 'ready for download';
            transferFile.done = true;
          } else if (successResponse.status === 102) {
            console.debug(`File is in progress`);
            transferFile.status = 'in-progress';
            transferFile.statusText = 'in progress';
          } else {
            console.debug(`File is in unknown state`);
            transferFile.status = 'unknown';
            transferFile.statusText = 'unknown';
          }
        },
        (errorResponse: any) => {
          console.debug(`Received failure response:`);
          if (errorResponse.status === 500) {
            console.debug(`File has failed`);
            transferFile.status = 'failed';
            transferFile.statusText = 'failed (maybe wrong password?)';
            transferFile.done = true;
          } else {
            console.debug(`File is in unknown state`);
            transferFile.status = 'unknown';
            transferFile.statusText = 'unknown';
          }
        }
      )
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }
}
