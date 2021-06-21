import {Component, Input, OnInit} from '@angular/core';
import {CartService} from '../services/cart/cart.service';
import {Address} from '../models/address';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {AddressService} from '../services/address/address.service';
import {AlertService} from '../services/alert/alert.service';
import {PurchaseService} from '../services/purchase/purchase.service';
import {Router} from '@angular/router';

@Component({
    selector: 'app-modal-manage-addresses',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">Select Address to Deliver</h5>
        </div>
        <div class="modal-body" style="min-width: 500px;">

            <div class="table" style="min-height: 200px; margin-top: 5%">
                <table class="table">
                    <thead class="text-info">
                    <th>
                        Address
                    </th>
                    <th>
                        Postal Code
                    </th>
                    <th>
                        City
                    </th>
                    <th>
                        Country
                    </th>
                    <th style="width: 25px">
                    </th>
                    </thead>
                    <tbody>
                    <tr *ngFor="let add of addresses">
                        <td>
                            {{add.address}}
                        </td>
                        <td>
                            {{add.postalCode}}
                        </td>
                        <td>
                            {{add.city}}
                        </td>
                        <td>
                            {{add.country}}
                        </td>
                        <td style="width: 25px">
                            <input type="checkbox" [disabled]="selected !== null && selected.id !== add.id" (change)="changeSelected(add)">
                        </td>
                    </tr>
                    </tbody>
                </table>
                <h5 style="text-align: center" *ngIf="addresses.length === 0">Add new addresses on your profile to make a new order!</h5>
            </div>
        </div>
        <div class="modal-footer" style="display: flex; justify-content: space-between">
            <button type="button" class="btn btn-primary  ml-auto" [disabled]="requested && addresses.length === 0 && selected == null"
                    data-dismiss="modal" (click)="makePurchase()">Make Order
            </button>
            <button type="button" class="btn btn-link  ml-auto"
                    data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalBuy implements OnInit {
    @Input() addresses: Address[] = [];
    selected: Address = null;
    requested = false;

    constructor(public activeModal: NgbActiveModal, private addressService: AddressService,
                private alertService: AlertService, private cartService: CartService, private purchaseService: PurchaseService,
                private router: Router) {
    }

    changeSelected(address: Address) {
        if (address === this.selected) {
            this.selected = null;
        } else {
            this.selected = address;
        }
    }

    getCartService(): CartService {
        return this.cartService;
    }

    makePurchase() {
        if (this.selected === null || this.cartService.totalProducts === 0) {
            this.alertService.error('No address selected!');
        } {
            this.purchaseService.makePurchase(this.selected.id, this.cartService.productsIds).subscribe(data => {
                this.alertService.success('Order made!');
                this.cartService.empty();
                this.activeModal.close();
            }, error => {
                this.alertService.error('There was an error. Order was not made!');
            });
        }
    }

    ngOnInit(): void {
        this.getAddresses();
    }

    getAddresses() {
        this.addressService.getAddresses()
            .subscribe(
                data => {
                    this.addresses = data;
                });
    }
}

@Component({
    selector: 'app-shopping-cart',
    templateUrl: './shopping-cart.component.html',
    styleUrls: ['./shopping-cart.component.css']
})
export class ShoppingCartComponent implements OnInit {

    constructor(private cartService: CartService, private modalService: NgbModal) {
    }

    openBuy() {
        const modalRef = this.modalService.open(NgbModalBuy, {
            centered: true,
            size: 'xl',
            scrollable: true,
            windowClass: 'my-class'
        });
    }

    ngOnInit(): void {
    }

    getCartService(): CartService {
        return this.cartService;
    }

}
