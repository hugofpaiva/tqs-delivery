import 'package:delivery_rider_app/pages/login_page.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        primaryColor: Color(0xFF1A1F4A),
        accentColor: Color(0xFFEF9039)
      ),
      home: LoginPage(),
    );
  }
}