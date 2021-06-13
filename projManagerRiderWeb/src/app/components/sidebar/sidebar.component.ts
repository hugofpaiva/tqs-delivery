import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AccountService} from '../../services/account/account.service';

declare interface RouteInfo {
  path: string;
  title: string;
  icon: string;
  class: string;
}

export let ROUTES: RouteInfo[] = [];

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  public menuItems: any[];
  public isCollapsed = true;

  constructor(private router: Router, private accountService: AccountService) {
  }

  ngOnInit() {
    ROUTES = [];
    if (this.accountService.userValue.type['authority'] === 'Manager') {
      ROUTES.push({path: '/stores', title: 'Stores Info', icon: 'ni-basket text-red', class: ''},
        {path: '/riders', title: 'Riders Info', icon: 'ni-delivery-fast text-blue', class: ''});
    } else {
      ROUTES.push({path: '/user-profile', title: 'User profile', icon: 'ni-single-02 text-yellow', class: ''});
    }

    this.menuItems = ROUTES.filter(menuItem => menuItem);
    this.router.events.subscribe((event) => {
      this.isCollapsed = true;
    });
  }
}
