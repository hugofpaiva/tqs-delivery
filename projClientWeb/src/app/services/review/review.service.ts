import { Injectable } from '@angular/core';
import {Review} from '../../models/review';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  public configObservable = new Subject<number>();

  constructor(private http: HttpClient) { }

  giveReview(review: Review): Observable<any> {
    return this.http.post(`${environment.baseURL}/review/add`, review, environment.httpOptions);
  }

  emitConfig(val) {
    this.configObservable.next(val);
  }
}
