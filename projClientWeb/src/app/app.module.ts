import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {Router, RouterModule} from '@angular/router';
import {AppRoutingModule} from './app.routing';

import {AppComponent} from './app.component';
import {SignupComponent} from './signup/signup.component';
import {NgbModalManageAddresses, NgbModalOrderDetails, NgbModalRiderReview, ProfileComponent} from './profile/profile.component';
import {NavbarComponent} from './shared/navbar/navbar.component';
import {FooterComponent} from './shared/footer/footer.component';

import {LoginComponent} from './login/login.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import { ShopComponent } from './shop/shop.component';
import {NgbModalBuy, ShoppingCartComponent} from './shopping-cart/shopping-cart.component';
import { AlertComponent } from './shared/alert/alert.component';
import {AccountService} from './services/account/account.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {ErrorInterceptor} from './shared/error.interceptor';
import {JwtInterceptor} from './shared/jwt.interceptor';
import {NgxPaginationModule} from 'ngx-pagination';

@NgModule({
    declarations: [
        AppComponent,
        SignupComponent,
        ProfileComponent,
        NavbarComponent,
        FooterComponent,
        LoginComponent,
        NgbModalRiderReview,
        NgbModalOrderDetails,
        NgbModalBuy,
        NgbModalManageAddresses,
        ShopComponent,
        ShoppingCartComponent,
        AlertComponent
    ],
    imports: [
        BrowserModule,
        NgbModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        AppRoutingModule,
        FontAwesomeModule,
        HttpClientModule,
        NgxPaginationModule
    ],
    providers: [
        { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
    constructor(
        private router: Router,
        private accountService: AccountService
    ) {
        // redirect to home if already logged in
        if (this.accountService.userValue) {
            this.router.navigate(['/']);
        } else {
            this.router.navigate(['/login']);
        }
    }
}
