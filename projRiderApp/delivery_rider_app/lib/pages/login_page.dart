import 'package:delivery_rider_app/services/generic_service.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';

import 'nav_layout.dart';

class LoginPage extends StatefulWidget {
  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  TextEditingController userNameController = TextEditingController();
  TextEditingController pwController = TextEditingController();
  final scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  void initState() {
    super.initState();
    GenericService.error = false;
    userNameController = TextEditingController();
    pwController = TextEditingController();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: scaffoldKey,
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height * 1,
          child: Column(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Spacer(flex: 5),
              Image.asset(
                'assets/images/logo.png',
                width: 100,
                height: 100,
                fit: BoxFit.cover,
              ),
              Spacer(),
              Text('Sign in to your account',
                  style: GoogleFonts.oswald(
                      fontWeight: FontWeight.w300, fontSize: 20)),
              Spacer(flex: 3),
              Padding(
                padding: EdgeInsets.fromLTRB(2, 0, 0, 0),
                child: Container(
                  width: MediaQuery.of(context).size.width * 0.7,
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(50),
                    shape: BoxShape.rectangle,
                    border: Border.all(
                      color: Color(0xFFAEAEAE),
                      width: 1,
                    ),
                  ),
                  child: Padding(
                    padding: EdgeInsets.fromLTRB(20, 0, 0, 0),
                    child: Row(
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        FaIcon(
                          FontAwesomeIcons.userAlt,
                          color: Theme.of(context).accentColor,
                          size: 24,
                        ),
                        Expanded(
                          child: TextFormField(
                            controller: userNameController,
                            obscureText: false,
                            decoration: InputDecoration(
                              hintText: 'Email',
                              hintStyle: GoogleFonts.oswald(
                                  fontWeight: FontWeight.w300),
                              enabledBorder: UnderlineInputBorder(
                                borderSide: BorderSide(
                                  color: Colors.transparent,
                                  width: 1,
                                ),
                                borderRadius: const BorderRadius.only(
                                  topLeft: Radius.circular(4.0),
                                  topRight: Radius.circular(4.0),
                                ),
                              ),
                              focusedBorder: UnderlineInputBorder(
                                borderSide: BorderSide(
                                  color: Colors.transparent,
                                  width: 1,
                                ),
                                borderRadius: const BorderRadius.only(
                                  topLeft: Radius.circular(4.0),
                                  topRight: Radius.circular(4.0),
                                ),
                              ),
                            ),
                            style:
                                GoogleFonts.nunito(fontWeight: FontWeight.w300),
                            textAlign: TextAlign.center,
                          ),
                        )
                      ],
                    ),
                  ),
                ),
              ),
              Spacer(),
              Padding(
                padding: EdgeInsets.fromLTRB(2, 0, 0, 0),
                child: Container(
                  width: MediaQuery.of(context).size.width * 0.7,
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(50),
                    shape: BoxShape.rectangle,
                    border: Border.all(
                      color: Color(0xFFAEAEAE),
                      width: 1,
                    ),
                  ),
                  child: Padding(
                    padding: EdgeInsets.fromLTRB(20, 0, 0, 0),
                    child: Row(
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        Icon(
                          Icons.lock,
                          color: Color(0xFFEF9039),
                          size: 24,
                        ),
                        Expanded(
                          child: TextFormField(
                            controller: pwController,
                            obscureText: true,
                            decoration: InputDecoration(
                              hintText: 'Password',
                              hintStyle: GoogleFonts.oswald(
                                  fontWeight: FontWeight.w300),
                              enabledBorder: UnderlineInputBorder(
                                borderSide: BorderSide(
                                  color: Colors.transparent,
                                  width: 1,
                                ),
                                borderRadius: const BorderRadius.only(
                                  topLeft: Radius.circular(4.0),
                                  topRight: Radius.circular(4.0),
                                ),
                              ),
                              focusedBorder: UnderlineInputBorder(
                                borderSide: BorderSide(
                                  color: Colors.transparent,
                                  width: 1,
                                ),
                                borderRadius: const BorderRadius.only(
                                  topLeft: Radius.circular(4.0),
                                  topRight: Radius.circular(4.0),
                                ),
                              ),
                            ),
                            style:
                                GoogleFonts.nunito(fontWeight: FontWeight.w300),
                            textAlign: TextAlign.center,
                          ),
                        )
                      ],
                    ),
                  ),
                ),
              ),
              Align(
                alignment: Alignment(0.8, 0),
                child: Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    color: Colors.white,
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.max,
                    children: [
                      Text('Sign in',
                          style:
                              GoogleFonts.oswald(fontWeight: FontWeight.w300)),
                      IconButton(
                        onPressed: () async {
                          await GenericService.login(
                              userNameController.text, pwController.text);
                          if (GenericService.loggedIn) {
                            await Navigator.pushAndRemoveUntil(
                              context,
                              MaterialPageRoute(
                                builder: (context) => NavLayout(),
                              ),
                              (r) => false,
                            );
                          } else {
                            setState(() {
                            });
                          }
                        },
                        icon: FaIcon(
                          FontAwesomeIcons.arrowCircleRight,
                          color: Color(0xFFEF9039),
                          size: 30,
                        ),
                        iconSize: 30,
                      )
                    ],
                  ),
                ),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  (GenericService.error == true
                      ? Text(GenericService.errorMsg,
                          style: GoogleFonts.oswald(
                              fontWeight: FontWeight.w300, fontSize: 20,
                              color: Colors.redAccent))
                      : Container()),
                  (GenericService.requested == true
                      ? CircularProgressIndicator()
                      : Container())
                ],
              ),
              Spacer(flex: 5)
            ],
          ),
        ),
      ),
    );
  }
}
