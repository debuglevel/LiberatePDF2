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

export function app_Init(settingsHttpService: SettingsHttpService) {
  return () => settingsHttpService.initializeApp();
}

@NgModule({
  declarations: [AppComponent, StatisticsComponent, FileListComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    //NgbModule.forRoot(),
    NgbModule,
  ],
  providers: [
    RestrictionRemoverService,
    {
      provide: APP_INITIALIZER,
      useFactory: app_Init,
      deps: [SettingsHttpService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
