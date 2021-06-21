export class Review {
    orderId: number;
    review: number;
    constructor(orderId: number, review: number) {
        this.orderId = orderId;
        this.review = review;
    }
}
