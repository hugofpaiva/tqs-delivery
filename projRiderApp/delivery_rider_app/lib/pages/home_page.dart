import 'package:delivery_rider_app/pages/login_page.dart';
import 'package:delivery_rider_app/services/generic_service.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:url_launcher/url_launcher.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: scaffoldKey,
      backgroundColor: Color(0xFF1A1F4A),
      body: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.fromLTRB(0, 0, 0, 1),
              child: Container(
                height: 100,
                decoration: BoxDecoration(),
                child: Padding(
                  padding: EdgeInsets.fromLTRB(35, 0, 0, 0),
                  child: Row(
                    mainAxisSize: MainAxisSize.max,
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Padding(
                        padding: EdgeInsets.fromLTRB(0, 0, 15, 0),
                        child: Container(
                          width: 70,
                          height: 70,
                          clipBehavior: Clip.antiAlias,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                          ),
                          child: Image.asset(
                            'assets/images/user.jpeg',
                          ),
                        ),
                      ),
                      Padding(
                        padding: EdgeInsets.fromLTRB(0, 0, 0, 0),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '${GenericService.name},',
                              style: GoogleFonts.nunito(
                                  fontWeight: FontWeight.w600,
                                  color: Colors.white,
                                  fontSize: 18),
                              textAlign: TextAlign.start,
                            ),
                            Text(
                              'Welcome Back!',
                              style: GoogleFonts.nunito(
                                  fontWeight: FontWeight.w300,
                                  color: Colors.white),
                              textAlign: TextAlign.start,
                            ),
                          ],
                        ),
                      ),
                      Spacer(),
                      Padding(
                        padding: EdgeInsets.fromLTRB(0, 0, 35, 0),
                        child: IconButton(
                          onPressed: () async {
                            GenericService.loggedIn = false;
                            await Navigator.pushAndRemoveUntil(
                              context,
                              MaterialPageRoute(
                                builder: (context) => LoginPage(),
                              ),
                              (r) => false,
                            );
                          },
                          icon: Icon(
                            Icons.login_outlined,
                            color: Colors.white,
                            size: 25,
                          ),
                          iconSize: 25,
                        ),
                      )
                    ],
                  ),
                ),
              ),
            ),
            Expanded(
              child: Container(
                width: MediaQuery.of(context).size.width,
                height: MediaQuery.of(context).size.height * 0.8,
                decoration: BoxDecoration(
                  color: Color(0xFFEEEEEE),
                  borderRadius: BorderRadius.only(
                    bottomLeft: Radius.circular(0),
                    bottomRight: Radius.circular(0),
                    topLeft: Radius.circular(35),
                    topRight: Radius.circular(35),
                  ),
                ),
                child: Padding(
                  padding: EdgeInsets.fromLTRB(20, 20, 20, 20),
                  child: Column(
                    mainAxisSize: MainAxisSize.max,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisSize: MainAxisSize.max,
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Current Order',
                            style: GoogleFonts.oswald(
                                fontWeight: FontWeight.w300, fontSize: 22),
                          ),
                          Text(
                            '#126',
                            style: GoogleFonts.lato(
                                fontWeight: FontWeight.w300, fontSize: 16),
                          )
                        ],
                      ),
                      Divider(
                        height: 10,
                        thickness: 1,
                        color: Color(0xFFA5A3A3),
                      ),
                      Spacer(),
                      Row(
                        mainAxisSize: MainAxisSize.max,
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Padding(
                            padding: EdgeInsets.fromLTRB(35, 0, 0, 0),
                            child: Icon(
                              Icons.place,
                              color: Colors.black,
                              size: 28,
                            ),
                          ),
                          Text(
                            'Next Destination',
                            style: GoogleFonts.nunito(
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          )
                        ],
                      ),
                      Spacer(),
                      Row(
                        mainAxisSize: MainAxisSize.max,
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Matsuri Sushi',
                            style: GoogleFonts.nunito(
                                fontWeight: FontWeight.w300, fontSize: 18),
                          ),
                          Text(
                            'Rua asdasd',
                            style: GoogleFonts.nunito(
                                fontWeight: FontWeight.w300, fontSize: 12),
                          )
                        ],
                      ),
                      Spacer(),
                      InkWell(
                        onTap: () async {
                          navigateTo(49.46800006494457, 17.11514008755796);
                        },
                        child: Image.asset(
                          'assets/images/map.jpeg',
                          width: MediaQuery.of(context).size.width,
                          height: 200,
                          fit: BoxFit.cover,
                        ),
                      ),
                      Spacer(flex: 3),
                      Row(
                        mainAxisSize: MainAxisSize.max,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          ElevatedButton(
                            onPressed: () {
                              print('Button pressed ...');
                            },
                            child: Text(
                              'Confirm Pickup',
                              style: GoogleFonts.lato(
                                  fontWeight: FontWeight.w300,
                                  color: Colors.white),
                            ),
                            style: ElevatedButton.styleFrom(
                                primary: Theme.of(context).primaryColor,
                                onPrimary: Colors.white),
                          ),
                        ],
                      ),
                      Spacer(flex: 2)
                    ],
                  ),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  static void navigateTo(double lat, double lng) async {
    var uri = Uri.parse("google.navigation:q=$lat,$lng&mode=d");
    if (await canLaunch(uri.toString())) {
      await launch(uri.toString());
    } else {
      throw 'Could not launch ${uri.toString()}';
    }
  }
}
