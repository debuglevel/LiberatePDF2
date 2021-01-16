import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { FileListComponent } from './file-list/file-list.component';

import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RestrictionRemoverService } from './restriction-remover.service';
import { SettingsHttpService } from './settings-http.service';
import { SettingsService } from './settings.service';

import {
  ApiModule,
  Configuration,
  ConfigurationParameters,
} from './restclient';

export function app_Init(settingsHttpService: SettingsHttpService) {
  console.debug('app_Init...');
  return () => settingsHttpService.initializeApp();
}

export function initializeApiConfiguration(
  settingsHttpService: SettingsHttpService,
  settingsService: SettingsService
): Configuration {
  console.debug('initializeApiConfiguration...');
  const params: ConfigurationParameters = {
    basePath: settingsService.settings.apiUrl,
  };
  return new Configuration(params);
}

@NgModule({
  declarations: [AppComponent, StatisticsComponent, FileListComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    //NgbModule.forRoot(),
    NgbModule,
    ApiModule,
  ],
  providers: [
    RestrictionRemoverService,
    {
      provide: APP_INITIALIZER,
      useFactory: app_Init,
      deps: [SettingsHttpService],
      multi: true,
    },
    {
      provide: Configuration,
      useFactory: initializeApiConfiguration,
      deps: [SettingsHttpService, SettingsService],
      multi: false,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
