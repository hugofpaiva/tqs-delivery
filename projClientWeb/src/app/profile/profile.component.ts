import {Component, Input, OnInit} from '@angular/core';
import {
    faArrowDown,
    faStar,
    faMotorcycle,
    faInfoCircle,
    faPlusCircle,
    faTimesCircle,
    faArrowLeft,
    faArrowRight
} from '@fortawesome/free-solid-svg-icons';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Address} from '../models/address';
import {AccountService} from '../services/account/account.service';
import {PurchaseService} from '../services/purchase/purchase.service';
import {Product} from '../models/product';
import {Purchase} from '../models/purchase';
import {ReviewService} from '../services/review/review.service';
import {Review} from '../models/review';
import {AlertService} from '../services/alert/alert.service';

@Component({
    selector: 'app-modal-rider-review',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">Rider Review</h5>
        </div>
        <div class="modal-body">
            <h6 class="mb-0">Order delivered by <b>{{purchase.riderName}}</b> at <b>{{purchase.date | date:'medium'}}</b></h6>

            <div style="display: flex; justify-content: center; font-size: 2.5rem; margin-top: 10%">
                <ngb-rating [(rate)]="rate" [max]="max"></ngb-rating>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" [disabled]="requested" (click)="giveReview()" class="btn btn-primary">Add Review</button>
            <button type="button" class="btn btn-link  ml-auto" data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalRiderReview {
    @Input() purchase: Purchase;
    requested = false;
    rate = 0;
    max: Number = 5;

    constructor(public activeModal: NgbActiveModal, private reviewService: ReviewService, private alertService: AlertService) {
    }

    giveReview() {
        this.reviewService.giveReview(new Review(this.purchase.id, this.rate)).subscribe(data => {
            this.alertService.success('Review added!');
        }, error => {
            this.alertService.error('There was an error. Review was not added!');
        });

    }
}

@Component({
    selector: 'app-modal-order-details',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">Order #{{purchase.id}}</h5>
        </div>
        <div class="modal-body">
            <h6 class="mb-0">
                <fa-icon [icon]="infoIcon"></fa-icon>
                <span style="margin-right: 8px; margin-left: 9px">Status:</span> <b>{{purchase.status}}</b></h6>
            <h6 class="mb-0">
                <fa-icon [icon]="motorcycleIcon"></fa-icon>
                <span style="margin-right: 15px; margin-left: 5px">Rider:</span> <b>{{purchase.riderName !== null ? purchase.riderName : '-'}}</b></h6>

            <div class="table" style="min-height: 200px; margin-top: 5%">
                <table class="table">
                    <thead class="text-info">
                    <th>
                        Product
                    </th>
                    <th>
                        Units
                    </th>
                    <th>
                        Unit Price
                    </th>
                    <th>
                        Total
                    </th>
                    </thead>
                    <tbody>
                    <tr *ngFor="let product of getEach(purchase)">
                        <td>
                            {{product.name}}
                        </td>
                        <td>
                            {{product.quantity}}
                        </td>
                        <td>
                            {{product.price}}
                        </td>
                        <td>
                            {{product.price * product.quantity}}€
                        </td>
                    </tr>
                    </tbody>
                </table>
                <h6 class="mb-0" style="text-align: right;">Total: {{getTotal(purchase)}}€<b></b></h6>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-link  ml-auto" data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalOrderDetails {
    @Input() purchase: Purchase;
    motorcycleIcon = faMotorcycle;
    infoIcon = faInfoCircle;
    rate = 0;
    max: Number = 5;

    constructor(public activeModal: NgbActiveModal) {
    }

    getEach(purchase: Purchase): Product[] {
        const products = [];
        purchase.products.forEach((p) => {
            const index = products.indexOf(p);
            if (index !== -1) {
                products[index].quantity = products[index].quantity + 1;
            } else {
                p.quantity = 1;
                products.push(p);
            }
        });
        return products;
    }

    getTotal(purchase: Purchase): Number {
        let total = 0;
        purchase.products.forEach((p) => {
            total = total + Number(p.price);
        });
        return total;
    }
}

@Component({
    selector: 'app-modal-manage-addresses',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">{{name}}</h5>
            <button class="btn btn-link  ml-auto">
                <fa-icon size="lg" (click)="newAddress()" [icon]="plusIcon"></fa-icon>
            </button>
        </div>
        <div class="modal-body" style="min-width: 500px;">

            <div *ngIf="creatingAddress" style="min-height: 200px; margin-top: 5%">
                <form [formGroup]="newAddressForm">
                    <div class="modal-body"
                         style="display: flex; justify-content: space-around; align-items: center">

                        <div class="form-group">
                            <label>Address</label>
                            <input #address type="text"
                                   [(ngModel)]="address.address"
                                   formControlName="address" class="form-control"
                                   [ngClass]="{ 'is-invalid': f.address.errors }"/>
                            <div *ngIf=" !requested && f.address.errors"
                                 class="invalid-feedback">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Postal Code</label>
                            <input #postalcode type="text"
                                   [(ngModel)]="address.postalcode"
                                   formControlName="postalcode" class="form-control"
                                   [ngClass]="{ 'is-invalid': f.postalcode.errors}"/>
                            <div *ngIf=" !requested && f.postalcode.errors"
                                 class="invalid-feedback">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>City</label>
                            <input #city type="text"
                                   [(ngModel)]="address.city"
                                   formControlName="city" class="form-control"
                                   [ngClass]="{ 'is-invalid': f.city.errors}"/>
                            <div *ngIf=" !requested && f.city.errors"
                                 class="invalid-feedback">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Country</label>
                            <input #country type="text"
                                   [(ngModel)]="address.country"
                                   formControlName="country" class="form-control"
                                   [ngClass]="{ 'is-invalid': f.country.errors }"/>
                            <div *ngIf=" !requested && f.country.errors"
                                 class="invalid-feedback">
                            </div>
                        </div>


                        <button [disabled]="requested
      || f.city.errors || f.country.errors || f.address.errors || f.postalcode.errors"
                                type="submit" rounded="true"
                                class="btn btn-info">
                            <fa-icon [icon]="plusIcon"></fa-icon>
                        </button>

                    </div>
                </form>
            </div>

            <div *ngIf="!creatingAddress" class="table" style="min-height: 200px; margin-top: 5%">
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
                    <tr>
                        <td>
                            Rua Quim Jo
                        </td>
                        <td>
                            3657-123
                        </td>
                        <td>
                            Aveiro
                        </td>
                        <td>
                            Portugal
                        </td>
                        <td style="width: 25px">
                            <button class="btn btn-link  ml-auto" style="padding: 0">
                                <fa-icon size="lg" style="color: red" (click)="deleteAddress()" [icon]="deleteIcon"></fa-icon>
                            </button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-link  ml-auto" data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalManageAddresses {
    @Input() name;
    creatingAddress = false;
    plusIcon = faPlusCircle;
    deleteIcon = faTimesCircle;
    private newAddressForm: FormGroup;
    requested = false;
    @Input() address: Address = new Address();

    newAddress() {
        if (!this.creatingAddress) {
            this.creatingAddress = true;
        } else {
            // Clear forms
            console.log('Olha o novo endereço');
        }

    }

    deleteAddress() {
        console.log('Olha a eliminar o endereço');
    }

    constructor(private formBuilder: FormBuilder, public activeModal: NgbActiveModal) {
        this.newAddressForm = this.formBuilder.group({
            address: ['address', [Validators.required]],
            postalcode: ['postalcode', [Validators.required]],
            city: ['city', [Validators.required]],
            country: ['country', [Validators.required]],
        });
    }

    // convenience getter for easy access to form fields
    get f(): any {
        return this.newAddressForm.controls;
    }
}

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
})

export class ProfileComponent implements OnInit {
    arrowIcon = faArrowDown;
    starIcon = faStar;
    arrowLeftIcon = faArrowLeft;
    arrowRightIcon = faArrowRight;
    purchases: Purchase[] = [];
    totalItems = 0;
    driversReviews = 0;
    totalPages = 0;
    currentPage = 1;

    constructor(private modalService: NgbModal, private accountService: AccountService, private purchaseService: PurchaseService) {
    }

    openRiderReview(purchase: Purchase) {
        const modalRef = this.modalService.open(NgbModalRiderReview, {centered: true});
        modalRef.componentInstance.purchase = purchase;
    }

    openOrderDetails(purchase: Purchase) {
        const modalRef = this.modalService.open(NgbModalOrderDetails, {centered: true, scrollable: true});
        modalRef.componentInstance.purchase = purchase;
    }

    openManageAddresses() {
        const modalRef = this.modalService.open(NgbModalManageAddresses, {
            centered: true,
            size: 'xl',
            scrollable: true,
            windowClass: 'my-class'
        });
        modalRef.componentInstance.name = 'Manage Addresses';
    }

    ngOnInit(): void {
        this.getPurchases();
    }

    getPage(event) {
        this.getPurchases();
    }

    getTotal(purchase: Purchase): Number {
        let total = 0;
        purchase.products.forEach((p) => {
            total = total + Number(p.price);
        });
        return total;
    }

    getPurchases() {
        this.purchaseService.getPurchases(this.currentPage - 1)
            .subscribe(
                data => {
                    console.log(data);
                    this.totalItems = data['totalItems'];
                    this.totalPages = data['totalPages'];
                    this.purchases = data['orders'];
                    this.driversReviews = data['reviewsGiven'];
                });
    }

}
