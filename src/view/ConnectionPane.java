package view;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

/**
 * Class Client
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 * 
 */

public class ConnectionPane extends javax.swing.JPanel {    
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JCheckBox checkbox;

    public void setjTextField1(String s) {
        jTextField1.setText(s);
        
    }

    public void setjPasswordField1(String s) {
        jPasswordField1.setText(s);
    }
    
    public ConnectionPane() {
        initComponents();
    }
    
    public void initComponents(){        
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 600));

        jLabel1.setFont(new java.awt.Font("Segoe Keycaps", 0, 24)); // NOI18N
        jLabel1.setText("MailBox");

        jLabel2.setText("Adresse mail");

        jLabel3.setText("Mot de passe");

        jButton1.setText("Se connecter");
        jButton1.addActionListener((java.awt.event.ActionEvent evt) -> {
            jButton1ActionPerformed(evt);
        });
        
        checkbox = new JCheckBox("Delete messages on server");
        checkbox.setSelected(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(380, 380, 380)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(69, 69, 69)
                                .addComponent(jLabel2))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField1)
                                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(431, 431, 431)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(450, 450, 450)
                        .addComponent(jLabel3)))
                .addContainerGap(410, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addComponent(jLabel1)
                .addGap(89, 89, 89)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jButton1)
                .addContainerGap(121, Short.MAX_VALUE))
        );
        layout.addLayoutComponent(checkbox, this);
    }
    
    // TOOD : Afficher des messages d'erreur si : le serveur est down, adresse/mdp non saisis ou pas reconnus sur le serveur, etc.
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String mailUtilisateur = jTextField1.getText();
        String motDePasse = new String(jPasswordField1.getPassword()); 
        
        if(!mailUtilisateur.isEmpty() && !motDePasse.isEmpty() && mailUtilisateur.contains("@")){
            ClientFrame topFrame = (ClientFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.hydrateClientIdentifiers(mailUtilisateur, motDePasse);

            if(topFrame.getClient().isConnected()){
                topFrame.getClient().serverDialog();
            } else {
                topFrame.getClient().localConnection();
            }
            topFrame.listeMail(); 
        } else {
            System.err.println("Wrong credentials");
        }
        
    }
}
