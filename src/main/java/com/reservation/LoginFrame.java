package com.reservation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.util.Arrays;

final class LoginFrame extends JFrame {
    private final String requiredRole;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    LoginFrame(String requiredRole) {
        super(requiredRole.equals("ADMIN") ? "Administrator Sign in" : "Customer Sign in");
        this.requiredRole = requiredRole;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(470, 355);
        setLocationRelativeTo(null);

        JPanel content=new JPanel(new BorderLayout(0,16));
        content.setBorder(BorderFactory.createEmptyBorder(26,32,26,32));
        JLabel heading=new JLabel(requiredRole.equals("ADMIN")?"Admin access":"Customer sign in");
        heading.setFont(heading.getFont().deriveFont(24f));
        JLabel subtitle=new JLabel(requiredRole.equals("ADMIN")?"Manage passenger bookings and seat reports.":"Book flights and manage your own reservations.");
        JPanel title=new JPanel(new BorderLayout(0,6));title.add(heading,BorderLayout.NORTH);title.add(subtitle,BorderLayout.SOUTH);content.add(title,BorderLayout.NORTH);

        JPanel fields=new JPanel(new GridBagLayout());GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(8,5,8,5);c.anchor=GridBagConstraints.WEST;
        c.gridx=0;c.gridy=0;fields.add(new JLabel(requiredRole.equals("ADMIN")?"Admin username":"Customer username"),c);c.gridx=1;fields.add(usernameField,c);
        c.gridx=0;c.gridy=1;fields.add(new JLabel("Password"),c);c.gridx=1;fields.add(passwordField,c);content.add(fields,BorderLayout.CENTER);

        String demoName=requiredRole.equals("ADMIN")?"admin":"customer";
        String demoPassword=requiredRole.equals("ADMIN")?"admin123":"customer123";
        JLabel demo=new JLabel("Demo: "+demoName+" / "+demoPassword);
        JButton back=new JButton("Home");back.addActionListener(event->{dispose();new RoleSelectionFrame().setVisible(true);});
        JButton login=new JButton("Sign in");login.addActionListener(event->signIn());passwordField.addActionListener(event->signIn());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.add(back);
        if(requiredRole.equals("CUSTOMER")){JButton register=new JButton("Create account");register.addActionListener(event->{dispose();new RegistrationFrame().setVisible(true);});actions.add(register);}
        actions.add(login);
        JPanel bottom=new JPanel(new BorderLayout(10,0));bottom.add(demo,BorderLayout.NORTH);bottom.add(actions,BorderLayout.SOUTH);content.add(bottom,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(login);setContentPane(content);
    }

    private void signIn(){
        String username=usernameField.getText().trim();char[] password=passwordField.getPassword();
        if(username.isEmpty()||password.length==0){JOptionPane.showMessageDialog(this,"Enter both username and password.","Missing information",JOptionPane.WARNING_MESSAGE);return;}
        try{
            if(AuthService.authenticate(username,password,requiredRole)){
                dispose();new ReservationFrame(username,requiredRole.equals("ADMIN")).setVisible(true);
            }else{passwordField.setText("");JOptionPane.showMessageDialog(this,"These credentials do not match the selected sign-in.","Access denied",JOptionPane.ERROR_MESSAGE);}
        }catch(Exception exception){JOptionPane.showMessageDialog(this,"Sign in failed:\n"+exception.getMessage(),"Sign in failed",JOptionPane.ERROR_MESSAGE);}
        finally{Arrays.fill(password,'\0');}
    }
}
