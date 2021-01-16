import { Component, OnInit } from '@angular/core';
import { RestrictionRemoverService } from '../restriction-remover.service';
import { TransferFile } from '../transfer-file';
import { timer } from 'rxjs';
import { SettingsService } from '../settings.service';

@Component({
  selector: 'app-file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.css'],
})
export class FileListComponent implements OnInit {
  maximumFileSize: number | undefined;

  transferFiles: TransferFile[] = [];

  commaSeperatedDoneFiles!: string;

  url = this.settingsService.settings.apiUrl;

  constructor(
    private restrictionRemoverService: RestrictionRemoverService,
    private settingsService: SettingsService
  ) {}

  ngOnInit() {
    console.debug('Initializing file-list.component...');
    console.log(
      'Reminder: Set log level to verbose in your Browser to see console.debug() messages'
    );

    let fileCheckTimer = timer(1000, 1000);
    fileCheckTimer.subscribe((t: any) => this.checkFiles(t));

    this.getMaximumFileSize();
  }

  updateCommaSeperatedDoneFiles(): void {
    console.debug('updateCommaSeperatedDoneFiles()...');

    let doneFiles: string[] = [];
    for (let transferFile of this.transferFiles) {
      if (transferFile.done === true && transferFile.status === 'done') {
        doneFiles.push(String(transferFile.id));
      }
    }

    this.commaSeperatedDoneFiles = doneFiles.join(',');
    console.debug('commaSeperatedDoneFiles=' + this.commaSeperatedDoneFiles);
  }

  checkFiles(t: any): void {
    console.debug('Checking files...');
    for (let transferFile of this.transferFiles) {
      if (
        transferFile.done === false &&
        transferFile.status !== 'uploading' &&
        transferFile.status !== 'upload-failed'
      ) {
        this.restrictionRemoverService
          .checkFile(transferFile)
          .then((success) => {
            this.updateCommaSeperatedDoneFiles();
          });
      }
    }
  }

  getMaximumFileSize(): void {
    console.debug('Getting maximum file size...');
    this.restrictionRemoverService
      .getMaximumFileSize()
      .then((maximumFileSize) => {
        console.debug(`Got maximum maximum file size: ${maximumFileSize}`);
        this.maximumFileSize = maximumFileSize;
        console.debug(`Set maximum maximum file size: ${this.maximumFileSize}`);
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

      if (file.size <= this.maximumFileSize!) {
        console.debug('The file is small enough for the server to accept.');
        this.restrictionRemoverService.uploadDocument(transferFile);
      } else {
        console.debug('The file is bigger than the server would accept.');
        transferFile.done = true;
        transferFile.status = 'too-big-failed';
        transferFile.statusText = 'File is too big, did not upload.';
      }
    }
  }
}
