package com.reservation;

record Reservation(
        String pnr,
        String ownerUsername,
        String passengerName,
        String mobileNumber,
        String flightNumber,
        String flightName,
        String classType,
        String journeyDate,
        String sourceStation,
        String destinationStation,
        String seatNumber) {

    String details() {
        return "PNR: " + pnr
                + "\nPassenger: " + passengerName
                + "\nMobile: " + mobileNumber
                + "\nFlight: " + flightNumber + " - " + flightName
                + "\nClass: " + classType
                + "\nSeat: " + seatNumber
                + "\nDate of journey: " + journeyDate
                + "\nFrom: " + sourceStation
                + "\nTo: " + destinationStation;
    }
}
