import { TransferFile } from './transfer-file';
import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class RestrictionRemoverService {

  url = `http://localhost:8080`;

  constructor(private http: Http) { }

  getMaximumFileSize(): Promise<number> {
    return this.http.get(this.url + '/api/v1/status/maximum-upload-size')
      .toPromise()
      .then(response => Number(response.text()))
      .catch(this.handleError);
  }
  
  getStatistics(): Promise<any> {
    return this.http.get(this.url + '/api/v1/status/statistics')
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  uploadDocument(transferFile: TransferFile) {
    let formData: FormData = new FormData();
    formData.append('file', transferFile.file, transferFile.file.name);
    formData.append('password', transferFile.password);

    let headers = new Headers();
    headers.append('Accept', 'application/json');

    let options = new RequestOptions({ headers: headers });

    this.http.post(this.url + '/api/v1/documents/', formData, options)
      .map(res => res.text())
      //.catch(this.handleError)
      .subscribe(
      data => {
        console.log('success');
        transferFile.id = data;
        transferFile.status = 'uploaded';
        transferFile.statusText = 'uploaded';
      },
      error => {
        console.log(error);
        transferFile.status = 'upload-failed';
        transferFile.statusText = 'upload failed';
      }
      );
  }

  checkFile(transferFile: TransferFile): Promise<void> {
    return this.http.head(this.url + '/api/v1/documents/' + transferFile.id)
      .toPromise()
      .then(successResponse => {
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
      }, errorResponse => {
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
