import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';

class OrdersHistoryPage extends StatefulWidget {
  @override
  _OrdersHistoryPageState createState() => _OrdersHistoryPageState();
}

class _OrdersHistoryPageState extends State<OrdersHistoryPage> {
  final scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: scaffoldKey,
      body: SafeArea(
        child: ListView(
          padding: EdgeInsets.zero,
          scrollDirection: Axis.vertical,
          children: [
            Row(
              mainAxisSize: MainAxisSize.max,
              mainAxisAlignment: MainAxisAlignment.start,
              children: [
                Padding(
                  padding: EdgeInsets.fromLTRB(25, 0, 0, 25),
                  child: Text(
                    'Orders History',
                    style: GoogleFonts.oswald(
                        fontWeight: FontWeight.w300,
                        fontSize: 26,
                        color: Theme.of(context).primaryColor),
                  ),
                )
              ],
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            ),
            ListTile(
              leading: FaIcon(
                FontAwesomeIcons.stickyNote,
                color: Color(0xFFEF9039),
              ),
              title: Text(
                '#1  Client Name',
                style: TextStyle(
                  fontFamily: 'Oswald',
                ),
              ),
              subtitle: Text(
                'Store name \n2021-05-25 12:24:24',
                style: TextStyle(
                  fontFamily: 'Lato',
                ),
              ),
              tileColor: Color(0xFFF5F5F5),
              dense: false,
            )
          ],
        ),
      ),
    );
  }
}
