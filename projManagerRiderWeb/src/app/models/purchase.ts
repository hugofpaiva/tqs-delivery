import {Address} from './address';
import {Store} from './store';

export class Purchase {
  id: number;
  date: Date;
  address: Address;
  status: string;
  store: Store;
  clientName: String;
  deliveryTime: Number;
  riderReview: Number;
}
