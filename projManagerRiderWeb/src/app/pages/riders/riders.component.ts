import { Component, OnInit } from '@angular/core';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';
import {RiderService} from '../../services/rider/rider.service';
import {Rider} from '../../models/rider';

@Component({
  selector: 'app-riders',
  templateUrl: './riders.component.html',
  styleUrls: ['./riders.component.css']
})
export class RidersComponent implements OnInit {
  starIcon = faStar;
  halfStarIcon = faStarHalfAlt;
  riders: Rider[] = [];
  totalItems = 0;
  totalPages = 0;
  currentPage = 1;
  inProgress = 0;
  avgTime = null;
  avgRating: Number = null;
  math = Math;

  constructor(private riderService: RiderService) { }

  ngOnInit(): void {
    this.getRiders();
    this.getRiderStats();
  }


  getRiders() {
    this.riderService.getRiders(this.currentPage - 1)
      .subscribe(
        data => {
          this.riders = data['riders'];
          this.totalItems = data['totalItems'];
          this.totalPages = data['totalPages'];
        });
  }

  getRiderStats() {
    this.riderService.getRiderManagerStats()
      .subscribe(
        data => {
          this.inProgress = data['inProcess'];
          this.avgTime = data['avgTimes'];
          this.avgRating = data['avgReviews'];
        });
  }

  millisToMinutesAndSeconds(millis: number) {
    const minutes = Math.floor(millis / 60000);
    const seconds: number = Number(((millis % 60000) / 1000).toFixed(0));
    return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
  }

  getPage(event) {
    this.currentPage = event;
    this.getRiders();
    this.getRiderStats();
  }

}
