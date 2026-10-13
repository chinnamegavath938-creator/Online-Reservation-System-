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
import java.util.Arrays;

final class RegistrationFrame extends JFrame {
    private final JTextField usernameField=new JTextField(20);
    private final JPasswordField passwordField=new JPasswordField(20);
    private final JPasswordField confirmField=new JPasswordField(20);

    RegistrationFrame(){
        super("Customer Registration");setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setSize(470,365);setLocationRelativeTo(null);
        JPanel content=new JPanel(new BorderLayout(0,16));content.setBorder(BorderFactory.createEmptyBorder(26,32,26,32));
        JLabel heading=new JLabel("Create your customer account");heading.setFont(heading.getFont().deriveFont(23f));content.add(heading,BorderLayout.NORTH);
        JPanel fields=new JPanel(new GridBagLayout());GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(8,5,8,5);c.anchor=GridBagConstraints.WEST;
        c.gridx=0;c.gridy=0;fields.add(new JLabel("Username (3–24 characters)"),c);c.gridx=1;fields.add(usernameField,c);
        c.gridx=0;c.gridy=1;fields.add(new JLabel("Password (8+ characters)"),c);c.gridx=1;fields.add(passwordField,c);
        c.gridx=0;c.gridy=2;fields.add(new JLabel("Confirm password"),c);c.gridx=1;fields.add(confirmField,c);content.add(fields,BorderLayout.CENTER);
        JButton back=new JButton("Back to sign in");back.addActionListener(event->{dispose();new LoginFrame("CUSTOMER").setVisible(true);});
        JButton create=new JButton("Register");create.addActionListener(event->register());JPanel actions=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));actions.add(back);actions.add(create);content.add(actions,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(create);setContentPane(content);
    }

    private void register(){
        String username=usernameField.getText().trim().toLowerCase(java.util.Locale.ROOT);char[] password=passwordField.getPassword(),confirm=confirmField.getPassword();
        if(!username.matches("[a-z0-9_]{3,24}")){show("Username must be 3–24 characters and use letters, numbers, or underscores.","Invalid username",JOptionPane.WARNING_MESSAGE);return;}
        if(password.length<8){show("Password must contain at least 8 characters.","Password too short",JOptionPane.WARNING_MESSAGE);return;}
        if(!Arrays.equals(password,confirm)){show("The passwords do not match.","Check password",JOptionPane.WARNING_MESSAGE);return;}
        try{AuthService.registerCustomer(username,password);JOptionPane.showMessageDialog(this,"Your customer account has been created. Sign in to continue.","Registration complete",JOptionPane.INFORMATION_MESSAGE);dispose();new LoginFrame("CUSTOMER").setVisible(true);}
        catch(java.sql.SQLException ex){show("That username is already registered. Choose another one.","Username unavailable",JOptionPane.WARNING_MESSAGE);}
        catch(Exception ex){show("Could not create the account: "+ex.getMessage(),"Registration failed",JOptionPane.ERROR_MESSAGE);}
        finally{Arrays.fill(password,'\0');Arrays.fill(confirm,'\0');}
    }

    private void show(String message,String title,int type){JOptionPane.showMessageDialog(this,message,title,type);}
}
