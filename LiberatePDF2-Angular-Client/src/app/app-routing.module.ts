import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FileListComponent }   from './file-list.component';
import { StatisticsComponent }      from './statistics.component';

const routes: Routes = [
  { path: '', redirectTo: '/filelist', pathMatch: 'full' },
  { path: 'filelist',  component: FileListComponent },
  { path: 'statistics',     component: StatisticsComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}