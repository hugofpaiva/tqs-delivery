import { Component, OnInit } from '@angular/core';
import {StoreService} from '../../services/store/store.service';
import {Store} from '../../models/store';

@Component({
  selector: 'app-stores',
  templateUrl: './stores.component.html',
  styleUrls: ['./stores.component.css']
})
export class StoresComponent implements OnInit {
  stores: Store[] = [];
  totalItems = 0;
  totalPages = 0;
  currentPage = 1;
  totalPurchases = 0;
  avgPurchasesPerWeek = 0;

  constructor(private storeService: StoreService) { }

  ngOnInit(): void {
    this.getStores();
    this.getStoresStats();
  }

  getStores() {
    this.storeService.getStore(this.currentPage - 1)
      .subscribe(
        data => {
          this.totalItems = data['totalItems'];
          this.totalPages = data['totalPages'];
          this.stores = data['stores'];
        });
  }

  getStoresStats() {
    this.storeService.getStoresStats()
      .subscribe(
        data => {
          this.totalPurchases = data['totalPurchases'];
          this.avgPurchasesPerWeek = data['avgPurchasesPerWeek'];
        });
  }

  getPage(event) {
    this.currentPage = event;
    this.getStores();
    this.getStoresStats();
  }

}
