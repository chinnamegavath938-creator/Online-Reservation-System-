package com.reservation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

final class ReservationFrame extends JFrame {
    private static final Map<String, String> FLIGHTS = Map.of(
            "AI101", "Air India · Delhi to Mumbai",
            "AI202", "Air India · Mumbai to Bengaluru",
            "6E305", "IndiGo · Delhi to Goa",
            "6E412", "IndiGo · Bengaluru to Kolkata",
            "UK811", "Vistara · Delhi to Chennai");
    private static final String[] CLASSES = {"Economy", "Premium Economy", "Business", "First Class"};
    private final String username;
    private final boolean admin;

    private final JTextField passengerField = new JTextField(22);
    private final JTextField mobileField = new JTextField(22);
    private final JTextField flightNumberField = new JTextField(22);
    private final JTextField flightNameField = new JTextField(22);
    private final JComboBox<String> classField = new JComboBox<>(CLASSES);
    private final JTextField dateField = new JTextField(22);
    private final JTextField sourceField = new JTextField(22);
    private final JTextField destinationField = new JTextField(22);
    private final JTextField pnrField = new JTextField(22);
    private final JLabel fetchedDetails = new JLabel("Enter a PNR number and select Fetch.");
    private final JLabel seatsAvailable = new JLabel("Enter a flight and journey date to see seats available.");
    private Reservation fetchedReservation;

