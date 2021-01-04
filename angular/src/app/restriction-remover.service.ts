import { TransferFile } from './transfer-file';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { SettingsService } from './settings.service';

@Injectable({
  providedIn: 'root',
})
export class RestrictionRemoverService {
  constructor(
    private http: HttpClient,
    private settingsService: SettingsService
  ) {}

  getMaximumFileSize(): Promise<number> {
    console.debug('Querying maximum file size...');
    return this.http
      .get(
        this.settingsService.settings.apiUrl +
          '/api/v1/status/maximum-upload-size'
      )
      .toPromise()
      .then((response) => {
        console.debug(
          `Queried maximum upload size: ${response} (${Number(response)})`
        );
        return Number(response);
      })
      .catch(this.handleError);
  }

  getStatistics(): Promise<any> {
    return this.http
      .get(this.settingsService.settings.apiUrl + '/api/v1/status/statistics')
      .toPromise()
      .then((response: any) => response.json())
      .catch(this.handleError);
  }

  uploadDocument(transferFile: TransferFile) {
    let formData: FormData = new FormData();
    formData.append('file', transferFile.file, transferFile.file.name);
    formData.append('password', transferFile.password);

    const options = {
      headers: new HttpHeaders().append('Accept', 'application/json'),
    };

    this.http
      .post(
        this.settingsService.settings.apiUrl + '/api/v1/documents/',
        formData,
        options
      )
      .pipe(
        map((res: any) => res.text())
        //.catch(this.handleError)
      )
      .subscribe(
        (data: any) => {
          console.log('success');
          transferFile.id = data;
          transferFile.status = 'uploaded';
          transferFile.statusText = 'uploaded';
        },
        (error: any) => {
          console.log(error);
          transferFile.status = 'upload-failed';
          transferFile.statusText = 'upload failed';
        }
      );
  }

  checkFile(transferFile: TransferFile): Promise<void> {
    return this.http
      .head(
        this.settingsService.settings.apiUrl +
          '/api/v1/documents/' +
          transferFile.id
      )
      .toPromise()
      .then(
        (successResponse: any) => {
          if (successResponse.status === 200) {
            transferFile.status = 'done';
            transferFile.statusText = 'ready for download';
            transferFile.done = true;
          } else if (successResponse.status === 260) {
            transferFile.status = 'in-progress';
            transferFile.statusText = 'in progress';
          } else {
            transferFile.status = 'unknown';
            transferFile.statusText = 'unknown';
          }
        },
        (errorResponse: any) => {
          if (errorResponse.status === 560) {
            transferFile.status = 'failed';
            transferFile.statusText = 'failed (maybe wrong password?)';
            transferFile.done = true;
          } else {
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
