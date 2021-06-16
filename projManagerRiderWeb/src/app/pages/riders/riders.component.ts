import { Component, OnInit } from '@angular/core';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';
import {RiderService} from '../../services/rider/rider.service';
import {Store} from '../../models/store';
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
  math = Math;

  constructor(private riderService: RiderService) { }

  ngOnInit(): void {
    this.getRiders();
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

  getPage(event) {
    this.getRiders();
  }

}
