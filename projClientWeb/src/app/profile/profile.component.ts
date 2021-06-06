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

@Component({
    selector: 'app-modal-rider-review',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">{{name}}</h5>
        </div>
        <div class="modal-body">
            <h6 class="mb-0">Order delivered by <b>{{riderName}}</b> at <b>{{orderDate}}</b></h6>

            <div style="display: flex; justify-content: center; font-size: 2.5rem; margin-top: 10%">
                <ngb-rating [(rate)]="rate" [max]="max"></ngb-rating>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary">Add Review</button>
            <button type="button" class="btn btn-link  ml-auto" data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalRiderReview {
    @Input() name;
    @Input() riderName;
    @Input() orderDate;
    rate = 0;
    max: Number = 5;

    constructor(public activeModal: NgbActiveModal) {
    }
}

@Component({
    selector: 'app-modal-order-details',
    template: `
        <div class="modal-header">
            <h5 class="modal-title text-center">{{name}}</h5>
        </div>
        <div class="modal-body">
            <h6 class="mb-0">
                <fa-icon [icon]="infoIcon"></fa-icon>
                <span style="margin-right: 8px; margin-left: 9px">Status:</span> <b>Delivered</b></h6>
            <h6 class="mb-0">
                <fa-icon [icon]="motorcycleIcon"></fa-icon>
                <span style="margin-right: 15px; margin-left: 5px">Rider:</span> <b>{{riderName}} Gonçalves</b></h6>

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
                    <tr>
                        <td>
                            Hammer
                        </td>
                        <td>
                            5
                        </td>
                        <td>
                            5€
                        </td>
                        <td>
                            25€
                        </td>
                    </tr>
                    </tbody>
                </table>
                <h6 class="mb-0" style="text-align: right;">Total: <b>25€</b></h6>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-link  ml-auto" data-dismiss="modal" (click)="activeModal.dismiss('Close click')">Close
            </button>
        </div>
    `
})
export class NgbModalOrderDetails {
    @Input() name;
    @Input() riderName;
    @Input() orderDate;
    motorcycleIcon = faMotorcycle;
    infoIcon = faInfoCircle;
    rate = 0;
    max: Number = 5;

    constructor(public activeModal: NgbActiveModal) {
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
                                class="btn btn-info"><fa-icon [icon]="plusIcon"></fa-icon></button>

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
    get f(): any { return this.newAddressForm.controls; }
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

    constructor(private modalService: NgbModal) {
    }

    openRiderReview() {
        const modalRef = this.modalService.open(NgbModalRiderReview, {centered: true});
        modalRef.componentInstance.name = 'Rider Review';
        modalRef.componentInstance.riderName = 'João';
        modalRef.componentInstance.orderDate = '30-05-2021 15:00PM';
    }

    openOrderDetails() {
        const modalRef = this.modalService.open(NgbModalOrderDetails, {centered: true, scrollable: true});
        modalRef.componentInstance.name = 'Order Details';
        modalRef.componentInstance.riderName = 'João';
        modalRef.componentInstance.orderDate = '30-05-2021 15:00PM';
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

    ngOnInit() {
    }

}
