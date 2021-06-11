import 'package:delivery_rider_app/models/order.dart';
import 'package:delivery_rider_app/services/generic_service.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';

class OrdersHistoryPage extends StatefulWidget {
  @override
  _OrdersHistoryPageState createState() => _OrdersHistoryPageState();
}

class _OrdersHistoryPageState extends State<OrdersHistoryPage> {
  final scaffoldKey = GlobalKey<ScaffoldState>();
  late Future<List<Order>> futureData;

  @override
  void initState() {
    super.initState();
    futureData = GenericService.getLastOrders(100);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        key: scaffoldKey,
        body: SafeArea(
            child: Column(
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
            ),
            FutureBuilder<List<Order>>(
              future: futureData,
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  List<Order>? data = snapshot.data;
                  return ListView.builder(
                      padding: EdgeInsets.zero,
                      scrollDirection: Axis.vertical,
                      shrinkWrap: true,
                      itemCount: data!.length,
                      itemBuilder: (BuildContext context, int index) {
                        return ListTile(
                          leading: FaIcon(
                            FontAwesomeIcons.stickyNote,
                            color: Color(0xFFEF9039),
                          ),
                          title: Text(
                            '#${data[index].id}  ${data[index].clientName}',
                            style: TextStyle(
                              fontFamily: 'Oswald',
                            ),
                          ),
                          subtitle: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                '${data[index].store.name} \n${DateFormat('yyyy-MM-dd kk:mm').format(data[index].date)}',
                                style: TextStyle(
                                  fontFamily: 'Lato',
                                ),
                              ),
                              Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Text(
                                    '${data[index].status}️',
                                    style: TextStyle(
                                      fontFamily: 'Lato',
                                    ),
                                  ),( data[index].riderReview != null ?
                                  Text(
                                    '⭐️️'*data[index].riderReview!,
                                    style: TextStyle(
                                      fontFamily: 'Lato',
                                      fontSize: 22,
                                      color: Colors.amberAccent
                                    ),
                                  ) : Container(height: 30)),
                                ],
                              ),
                            ],
                          ),
                          tileColor: Color(0xFFF5F5F5),
                          dense: false,
                        );
                      });
                } else if (snapshot.hasError && GenericService.error) {
                  return Text('There was an error',
                      style: GoogleFonts.oswald(
                          fontWeight: FontWeight.w300, fontSize: 20,
                          color: Colors.redAccent));
                }
                // By default show a loading spinner.
                return Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    CircularProgressIndicator(),
                  ],
                );
              },
            ),
          ],
        )));
  }
}
