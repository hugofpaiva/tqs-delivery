import { Component, OnInit } from '@angular/core';
import {faStar} from '@fortawesome/free-solid-svg-icons';
import {AccountService} from '../../services/account/account.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  starIcon = faStar;

  constructor(private accountService: AccountService, private router: Router) { }

  ngOnInit() {
    if (this.accountService.userValue.type['authority'] === 'Manager') {
      this.router.navigateByUrl('/stores');
    }
  }

}
