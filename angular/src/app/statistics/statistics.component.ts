import { Component, OnInit } from '@angular/core';
import { GetStatisticResponse } from '../restclient';
import { RestrictionRemoverService } from '../restriction-remover.service';
//import 'rxjs/add/operator/map';
//import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css'],
})
export class StatisticsComponent implements OnInit {
  statisticsJson!: string;

  constructor(private restrictionRemoverService: RestrictionRemoverService) {}

  ngOnInit() {
    this.getStatistics();
  }

  getStatistics(): void {
    this.restrictionRemoverService
      .getStatistics()
      .then((statistics: GetStatisticResponse) => {
        this.statisticsJson = JSON.stringify(statistics, null, '\t');
      });
  }
}
