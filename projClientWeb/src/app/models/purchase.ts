import {Address} from './address';
import {Product} from './product';

export class Purchase {
    id: number;
    date: Date;
    address: Address;
    products: Product[];
    riderReview: Number;
    riderName: String;
    status: string;
}
