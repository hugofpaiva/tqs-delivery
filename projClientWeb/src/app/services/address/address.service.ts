import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Review} from '../../models/review';
import {Address} from '../../models/address';

@Injectable({
  providedIn: 'root'
})
export class AddressService {

  constructor(private http: HttpClient) {
  }

  getAddresses(): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/address/getAll', environment.httpOptions);
  }

  createAddress(address: Address): Observable<any> {
    return this.http.post(`${environment.baseURL}/address/add`, address, environment.httpOptions);
  }

  delAddress(address: Address): Observable<any> {
    return this.http.delete(`${environment.baseURL}/address/del?addressId=${address.id}`, environment.httpOptions);
  }

}
