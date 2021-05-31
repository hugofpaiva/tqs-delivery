import { NgModule } from '@angular/core';
import { CommonModule, } from '@angular/common';
import { BrowserModule  } from '@angular/platform-browser';
import { Routes, RouterModule } from '@angular/router';

import { ProfileComponent } from './profile/profile.component';
import { SignupComponent } from './signup/signup.component';
import { LoginComponent } from './login/login.component';
import {ShopComponent} from './shop/shop.component';
import {ShoppingCartComponent} from './shopping-cart/shopping-cart.component';

const routes: Routes = [
    { path: 'profile',     component: ProfileComponent },
    { path: 'register',           component: SignupComponent },
    { path: 'login',          component: LoginComponent },
    { path: 'shop',     component: ShopComponent },
    { path: 'shopping-cart',     component: ShoppingCartComponent },
    { path: '', redirectTo: 'shop', pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    BrowserModule,
    RouterModule.forRoot(routes,{
      useHash: true
    })
  ],
  exports: [
  ],
})
export class AppRoutingModule { }
