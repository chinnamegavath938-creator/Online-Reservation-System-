# Online Flight Reservation System

A Java Swing flight reservation demo built with JDBC and SQLite, with a standalone browser demo in `index.html`. The SQLite database (`reservation.db`) is created in the directory where the Java app is launched. The first launch creates the tables and an admin demo account.

## Run

Requirements: Java 17+ and Maven.

```sh
mvn compile exec:java
```

For the standalone browser edition, open `index.html` in a browser. It starts with a designed home page, route/date flight search, and popular route cards. Customers can register from the customer sign-in page, then sign in. Admin sign-in is hidden; while signed out, double-click a non-form area of the home page to open it. After admin authentication, the admin dashboard opens. Browser customer accounts and reservations are saved to that browser's local storage. Browser data and the Java app's SQLite database are separate.

Java demo accounts: **customer** / **customer123** and **admin** / **admin123**. In the Java app, admin sign-in is opened by double-clicking the welcome heading on the home screen. Customers can create their own accounts in the Java app, book flights, and manage their own PNRs. Admins can download the report. Indian mobile numbers are accepted as 10 digits, with a leading `0`, or with `91`; these are normalized to `+91`. Other international numbers can be entered with `+` and their country code. Spaces, parentheses, and hyphens are removed before saving and sending.

## Sample flights

| Flight number | Flight |
| --- | --- |
| AI101 | Air India · Delhi to Mumbai |
| AI202 | Air India · Mumbai to Bengaluru |
| 6E305 | IndiGo · Delhi to Goa |
| 6E412 | IndiGo · Bengaluru to Kolkata |
| UK811 | Vistara · Delhi to Chennai |

The booking form fills flight details from this sample list. The demo assigns 45 seats per flight, journey date, and class. Seat numbers are assigned automatically and reopened seats can be reused after cancellation. PNRs are generated from a random UUID. SQL values are passed through JDBC `PreparedStatement`s.

## Features

- Credential check with salted PBKDF2-HMAC-SHA256 password digests in the local SQLite file.
- Flight booking with mobile number, flight lookup, class selection, route, date validation, seat assignment, and availability display.
- Confirmation text message through Twilio when credentials are configured.
- Admin report export containing passenger/seat bookings and available seat counts, as an Excel `.xlsx` workbook. The browser edition downloads an Excel-compatible `.csv`.
- PNR lookup and confirmation prompt before cancelling a booking.

## Twilio SMS setup

To send live booking confirmations from the Java app, configure a Twilio account with an SMS capable sender and enable messaging to the countries where your customers receive texts. From Terminal, run:

```sh
bash start-with-twilio.sh
```

The launcher asks for your Account SID, Auth Token (hidden while typing), and sender number in E.164 format. It keeps the credentials in that process's environment and does not write them to project files. The app sends the passenger name, PNR, mobile number, flight, class, seat, date, and route. A successful API response means Twilio accepted the message request; carrier delivery can still fail. If Twilio rejects the request, the booking remains saved and the result dialog shows the SMS status. Keep credentials private. Twilio account setup, trial recipient verification, destination-country permissions, local sender rules, and account balance determine whether each message can be delivered. Text messages may incur charges. The browser-only `index.html` demo does not send SMS; browser bookings are local to one device and separate from the Java app's SQLite data.
