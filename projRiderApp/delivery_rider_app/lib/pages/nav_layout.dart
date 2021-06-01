import 'package:delivery_rider_app/pages/home_page.dart';
import 'package:delivery_rider_app/pages/orders_history_page.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class NavLayout extends StatefulWidget {
  @override
  _NavLayoutState createState() => _NavLayoutState();
}

class _NavLayoutState extends State<NavLayout> {

  int _selectedIndex = 0;
  final pages = [HomePage(), OrdersHistoryPage()];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      bottomNavigationBar: BottomNavigationBar(
        backgroundColor: Theme.of(context).primaryColor,
        unselectedItemColor: Colors.white,
        items: <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(FontAwesomeIcons.home),
            label: 'Home'
          ),
          BottomNavigationBarItem(
            icon: Icon(FontAwesomeIcons.clock),
            label: 'Orders History'
          )
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Theme.of(context).accentColor,
        onTap: _onItemTapped,
      ),
      body: pages[_selectedIndex],
    );
  }
}
