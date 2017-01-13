package view;

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

public class DetailMailPane extends javax.swing.JPanel {
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    
    public DetailMailPane() {
        initComponents();
    }
    
    public void initComponents(){
        jButton1 = new javax.swing.JButton();
        jButton1.addActionListener((java.awt.event.ActionEvent evt) -> {
            retourVuePleine();
        });
        
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setPreferredSize(new java.awt.Dimension(1000, 600));

        jButton1.setText("Retour");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36));
        jLabel1.setText("<3");

        jLabel2.setText("Le 12/01/2017 à 12:30");

        jLabel3.setText("De : Sacha");

        jTextPane1.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum non ullamcorper augue. Maecenas purus dolor, posuere ut faucibus eu, egestas ac nisi. Morbi vestibulum diam sed efficitur lobortis. Cras sed felis a justo finibus volutpat id at arcu. Nam rutrum dolor ligula, ut malesuada sem sodales nec. In tempus, lacus eget varius pulvinar, erat libero mollis elit, id hendrerit tortor velit a justo. Duis dapibus tellus ut nibh cursus sodales. Vestibulum vitae tempor ante. Proin sit amet nulla vitae sapien vestibulum ultricies imperdiet ut erat. Mauris mollis finibus est vel cursus.  Proin et ornare mi. Donec purus nisl, pharetra eget mauris vel, pharetra lobortis purus. Aenean lobortis eu risus eu ultricies. Proin at nisi pharetra, vulputate dui at, egestas nunc. Integer ante ante, ultricies quis sem mollis, ultrices finibus nisl. Nullam nec eleifend erat, nec vulputate quam. Aenean eleifend tempus sem quis semper. Maecenas facilisis diam et orci auctor, eu vulputate sapien maximus. Pellentesque quam mauris, commodo at quam sit amet, gravida tempor turpis. Sed euismod vestibulum dapibus.  Donec sit amet tincidunt eros. Donec et magna sit amet lacus mollis finibus nec eu tellus. Ut gravida nibh quis fringilla porta. Duis iaculis turpis lacus, quis elementum eros viverra et. Suspendisse ornare posuere molestie. Interdum et malesuada fames ac ante ipsum primis in faucibus. Duis hendrerit purus in pretium egestas.");
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addContainerGap(670, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(28, 28, 28))
        );
    }
    
    private void retourVuePleine() {
        ClientFrame topFrame = (ClientFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.listeMail();
    }
}
