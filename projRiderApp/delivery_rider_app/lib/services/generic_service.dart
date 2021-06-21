import 'package:delivery_rider_app/models/address.dart';
import 'package:delivery_rider_app/models/order.dart';
import 'package:delivery_rider_app/models/store.dart';
import 'package:http/http.dart' as http;
import 'package:geolocator/geolocator.dart';
import 'dart:convert';

class GenericService {
  static const BASE_URL = "http://127.0.0.1:8081";
  static bool loggedIn = false;
  static bool error = false;
  static String errorMsg = '';
  static String name = '';
  static String token = '';
  static bool requested = false;
  static Order? actualOrder;

  static Future<void> login(String email, String password) async {
    error = false;
    errorMsg = '';
    requested = true;
    var response = await http.post(Uri.parse(BASE_URL + "/login"),
        headers: {"Content-Type": "application/json"},
        body: jsonEncode({"username": email, "password": password}));

    if (response.statusCode == 200) {
      loggedIn = true;
      var data = json.decode(utf8.decode(response.bodyBytes));

      if (data['type']['authority'] != 'Rider') {
        error = true;
        loggedIn = false;
        errorMsg = 'Invalid Permissions';
        return;
      }

      token = "Bearer " + data['token'];
      name = data['name'];
    } else {
      error = true;
      errorMsg = 'Invalid Credentials';
    }
    requested = false;
  }

  static logout() async {
    loggedIn = false;
    error = false;
    token = '';
    name = '';
  }

  static Future<List<Order>> getLastOrders(int pageSize) async {
    error = false;
    errorMsg = '';
    requested = true;
    var response = await http.get(Uri.parse(BASE_URL + "/rider/orders"),
        headers: {"Content-Type": "application/json", "Authorization": token});

    List<Order> responseOrders = [];

    if (response.statusCode == 200) {
      var data = json.decode(utf8.decode(response.bodyBytes));
      for (var order in data['orders']) {
        Address storeAddress = Address(
            order['store']['address']['address'],
            order['store']['address']['country'],
            order['store']['address']['city'],
            order['store']['address']['postalCode']);
        Store store =
            Store(order['store']['id'], order['store']['name'], storeAddress);
        Address clientAddress = Address(
            order['address']['address'],
            order['address']['country'],
            order['address']['city'],
            order['address']['postalCode']);
        Order newOrder = Order(order['id'], order['clientName'], new DateTime.fromMillisecondsSinceEpoch(order['date']),
            clientAddress, store, order['status'], order['riderReview']);

        responseOrders.add(newOrder);
      }
    } else {
      error = true;
    }

    requested = false;

    return responseOrders;
  }


  static Future<void> checkOrder() async {
    error = false;
    errorMsg = '';
    requested = true;
    var response = await http.get(Uri.parse(BASE_URL + "/rider/order/current"),
        headers: {"Content-Type": "application/json", "Authorization": token});

    if (response.statusCode == 200) {
        var data = json.decode(utf8.decode(response.bodyBytes));
        data = data['data'];
        Address storeAddress = Address(
            data['store']['address']['address'],
            data['store']['address']['country'],
            data['store']['address']['city'],
            data['store']['address']['postalCode']);
        Store store =
        Store(data['store']['id'], data['store']['name'], storeAddress);
        Address clientAddress = Address(
            data['clientAddress']['address'],
            data['clientAddress']['country'],
            data['clientAddress']['city'],
            data['clientAddress']['postalCode']);

        actualOrder = Order(data['orderId'], data['clientName'], new DateTime.fromMillisecondsSinceEpoch(data['date']),
            clientAddress, store, data['status'], null);

    } else if (response.statusCode == 404) {
      error = true;
      actualOrder = null;
    } else {
      error = true;
    }

    requested = false;

  }

  static Future<void> getNewOrder() async {
    error = false;
    errorMsg = '';
    requested = true;
    var response = await http.get(Uri.parse(BASE_URL + "/rider/order/new"),
        headers: {"Content-Type": "application/json", "Authorization": token});

    if (response.statusCode == 200) {
      var data = json.decode(utf8.decode(response.bodyBytes));
      data = data['data'];
      Address storeAddress = Address(
          data['store']['address']['address'],
          data['store']['address']['country'],
          data['store']['address']['city'],
          data['store']['address']['postalCode']);
      Store store =
      Store(data['store']['id'], data['store']['name'], storeAddress);
      Address clientAddress = Address(
          data['clientAddress']['address'],
          data['clientAddress']['country'],
          data['clientAddress']['city'],
          data['clientAddress']['postalCode']);

      actualOrder = Order(data['orderId'], data['clientName'], new DateTime.fromMillisecondsSinceEpoch(data['date']),
          clientAddress, store, data['status'], null);

    } else if (response.statusCode == 404) {
      error = true;
      errorMsg = 'No Orders Available';
      actualOrder = null;
    } else {
      error = true;
    }

    requested = false;

  }


  static Future<void> nextStatusOrder() async {
    error = false;
    errorMsg = '';
    requested = true;
    var response = await http.patch(Uri.parse(BASE_URL + "/rider/order/status"),
        headers: {"Content-Type": "application/json", "Authorization": token});

    if (response.statusCode == 200) {
      var data = json.decode(utf8.decode(response.bodyBytes));

      if(actualOrder!.id != data['order_id']){
        error = true;
        requested = false;
        checkOrder();
        return;
      }

      actualOrder!.status = data['status'];

      if (actualOrder!.status == 'DELIVERED'){
        actualOrder = null;
      }

    } else if (response.statusCode == 404) {
      error = true;
      actualOrder = null;
      errorMsg = 'Order no longer existed';
    } else {
      error = true;
    }

    requested = false;

  }

}
