import { Component, OnInit } from '@angular/core';
import {faStar} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  starIcon = faStar;

  constructor() { }

  ngOnInit() {
  }

}
