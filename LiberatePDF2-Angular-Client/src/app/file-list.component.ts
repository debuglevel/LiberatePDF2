import { Component, OnInit } from '@angular/core';
import { RestrictionRemoverService } from './restriction-remover.service';
import { Http } from '@angular/http';
import { TransferFile } from './transfer-file';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.css']
})
export class FileListComponent implements OnInit {

  maximumFileSize: number;

  transferFiles: TransferFile[] = [];

  url = `http://localhost:8080`;

  constructor(
    private restrictionRemoverService: RestrictionRemoverService,
    private http: Http
  ) { }

  ngOnInit() {
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(t => this.checkFiles(t));
  }

  checkFiles(t: any): void {
    for (let transferFile of this.transferFiles) {
      if (transferFile.done === false && transferFile.status !== 'uploading') {
        this.restrictionRemoverService.checkFile(transferFile);
      }
    }
  }

  getMaximumFileSize(): void {
    this.restrictionRemoverService.getMaximumFileSize().then(maximumFileSize => {
      this.maximumFileSize = maximumFileSize;
    });
  }

  onFileChanged(event: any, password: string) {
    let files: FileList = event.target.files;

    for (let i = 0; i < files.length; i++) {
      let file = files[i];
      let transferFile: TransferFile = {
        file: file,
        id: null,
        name: file.name,
        password: password,
        status: 'uploading',
        statusText: 'uploading',
        done: false
      };
      this.transferFiles.push(transferFile);

      this.restrictionRemoverService.uploadDocument(transferFile);
    }
  }
}
