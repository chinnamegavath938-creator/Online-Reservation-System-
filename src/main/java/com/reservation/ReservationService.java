package com.reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class ReservationService {
    static final int SEATS_PER_FLIGHT_CLASS = 45;

    private ReservationService() { }

    static Reservation book(Reservation reservation) throws SQLException {
        String pnr = "PNR" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase(Locale.ROOT);
        try (Connection connection = Database.connect()) {
            boolean[] occupied = new boolean[SEATS_PER_FLIGHT_CLASS + 1];
            try (PreparedStatement query = connection.prepareStatement("""
                    SELECT seat_number FROM reservations
                    WHERE flight_number = ? AND journey_date = ? AND class_type = ?
                    """)) {
                query.setString(1, reservation.flightNumber());
                query.setString(2, reservation.journeyDate());
                query.setString(3, reservation.classType());
                try (ResultSet result = query.executeQuery()) {
                    while (result.next()) {
                        String bookedSeat = result.getString(1);
                        int dash = bookedSeat.lastIndexOf('-');
                        if (dash >= 0) try {
                            int number = Integer.parseInt(bookedSeat.substring(dash + 1));
                            if (number > 0 && number < occupied.length) occupied[number] = true;
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
            int seatNumber=1;while(seatNumber<occupied.length&&occupied[seatNumber])seatNumber++;
            if (seatNumber >= occupied.length) throw new SQLException("This flight and class are fully booked for that date.");
            String seat = seatPrefix(reservation.classType()) + "-" + String.format(Locale.ROOT, "%03d", seatNumber);
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO reservations
                    (pnr, owner_username, passenger_name, mobile_number, flight_number, flight_name, class_type,
                     journey_date, source_station, destination_station, seat_number)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """)) {
                insert.setString(1, pnr);
                insert.setString(2, reservation.ownerUsername());
                insert.setString(3, reservation.passengerName());
                insert.setString(4, reservation.mobileNumber());
                insert.setString(5, reservation.flightNumber());
                insert.setString(6, reservation.flightName());
                insert.setString(7, reservation.classType());
                insert.setString(8, reservation.journeyDate());
                insert.setString(9, reservation.sourceStation());
                insert.setString(10, reservation.destinationStation());
                insert.setString(11, seat);
                insert.executeUpdate();
            }
            return new Reservation(pnr, reservation.ownerUsername(), reservation.passengerName(), reservation.mobileNumber(),
                    reservation.flightNumber(), reservation.flightName(), reservation.classType(),
                    reservation.journeyDate(), reservation.sourceStation(), reservation.destinationStation(), seat);
        }
    }

    static Reservation find(String pnr, String username, boolean admin) throws SQLException {
        try (Connection connection = Database.connect(); PreparedStatement query = connection.prepareStatement("""
                SELECT pnr, owner_username, passenger_name, mobile_number, flight_number, flight_name, class_type,
                       journey_date, source_station, destination_station, seat_number
                FROM reservations WHERE pnr = ? AND (? = 1 OR owner_username = ?)
                """)) {
            query.setString(1, pnr.trim().toUpperCase(Locale.ROOT));
            query.setInt(2, admin ? 1 : 0);
            query.setString(3, username);
            try (ResultSet result = query.executeQuery()) { return result.next() ? readReservation(result) : null; }
        }
    }

    static List<Reservation> all() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = Database.connect(); PreparedStatement query = connection.prepareStatement("""
                SELECT pnr, owner_username, passenger_name, mobile_number, flight_number, flight_name, class_type,
                       journey_date, source_station, destination_station, seat_number
                FROM reservations ORDER BY journey_date, flight_number, class_type, seat_number
                """)) {
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) reservations.add(readReservation(result));
            }
        }
        return reservations;
    }

    static int seatsRemaining(String flight, String date, String classType) throws SQLException {
        try (Connection connection = Database.connect(); PreparedStatement query = connection.prepareStatement("""
                SELECT COUNT(*) FROM reservations WHERE flight_number=? AND journey_date=? AND class_type=?
                """)) {
            query.setString(1, flight); query.setString(2, date); query.setString(3, classType);
            try (ResultSet result = query.executeQuery()) { return SEATS_PER_FLIGHT_CLASS - result.getInt(1); }
        }
    }

    static void cancel(String pnr, String username, boolean admin) throws SQLException {
        try (Connection connection = Database.connect(); PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM reservations WHERE pnr = ? AND (? = 1 OR owner_username = ?)")) {
            delete.setString(1, pnr.trim().toUpperCase(Locale.ROOT));
            delete.setInt(2, admin ? 1 : 0);
            delete.setString(3, username);
            if (delete.executeUpdate() == 0) throw new SQLException("The reservation no longer exists.");
        }
    }

    private static String seatPrefix(String classType) {
        return switch (classType) {
            case "Economy" -> "E";
            case "Premium Economy" -> "PE";
            case "Business" -> "B";
            case "First Class" -> "F";
            default -> "E";
        };
    }

    private static Reservation readReservation(ResultSet result) throws SQLException {
        return new Reservation(result.getString("pnr"), result.getString("owner_username"), result.getString("passenger_name"),
                result.getString("mobile_number"), result.getString("flight_number"),
                result.getString("flight_name"), result.getString("class_type"),
                result.getString("journey_date"), result.getString("source_station"),
                result.getString("destination_station"), result.getString("seat_number"));
    }
}
