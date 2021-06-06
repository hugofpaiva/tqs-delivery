import { Component, OnInit } from '@angular/core';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-riders',
  templateUrl: './riders.component.html',
  styleUrls: ['./riders.component.css']
})
export class RidersComponent implements OnInit {
  starIcon = faStar;
  halfStarIcon = faStarHalfAlt;

  constructor() { }

  ngOnInit(): void {
  }

}