    ReservationFrame(String username, boolean admin) {
        super(admin ? "Flight Reservation | Admin" : "Flight Reservation | Customer");
        this.username=username;this.admin=admin;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(790, 650);
        setMinimumSize(new java.awt.Dimension(720, 590));
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 24, 22, 24));
        JLabel header = new JLabel("Flight reservations  |  Signed in as " + username);
        header.setFont(header.getFont().deriveFont(19f));
        root.add(header, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        if(admin){tabs.addTab("Admin report", createAdminPanel());}
        else{tabs.addTab("Book a flight", createBookingPanel());tabs.addTab("My bookings", createCancellationPanel());}
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        GridBagConstraints c = constraints();
        addRow(panel, c, 0, "Passenger name", passengerField);
        addRow(panel, c, 1, "Mobile number (Indian 10-digit or international)", mobileField);
        addRow(panel, c, 2, "Flight number", flightNumberField);
        addRow(panel, c, 3, "Flight details", flightNameField);
        flightNameField.setEditable(false);
        addRow(panel, c, 4, "Class type", classField);
        addRow(panel, c, 5, "Journey date (YYYY-MM-DD)", dateField);
        addRow(panel, c, 6, "Source airport", sourceField);
        addRow(panel, c, 7, "Destination airport", destinationField);
        c.gridx=0; c.gridy=8; c.gridwidth=2; c.insets=new Insets(3,8,8,8); panel.add(seatsAvailable,c);
        flightNumberField.getDocument().addDocumentListener(listener(this::updateFlightName));
        dateField.getDocument().addDocumentListener(listener(this::updateSeatsAvailable));
        flightNumberField.getDocument().addDocumentListener(listener(this::updateSeatsAvailable));
        classField.addActionListener(event -> updateSeatsAvailable());
        JButton bookButton = new JButton("Book flight");
        bookButton.addActionListener(event -> bookReservation());
        c.gridx=1; c.gridy=9; c.gridwidth=1; c.anchor=GridBagConstraints.EAST; c.insets=new Insets(8,8,8,8); panel.add(bookButton,c);
        return panel;
    }

    private JPanel createCancellationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));
        GridBagConstraints c = constraints();
        addRow(panel, c, 0, "PNR number", pnrField);
        JButton fetchButton = new JButton("Fetch booking");
        fetchButton.addActionListener(event -> fetchReservation());
        c.gridx=1; c.gridy=1; panel.add(fetchButton,c);
        fetchedDetails.setVerticalAlignment(JLabel.TOP);
        fetchedDetails.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(14,14,14,14)));
        c.gridx=0; c.gridy=2; c.gridwidth=2; c.weightx=1; c.weighty=1; panel.add(fetchedDetails,c);
        JButton cancelButton=new JButton("Confirm cancellation"); cancelButton.addActionListener(event->cancelReservation());
        c.gridx=1;c.gridy=3;c.gridwidth=1;c.weighty=0;panel.add(cancelButton,c);
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel=new JPanel(new BorderLayout(0,14));
        panel.setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        JLabel explanation=new JLabel("Export passenger bookings, assigned seats, and remaining seats by flight, date, and class.");
        explanation.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        panel.add(explanation,BorderLayout.NORTH);
        JButton export=new JButton("Export Excel report (.xlsx)");
        export.addActionListener(event->exportReport());
        JPanel center=new JPanel(new GridBagLayout());center.add(export);panel.add(center,BorderLayout.CENTER);
        JLabel note=new JLabel("Workbook includes passengers, assigned seats, and remaining seats. Demo capacity: 45 per flight, date, and class.");
        panel.add(note,BorderLayout.SOUTH);
        return panel;
    }

    private GridBagConstraints constraints(){GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(6,8,6,8);c.anchor=GridBagConstraints.WEST;c.fill=GridBagConstraints.HORIZONTAL;return c;}
    private void addRow(JPanel panel,GridBagConstraints c,int row,String label,java.awt.Component field){c.gridx=0;c.gridy=row;c.weightx=0;panel.add(new JLabel(label),c);c.gridx=1;c.weightx=1;panel.add(field,c);}
    private DocumentListener listener(Runnable action){return new DocumentListener(){public void insertUpdate(DocumentEvent e){action.run();}public void removeUpdate(DocumentEvent e){action.run();}public void changedUpdate(DocumentEvent e){action.run();}};}

    private void updateFlightName(){flightNameField.setText(FLIGHTS.getOrDefault(flightNumberField.getText().trim().toUpperCase(),""));}
    private void updateSeatsAvailable(){
        String flight=flightNumberField.getText().trim().toUpperCase(),date=dateField.getText().trim();
        if(!FLIGHTS.containsKey(flight)||!date.matches("\\d{4}-\\d{2}-\\d{2}")){seatsAvailable.setText("Enter a valid flight number and journey date to see seats available.");return;}
        try{int left=ReservationService.seatsRemaining(flight,date,(String)classField.getSelectedItem());seatsAvailable.setText("Seats available in this class: "+left+" / "+ReservationService.SEATS_PER_FLIGHT_CLASS);}catch(Exception ex){seatsAvailable.setText("Seat availability is unavailable.");}
    }

    private void bookReservation(){
        String passenger=passengerField.getText().trim(),mobile=normalizeMobile(mobileField.getText()),flight=flightNumberField.getText().trim().toUpperCase(),journeyDate=dateField.getText().trim(),source=sourceField.getText().trim(),destination=destinationField.getText().trim();
        if(passenger.isEmpty()||mobileField.getText().isBlank()||flight.isEmpty()||journeyDate.isEmpty()||source.isEmpty()||destination.isEmpty()){showMessage("Complete all required fields.","Missing information",JOptionPane.WARNING_MESSAGE);return;}
        if(mobile==null){showMessage("Enter a valid 10-digit Indian mobile number, or an international number with + and its country code. Spaces and dashes are okay.","Invalid mobile number",JOptionPane.WARNING_MESSAGE);return;}
        if(!FLIGHTS.containsKey(flight)){showMessage("Choose a flight number from the available flight list.","Invalid flight number",JOptionPane.WARNING_MESSAGE);return;}
        if(source.equalsIgnoreCase(destination)){showMessage("Source and destination airports must be different.","Invalid route",JOptionPane.WARNING_MESSAGE);return;}
        try{
            LocalDate parsed=LocalDate.parse(journeyDate);if(parsed.isBefore(LocalDate.now())){showMessage("Journey date cannot be in the past.","Invalid journey date",JOptionPane.WARNING_MESSAGE);return;}
            Reservation request=new Reservation(null,username,passenger,mobile,flight,FLIGHTS.get(flight),(String)classField.getSelectedItem(),parsed.toString(),source,destination,null);
            Reservation saved=ReservationService.book(request);
            String smsStatus;
            try{smsStatus=TwilioSmsService.sendBookingConfirmation(saved);}catch(Exception smsError){smsStatus="Booking saved, but SMS sending failed: "+smsError.getMessage();}
            JOptionPane.showMessageDialog(this,"Flight booked!\n\n"+saved.details()+"\n\n"+smsStatus,"Booking result",JOptionPane.INFORMATION_MESSAGE);
            passengerField.setText("");mobileField.setText("");flightNumberField.setText("");flightNameField.setText("");dateField.setText("");sourceField.setText("");destinationField.setText("");updateSeatsAvailable();
        }catch(DateTimeParseException ex){showMessage("Enter the journey date in YYYY-MM-DD format.","Invalid date",JOptionPane.WARNING_MESSAGE);}catch(Exception ex){showError("Could not save the booking",ex);}
    }

    private String normalizeMobile(String value){
        String normalized=value.trim().replaceAll("[\\s().-]","");
        if(normalized.matches("[6-9]\\d{9}"))return "+91"+normalized;
        if(normalized.matches("0[6-9]\\d{9}"))return "+91"+normalized.substring(1);
        if(normalized.matches("91[6-9]\\d{9}"))return "+"+normalized;
        if(normalized.matches("0091[6-9]\\d{9}"))return "+"+normalized.substring(2);
        return normalized.matches("\\+[1-9]\\d{7,14}")?normalized:null;
    }

    private void fetchReservation(){String pnr=pnrField.getText().trim();if(pnr.isEmpty()){showMessage("Enter a PNR number first.","Missing PNR",JOptionPane.WARNING_MESSAGE);return;}try{fetchedReservation=ReservationService.find(pnr,username,admin);if(fetchedReservation==null){fetchedDetails.setText("No active flight booking was found for this account and PNR.");}else{fetchedDetails.setText("<html>"+fetchedReservation.details().replace("\n","<br>")+"</html>");}}catch(Exception ex){showError("Could not fetch the booking",ex);}}

    private void cancelReservation(){if(fetchedReservation==null){showMessage("Fetch an active booking before cancelling.","No booking selected",JOptionPane.WARNING_MESSAGE);return;}int answer=JOptionPane.showConfirmDialog(this,"Are you sure you want to cancel this flight?\n\n"+fetchedReservation.details(),"Confirm cancellation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(answer!=JOptionPane.YES_OPTION)return;try{ReservationService.cancel(fetchedReservation.pnr(),username,admin);fetchedReservation=null;fetchedDetails.setText("The flight booking has been cancelled.");pnrField.setText("");showMessage("The reservation has been cancelled.","Cancellation complete",JOptionPane.INFORMATION_MESSAGE);}catch(Exception ex){showError("Could not cancel the booking",ex);}}

    private void exportReport(){
        JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Save flight bookings and availability report");chooser.setSelectedFile(new java.io.File("flight-reservations.xlsx"));chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)","xlsx"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        java.io.File file=chooser.getSelectedFile();if(!file.getName().toLowerCase().endsWith(".xlsx"))file=new java.io.File(file.getParentFile(),file.getName()+".xlsx");
        try {
            List<Reservation> bookings=ReservationService.all();
            java.util.Set<String> dates=new java.util.TreeSet<>();
            for(Reservation booking:bookings)dates.add(booking.journeyDate());
            if(dates.isEmpty())dates.add(LocalDate.now().toString());
            List<List<String>> rows=new java.util.ArrayList<>();
            rows.add(List.of("Active Flight Bookings"));
            rows.add(List.of("PNR","Passenger","Mobile","Flight Number","Flight","Class","Date","Seat","From","To"));
            for(Reservation r:bookings)rows.add(List.of(r.pnr(),r.passengerName(),r.mobileNumber(),r.flightNumber(),r.flightName(),r.classType(),r.journeyDate(),r.seatNumber(),r.sourceStation(),r.destinationStation()));
            rows.add(List.of(""));rows.add(List.of("Seat Availability"));
            rows.add(List.of("Flight Number","Flight","Date","Class","Capacity","Booked","Available"));
            for(String date:dates)for(Map.Entry<String,String> flight:FLIGHTS.entrySet())for(String classType:CLASSES){
                int remaining=ReservationService.seatsRemaining(flight.getKey(),date,classType);
                rows.add(List.of(flight.getKey(),flight.getValue(),date,classType,
                        String.valueOf(ReservationService.SEATS_PER_FLIGHT_CLASS),
                        String.valueOf(ReservationService.SEATS_PER_FLIGHT_CLASS-remaining),String.valueOf(remaining)));
            }
            ExcelReportWriter.write(file.toPath(),rows);
            showMessage("Report saved to:\n"+file.getAbsolutePath(),"Export complete",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){showError("Could not export report",ex);}
    }

    private void showMessage(String message,String title,int type){JOptionPane.showMessageDialog(this,message,title,type);}
    private void showError(String title,Exception exception){showMessage(title+":\n"+exception.getMessage(),title,JOptionPane.ERROR_MESSAGE);}
}
