import { TransferFile } from './transfer-file';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { SettingsService } from './settings.service';
import { StatusService } from './restclient/api/api';

@Injectable({
  providedIn: 'root',
})
export class RestrictionRemoverService {
  constructor(
    private statusService: StatusService,
    private http: HttpClient,
    private settingsService: SettingsService
  ) {}

  getMaximumFileSize(): Promise<number> {
    console.debug('Querying maximum upload size...');

    return this.statusService
      .maximumUploadSize()
      .toPromise()
      .then((maximumUploadSize) => {
        console.debug(`Queried maximum upload size: ${maximumUploadSize}`);
        return maximumUploadSize;
      })
      .catch(this.handleError);
  }

  getStatistics(): Promise<any> {
    console.debug('Querying statistics...');
    return this.http
      .get(this.settingsService.settings.apiUrl + '/v1/status/statistics')
      .toPromise()
      .then((response: any) => {
        return response;
      })
      .catch(this.handleError);
  }

  uploadDocument(transferFile: TransferFile) {
    console.debug('Uploading document...');

    let formData: FormData = new FormData();
    formData.append('file', transferFile.file, transferFile.file.name);
    formData.append('password', transferFile.password);

    const options = {
      headers: new HttpHeaders().append('Accept', 'text/plain'),
      responseType: 'text' as 'text',
    };

    console.debug('Sending POST request...');
    this.http
      .post(
        this.settingsService.settings.apiUrl + '/v1/documents/',
        formData,
        options
      )
      .pipe(
        map((res: any) => {
          console.debug('Got response on POST request:');
          console.debug(res);
          return res;
        })
        //.catch(this.handleError)
      )
      .subscribe(
        (data: any) => {
          console.log('Success on uploading file');
          transferFile.id = data;
          transferFile.status = 'uploaded';
          transferFile.statusText = 'uploaded';
        },
        (error: any) => {
          console.error('An error occurred:');
          console.error(error);
          transferFile.status = 'upload-failed';
          transferFile.statusText = 'upload failed';
        }
      );
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
