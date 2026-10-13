package com.reservation;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

final class TwilioSmsService {
    private TwilioSmsService() { }

    static String sendBookingConfirmation(Reservation reservation) throws Exception {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");
        if (blank(accountSid) || blank(authToken) || blank(fromNumber)) {
            return "SMS not sent: configure TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER.";
        }

        String endpoint = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String body = "FLIGHT TICKET CONFIRMED\n"
                + "Passenger: " + reservation.passengerName() + "\n"
                + "PNR: " + reservation.pnr() + "\n"
                + "Mobile: " + reservation.mobileNumber() + "\n"
                + "Flight: " + reservation.flightNumber() + " - " + reservation.flightName() + "\n"
                + "Class: " + reservation.classType() + "\n"
                + "Seat: " + reservation.seatNumber() + "\n"
                + "Date: " + reservation.journeyDate() + "\n"
                + "From: " + reservation.sourceStation() + "\n"
                + "To: " + reservation.destinationStation();
        String form = "To=" + encode(reservation.mobileNumber()) + "&From=" + encode(fromNumber)
                + "&Body=" + encode(body);
        String auth = Base64.getEncoder().encodeToString((accountSid + ":" + authToken)
                .getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) return "Twilio accepted the full ticket confirmation for " + reservation.mobileNumber() + ".";
        return "Booking saved, but Twilio could not send the SMS (HTTP " + response.statusCode() + ").";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
