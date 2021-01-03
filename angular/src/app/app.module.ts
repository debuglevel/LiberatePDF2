import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { FileListComponent } from './file-list.component';
import { StatisticsComponent } from './statistics.component';
import { RestrictionRemoverService } from './restriction-remover.service';

@NgModule({
  imports: [BrowserModule, HttpModule, NgbModule.forRoot(), AppRoutingModule],
  declarations: [AppComponent, FileListComponent, StatisticsComponent],
  bootstrap: [AppComponent],
  providers: [RestrictionRemoverService],
})
export class AppModule { }
