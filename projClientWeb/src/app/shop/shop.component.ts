import { Component, OnInit } from '@angular/core';
import {faArrowLeft, faArrowRight} from '@fortawesome/free-solid-svg-icons';
import {Product} from '../models/product';
import {ProductService} from '../services/product/product.service';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.css']
})
export class ShopComponent implements OnInit {
  arrowLeftIcon = faArrowLeft;
  arrowRightIcon = faArrowRight;
  products: Product[] = [];
  totalItem = 0;
  totalPages = 0;
  currentPage = 0;

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
  }

  getProducts(pageNo = 0, name: String, maxPrice: Number, minPrice: Number, orderBy: String, category: String) {
    this.productService.getProducts(pageNo, name, maxPrice, minPrice, orderBy, category).subscribe(
        data => {
          
        })
  }

}
