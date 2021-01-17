import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SettingsService } from './settings.service';
import { Settings } from './settings';

@Injectable({ providedIn: 'root' })
export class SettingsHttpService {
  constructor(
    private http: HttpClient,
    private settingsService: SettingsService
  ) {}

  initializeApp(): Promise<any> {
    console.debug('Getting settings via HttpService...');
    return new Promise((resolve) => {
      this.http
        .get('assets/settings.json')
        .toPromise()
        .then((response) => {
          console.debug('Got settings via HttpService');
          console.debug(response);
          this.settingsService.settings = <Settings>response;
          resolve();
        });
    });
  }
}
