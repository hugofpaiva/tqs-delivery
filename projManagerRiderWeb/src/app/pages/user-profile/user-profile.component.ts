import {Component, OnInit} from '@angular/core';
import {faStar} from '@fortawesome/free-solid-svg-icons';
import {AccountService} from '../../services/account/account.service';
import {Router} from '@angular/router';
import {Purchase} from '../../models/purchase';
import {PurchaseService} from '../../services/purchase/purchase.service';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  starIcon = faStar;
  purchases: Purchase[] = [];
  totalItems = 0;
  totalPages = 0;
  currentPage = 1;

  constructor(private accountService: AccountService, private router: Router, private purchaseService: PurchaseService) {
  }

  ngOnInit() {
    if (this.accountService.userValue.type['authority'] === 'Manager') {
      this.router.navigateByUrl('/stores');
    } else {
      this.getPurchases();
    }
  }

  getPurchases() {
    this.purchaseService.getPurchases(this.currentPage - 1)
      .subscribe(
        data => {
          this.totalItems = data['totalItems'];
          this.totalPages = data['totalPages'];
          this.purchases = data['orders'];
        });
  }

  getPage(event) {
    this.getPurchases();
  }

}
