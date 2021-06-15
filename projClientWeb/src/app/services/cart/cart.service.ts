import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {Product} from '../../models/product';
import {Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {User} from '../../models/user';
import {environment} from '../../../environments/environment';
import {map} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private productsSubject: BehaviorSubject<Product[]>;
    public products: Observable<Product[]>;

    constructor() {
        this.productsSubject = new BehaviorSubject<Product[]>([]);
        this.products = this.productsSubject.asObservable();
    }

    public get productsValue(): Product[] {
        return this.productsSubject.value;
    }

    public get totalProducts(): number {
        let n = 0;
        this.productsSubject.value.forEach((p) => {
            n = n + Number(p.quantity);
        });
        return n;
    }

    public get totalPrice(): number {
        let n = 0;
        this.productsSubject.value.forEach((p) => {
            n = n + Number(p.price * p.quantity);
        });
        return n;
    }

    addProduct(product: Product) {
        const plist = this.productsSubject.value;

        const index = plist.indexOf(product);
        if (index !== -1) {
            if (plist[index].quantity < 10) {
                plist[index].quantity = plist[index].quantity + 1;
            }
        } else {
            product.quantity = 1;
            plist.push(product);
        }

        this.productsSubject.next(plist);
    }

    removeProduct(product: Product) {
        const plist = this.productsSubject.value;

        const index = plist.indexOf(product);
        if (index !== -1) {
            if (plist[index].quantity > 1) {
                plist[index].quantity = plist[index].quantity - 1;
            } else {
                plist.splice(index, 1);
            }
        }

        this.productsSubject.next(plist);
    }

    changeQuantityProduct(product: Product, quantity: number) {
        const plist = this.productsSubject.value;

        const index = plist.indexOf(product);
        if (index !== -1) {
            plist[index].quantity = quantity;
        }

        this.productsSubject.next(plist);
    }
}
