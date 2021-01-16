import { Settings } from './settings';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  public settings: Settings;

  constructor() {
    //console.debug('Ctor SettingsService...');
    this.settings = new Settings();
  }
}
