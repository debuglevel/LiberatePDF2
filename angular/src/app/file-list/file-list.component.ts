import { Component, OnInit } from '@angular/core';
import { RestrictionRemoverService } from '../restriction-remover.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TransferFile } from '../transfer-file';
//import 'rxjs/add/operator/map';
//import 'rxjs/add/operator/toPromise';
import { timer } from 'rxjs';

@Component({
  selector: 'app-file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.css'],
})
export class FileListComponent implements OnInit {
  maximumFileSize!: number;

  transferFiles: TransferFile[] = [];

  doneFilesCommaSeperated!: string;

  url = `http://localhost:8080`;

  constructor(
    private restrictionRemoverService: RestrictionRemoverService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    let timerx = timer(1000, 1000);
    timerx.subscribe((t: any) => this.checkFiles(t));

    this.getMaximumFileSize();
  }

  updateDoneFilesCommaSeperated(): void {
    let doneFiles: string[] = [];

    for (let transferFile of this.transferFiles) {
      if (transferFile.done === true && transferFile.status === 'done') {
        doneFiles.push(String(transferFile.id));
      }
    }

    this.doneFilesCommaSeperated = doneFiles.join(',');
  }

  checkFiles(t: any): void {
    for (let transferFile of this.transferFiles) {
      if (transferFile.done === false && transferFile.status !== 'uploading') {
        this.restrictionRemoverService
          .checkFile(transferFile)
          .then((success) => {
            this.updateDoneFilesCommaSeperated();
          });
      }
    }
  }

  getMaximumFileSize(): void {
    this.restrictionRemoverService
      .getMaximumFileSize()
      .then((maximumFileSize) => {
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
        done: false,
      };
      this.transferFiles.push(transferFile);

      if (file.size <= this.maximumFileSize) {
        this.restrictionRemoverService.uploadDocument(transferFile);
      } else {
        transferFile.done = true;
        transferFile.status = 'too-big-failed';
        transferFile.statusText = 'file is too big, did not upload';
      }
    }
  }
}
