package com.reservation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class RoleSelectionFrame extends JFrame {
    RoleSelectionFrame(){
        super("Flight Reservation | Customer home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setSize(520,340);setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout(0,22));root.setBorder(BorderFactory.createEmptyBorder(30,34,30,34));
        JLabel title=new JLabel("Welcome to Flight Reservation");title.setFont(title.getFont().deriveFont(23f));title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel subtitle=new JLabel("Search flights and manage your customer bookings.");JPanel heading=new JPanel(new GridLayout(0,1,0,8));heading.add(title);heading.add(subtitle);root.add(heading,BorderLayout.NORTH);
        JButton customer=new JButton("<html><center><b>Customer sign in</b><br>Book and manage your flights</center></html>");
        customer.addActionListener(event->{dispose();new LoginFrame("CUSTOMER").setVisible(true);});
        JPanel choices=new JPanel(new GridLayout(1,1));choices.add(customer);root.add(choices,BorderLayout.CENTER);
        JLabel demo=new JLabel("Customer demo: customer / customer123");demo.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));root.add(demo,BorderLayout.SOUTH);
        MouseAdapter hiddenAdminAccess=new MouseAdapter(){@Override public void mouseClicked(MouseEvent event){if(event.getClickCount()==2){dispose();new LoginFrame("ADMIN").setVisible(true);}}};
        title.addMouseListener(hiddenAdminAccess);heading.addMouseListener(hiddenAdminAccess);
        setContentPane(root);
    }
}
