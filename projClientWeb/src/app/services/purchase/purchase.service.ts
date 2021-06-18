import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {

  constructor(private http: HttpClient) {
  }

  makePurchase(addressId: number, productsId: Number[]): Observable<any> {
    console.log(productsId);
    return this.http.post(`${environment.baseURL}/purchase/new`, {'addressId': addressId, 'productsId': productsId},
        environment.httpOptions);
  }

  getPurchases(pageNo = 0): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/purchase/getAll?pageNo=' + pageNo, environment.httpOptions);
  }

}
