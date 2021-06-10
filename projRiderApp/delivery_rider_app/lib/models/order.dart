import 'package:delivery_rider_app/models/store.dart';

import 'address.dart';

class Order {
  int id;
  String clientName;
  DateTime date;
  String status;
  Address clientAddress;
  Store store;
  int? riderReview;


  Order(this.id, this.clientName, this.date, this.clientAddress, this.store, this.status, this.riderReview);
}