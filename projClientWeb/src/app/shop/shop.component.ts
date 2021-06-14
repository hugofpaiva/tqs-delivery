import {Component, OnInit} from '@angular/core';
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
    totalItems = 0;
    totalPages = 0;
    currentPage = 1;
    name: String = null;
    maxPrice: Number = null;
    minPrice: Number = null;
    orderBy: String = null;
    category: String = null;

    constructor(private productService: ProductService) {
    }

    ngOnInit(): void {
        this.getProducts();
    }

    getProducts() {
        this.productService.getProducts(this.currentPage - 1 , this.name, this.maxPrice, this.minPrice, this.orderBy, this.category)
            .subscribe(
            data => {
                this.totalItems = data['totalItems'];
                this.totalPages = data['totalPages'];
                this.products = data['products'];
            });
    }

    getPage(event) {
        this.getProducts();
    }

    getName(event) {
        if (event !== '') {
            this.name = event;
        } else {
            this.name = null;
        }
    }

    getMinPrice(event) {
        if (event !== '') {
            this.minPrice = event;
        } else {
            this.minPrice = null;
        }
    }

    getMaxPrice(event) {
        if (event !== '') {
            this.maxPrice = event;
        } else {
            this.maxPrice = null;
        }
    }

    getOrderBy(event) {
        if (event === 'Cheapest') {
            this.orderBy = 'price';
        } else {
            this.orderBy = null;
        }
        this.getProducts();
    }

    getCategory(event) {
        if (event === this.category) {
            this.category = null;
        } else {
            this.category = event;
        }
        this.getProducts();
    }

}
