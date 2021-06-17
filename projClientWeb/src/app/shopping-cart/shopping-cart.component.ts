import { Component, OnInit } from '@angular/core';
import {CartService} from '../services/cart/cart.service';
import {AccountService} from '../services/account/account.service';

@Component({
  selector: 'app-shopping-cart',
  templateUrl: './shopping-cart.component.html',
  styleUrls: ['./shopping-cart.component.css']
})
export class ShoppingCartComponent implements OnInit {

  constructor(private cartService: CartService) { }

  ngOnInit(): void {
  }

  getCartService(): CartService {
    return this.cartService;
  }

}
