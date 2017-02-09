package view;

import java.awt.CardLayout;
import javax.swing.JPanel;
import pop3.Client2;
import pop3.Mail;

/**
 * Class Client
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 * 
 */

public class ClientFrame extends javax.swing.JFrame  {    
    JPanel cards;
    ConnectionPane card1;
    BoiteMailPane card2;
    DetailMailPane card3;
    Client2 client;
    
    public ClientFrame(){             
        this.setTitle("SuperClientPop3");
        this.pack();
        this.setLocationRelativeTo(null);
        this.setSize(1000, 600);
        this.setResizable(false);
        this.setDefaultCloseOperation(ClientFrame.EXIT_ON_CLOSE);
        
        // Create the "cards".
        card1 = new ConnectionPane();
        card2 = new BoiteMailPane();
        card3 = new DetailMailPane();

        // Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        cards.add(card1,"Card 1");
        cards.add(card2,"Card 2");
        cards.add(card3,"Card 3");
        
        this.setContentPane(cards);
        this.setVisible(true);
    }
    
    public void listeMail() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        card2.hydrateMailsInView();
        cl.show(cards, "Card 2");
    }
    
    public void deconnection(){
        client.close();
        client = null;
        card1.setjPasswordField1("");
        card1.setjTextField1("");
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, "Card 1");
    }
    
    public void detailMail(int mailId){
        Mail m = client.getMails().get(mailId);
        card3.setMail(m.getFrom(),m.getDate(),m.getSubject(),m.getMessage());
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, "Card 3");
    }

    public Client2 getClient() {
        return client;
    }
    
    public void hydrateClientIdentifiers(String mail, String password) {
        // Instanciate client model
        client = new Client2("localhost", 3000, "userMachin", "test");
        String[] userMail = mail.split("@");
        client.setUsername(userMail[0]);
        client.setPassword(password);
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientFrame().setVisible(true);
            }
        });
    }
    
}
