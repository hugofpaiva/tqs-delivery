import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {RouterModule} from '@angular/router';
import {AppRoutingModule} from './app.routing';

import {AppComponent} from './app.component';
import {SignupComponent} from './signup/signup.component';
import {NgbModalManageAddresses, NgbModalOrderDetails, NgbModalRiderReview, ProfileComponent} from './profile/profile.component';
import {NavbarComponent} from './shared/navbar/navbar.component';
import {FooterComponent} from './shared/footer/footer.component';

import {HomeModule} from './home/home.module';
import {LoginComponent} from './login/login.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import { ShopComponent } from './shop/shop.component';
import { ShoppingCartComponent } from './shopping-cart/shopping-cart.component';

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
        NgbModalManageAddresses,
        ShopComponent,
        ShoppingCartComponent
    ],
    imports: [
        BrowserModule,
        NgbModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        AppRoutingModule,
        HomeModule,
        FontAwesomeModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
