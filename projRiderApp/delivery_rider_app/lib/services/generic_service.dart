import 'package:delivery_rider_app/models/address.dart';
import 'package:delivery_rider_app/models/order.dart';
import 'package:delivery_rider_app/models/store.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class GenericService {
  static const BASE_URL = "http://127.0.0.1:8080";
  static bool loggedIn = false;
  static bool error = false;
  static String name = '';
  static String token = '';
  static bool requested = false;

  static Future<void> login(String email, String password) async {
    error = false;
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
        return;
      }

      token = "Bearer " + data['token'];
      name = data['name'];
    } else {
      error = true;
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
}
