import { Injectable } from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {

  constructor(private http: HttpClient) {
  }

  getPurchases(pageNo = 0): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/rider/orders?pageSize=7&pageNo=' + pageNo, environment.httpOptions);
  }

  getMostDeliveredCities(): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/manager/riders/top_delivered_cities', environment.httpOptions);
  }

}
