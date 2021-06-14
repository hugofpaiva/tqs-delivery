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
            (name === null ? '' : '&name=' + name)
            + (maxPrice === null ? '' : '&maxPrice=' + maxPrice)
            + (minPrice === null ? '' : '&minPrice=' + minPrice)
            + (orderBy === null ? '' : '&orderBy=' + orderBy)
            + (category === null ? '' : '&category=' + category), environment.httpOptions);
    }

}
