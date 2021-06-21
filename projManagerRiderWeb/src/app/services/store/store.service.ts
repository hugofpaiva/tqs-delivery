import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  constructor(private http: HttpClient) {
  }

  getStore(pageNo = 0): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/manager/stores?pageSize=7&pageNo=' + pageNo, environment.httpOptions);
  }

  getStoresStats(): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/manager/statistics', environment.httpOptions);
  }
}
