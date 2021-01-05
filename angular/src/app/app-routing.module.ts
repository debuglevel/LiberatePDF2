import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { StatisticsComponent } from './statistics/statistics.component';
import { FileListComponent } from './file-list/file-list.component';

const routes: Routes = [
  { path: '', redirectTo: '/filelist', pathMatch: 'full' },
  { path: 'filelist', component: FileListComponent },
  { path: 'statistics', component: StatisticsComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
