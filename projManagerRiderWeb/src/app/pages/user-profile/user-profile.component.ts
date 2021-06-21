import {Component, OnInit} from '@angular/core';
import {faStar} from '@fortawesome/free-solid-svg-icons';
import {AccountService} from '../../services/account/account.service';
import {Router} from '@angular/router';
import {Purchase} from '../../models/purchase';
import {PurchaseService} from '../../services/purchase/purchase.service';
import {RiderService} from '../../services/rider/rider.service';

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
  totalNumReviews = 0;
  avgReviews: Number = null;

  constructor(private accountService: AccountService, private router: Router, private purchaseService: PurchaseService,
              private riderService: RiderService) {
  }

  ngOnInit() {
    if (this.accountService.userValue.type['authority'] === 'Manager') {
      this.router.navigateByUrl('/stores');
    } else {
      this.getPurchases();
      this.getRiderStats();
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


  getRiderStats() {
    this.riderService.getRiderStats()
      .subscribe(
        data => {
          console.log(data);
          this.totalNumReviews = data['totalNumReviews'];
          this.avgReviews = data['avgReviews'];
        });
  }

  getPage(event) {
    this.currentPage = event;
    this.getPurchases();
    this.getRiderStats();
  }

}
