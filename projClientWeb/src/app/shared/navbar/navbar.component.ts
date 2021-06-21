import {Component, OnInit} from '@angular/core';
import {Router, NavigationEnd, NavigationStart, ActivatedRoute} from '@angular/router';
import {faShoppingCart, faUser} from '@fortawesome/free-solid-svg-icons';
import {Location, PopStateEvent} from '@angular/common';
import {first} from 'rxjs/operators';
import {AccountService} from '../../services/account/account.service';
import {CartService} from '../../services/cart/cart.service';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
    public isCollapsed = true;
    private lastPoppedUrl: string;
    private yScrollStack: number[] = [];
    cartIcon = faShoppingCart;
    userIcon = faUser;

    constructor(public location: Location, private router: Router,
                public accountService: AccountService, private cartService: CartService) {
    }

    ngOnInit() {
        this.router.events.subscribe((event) => {
            this.isCollapsed = true;
            if (event instanceof NavigationStart) {
                if (event.url !== this.lastPoppedUrl) {
                    this.yScrollStack.push(window.scrollY);
                }
            } else if (event instanceof NavigationEnd) {
                if (event.url === this.lastPoppedUrl) {
                    this.lastPoppedUrl = undefined;
                    window.scrollTo(0, this.yScrollStack.pop());
                } else {
                    window.scrollTo(0, 0);
                }
            }
        });
        this.location.subscribe((ev: PopStateEvent) => {
            this.lastPoppedUrl = ev.url;
        });
    }

    logout() {
        this.accountService.logout();
    }

    goHome() {
        if (this.accountService.userValue != null) {
            this.router.navigate(['/']);
        } else {
            this.router.navigate(['/login']);
        }
    }
}
