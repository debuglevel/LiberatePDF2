import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { FileListComponent } from './file-list/file-list.component';

import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RestrictionRemoverService } from './restriction-remover.service';

@NgModule({
  declarations: [AppComponent, StatisticsComponent, FileListComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    //NgbModule.forRoot(),
    NgbModule,
  ],
  providers: [RestrictionRemoverService],
  bootstrap: [AppComponent],
})
export class AppModule {}
