import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class ProductService {


    constructor(private http: HttpClient) {
    }

    getProducts(pageNo = 0, name: String, maxPrice: Number, minPrice: Number, orderBy: String, category: String): Observable<any> {
        return this.http.get<any>(environment.baseURL + '/product/getAll?pageNo=' + pageNo +
            (name === undefined ? '' : '&name=' + name)
            + (maxPrice === undefined ? '' : '&maxPrice=' + maxPrice)
            + (minPrice === undefined ? '' : '&minPrice=' + minPrice)
            + (orderBy === undefined ? '' : '&orderBy=' + orderBy)
            + (category === undefined ? '' : '&category=' + category), environment.httpOptions);
    }

}
