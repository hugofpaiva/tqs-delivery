import 'package:http/http.dart' as http;
import 'dart:convert';

class GenericService {
  static const BASE_URL = "http://127.0.0.1:8080";
  static bool loggedIn = false;
  static bool error = false;
  static String name = '';
  static String token = '';

  static Future<void> login(String email, String password) async {
    var response = await http.post(Uri.parse(BASE_URL + "/login"),  headers: {"Content-Type": "application/json"},
        body: jsonEncode({"username": email, "password": password}));

    if (response.statusCode == 200) {
      error = false;
      loggedIn = true;
      var data = json.decode(response.body);

      if (data['type']['authority'] != 'Rider'){
        error = true;
        loggedIn = false;
        return;
      }

      token = data['token'];
      name = data['name'];
    } else{
      error = true;
    }
  }

  static logout() async {
    loggedIn = false;
    token = '';
    name = '';
  }
}