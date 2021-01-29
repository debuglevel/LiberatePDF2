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
import { GetStatisticResponse } from './restclient';

@Injectable({
  providedIn: 'root',
})
export class RestrictionRemoverService {
  constructor(
    private transformationsService: TransformationsService,
    private configurationService: ConfigurationService,
    private statusService: StatusService
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

  getStatistics(): Promise<GetStatisticResponse> {
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
      .then((postTransformationResponse) => {
        console.debug('Got response on POST request:');
        console.debug(postTransformationResponse);

        console.log('Success on uploading file');
        transferFile.id = postTransformationResponse.id!;
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
    console.debug(`Checking transformation ${transferFile.id}...`);

    return this.transformationsService
      .getOneTransformation(transferFile.id!)
      .toPromise()
      .then((getTransformationResponse) => {
        console.debug(
          `Got response on GET /transformation/${transferFile.id}:`
        );
        console.debug(getTransformationResponse);

        if (getTransformationResponse.finished == false) {
          transferFile.status = 'in-progress';
          transferFile.statusText = 'in progress';
          transferFile.done = false;
        } else if (
          getTransformationResponse.finished &&
          getTransformationResponse.failed == false
        ) {
          transferFile.status = 'done';
          transferFile.statusText = 'ready for download';
          transferFile.done = true;
        } else if (
          getTransformationResponse.finished &&
          getTransformationResponse.failed == true
        ) {
          transferFile.status = 'failed';
          transferFile.statusText = `failed (${getTransformationResponse.errorMessage})`;
          transferFile.done = true;
        } else {
          console.error('Transformation is in unhandled state');
          console.error(getTransformationResponse);
          transferFile.status = 'unknown';
          transferFile.statusText = 'unknown';
        }
      })
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }
}
