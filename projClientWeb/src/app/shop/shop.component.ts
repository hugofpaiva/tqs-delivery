import { Component, OnInit } from '@angular/core';
import {faArrowLeft, faArrowRight} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.css']
})
export class ShopComponent implements OnInit {
  arrowLeftIcon = faArrowLeft;
  arrowRightIcon = faArrowRight;

  constructor() { }

  ngOnInit(): void {
  }

}
