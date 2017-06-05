import { Component, OnInit } from '@angular/core';
import { RestrictionRemoverService } from './restriction-remover.service';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'statistics',
  templateUrl: './statistics.component.html',
  // styleUrls: ['./statistics.component.css']
})

export class StatisticsComponent implements OnInit {
  statisticsJson: string;

  constructor(
    private restrictionRemoverService: RestrictionRemoverService,
    private http: Http
  ) { }

  ngOnInit() {
    this.getStatistics();
  }

  getStatistics(): void {
    this.restrictionRemoverService.getStatistics().then(json => {
      this.statisticsJson = JSON.stringify(json, null, '\t');
    });
  }
}
