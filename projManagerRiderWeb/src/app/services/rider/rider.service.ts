import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RiderService {

  constructor(private http: HttpClient) { }

  getRiders(pageNo = 0): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/manager/riders/all?pageSize=7&pageNo=' + pageNo, environment.httpOptions);
  }

  getRiderStats(): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/rider/reviews/stats', environment.httpOptions);
  }

  getRiderManagerStats(): Observable<any> {
    return this.http.get<any>(environment.baseURL + '/manager/riders/stats', environment.httpOptions);
  }
}
