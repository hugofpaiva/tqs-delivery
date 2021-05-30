import {Component, Input, OnInit} from '@angular/core';
import {faArrowDown, faStar} from '@fortawesome/free-solid-svg-icons';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

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
export class NgbModalOrderDetails {
    @Input() name;
    @Input() riderName;
    @Input() orderDate;
    rate = 0;
    max: Number = 5;

    constructor(public activeModal: NgbActiveModal) {
    }
}

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})

export class ProfileComponent implements OnInit {
    arrowIcon = faArrowDown;
    starIcon = faStar;

    constructor(private modalService: NgbModal) {
    }

    openRiderReview() {
        const modalRef = this.modalService.open(NgbModalRiderReview, { centered: true });
        modalRef.componentInstance.name = 'Rider Review';
        modalRef.componentInstance.riderName = 'João';
        modalRef.componentInstance.orderDate = '30-05-2021 15:00PM';
    }

    ngOnInit() {
    }

}
