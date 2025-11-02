
package view;
 
import controller.KontakController; 
import java.io.*; 
import model.Kontak; 
import javax.swing.*; 
import javax.swing.table.DefaultTableModel; 
import java.sql.SQLException; 
import java.util.List; 
import java.util.logging.Level; 
import java.util.logging.Logger;

public class PengelolaanKontakFrame extends javax.swing.JFrame {

    private DefaultTableModel model; 
    private KontakController controller;
    /**
     * Creates new form PengelolaanKontakFrame
     */
    public PengelolaanKontakFrame() {
        initComponents();
        model = new DefaultTableModel(new String[] {"No", "Nama", "Nomor Telepon", "Kategori"}, 0); 
        tblKontak.setModel(model); 
        controller = new KontakController();
        loadContacts(); 
    }
    
    private void addContact() {       
         String nama = txtNama.getText().trim(); 
    String nomorTelepon = txtNomorTelepon.getText().trim(); 
    String kategori = (String) cmbKategori.getSelectedItem(); 
    
    if (!validatePhoneNumber(nomorTelepon)) { 
        return; // Validasi nomor telepon gagal 
    } 
    
    try { 
        if (controller.isDuplicatePhoneNumber(nomorTelepon, null)) {
            JOptionPane.showMessageDialog(this, "Kontak nomor telepon ini sudah ada.", "Kesalahan", JOptionPane.WARNING_MESSAGE);
            return; 
        } 
 
        controller.addContact(nama, nomorTelepon, kategori); 
        loadContacts(); 
        JOptionPane.showMessageDialog(this, "Kontak berhasil ditambahkan!"); 
        clearInputFields(); 
    } catch (SQLException ex) { 
        showError("Gagal menambahkan kontak: " + ex.getMessage()); 
    }  
} 
 
    private boolean validatePhoneNumber(String phoneNumber) { 
        if (phoneNumber == null || phoneNumber.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong."); 
            return false; 
        } 
        if (!phoneNumber.matches("\\d+")) { // Hanya angka 
        JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh berisi angka."); 
        return false; 
    } 
    if (phoneNumber.length() < 8 || phoneNumber.length() > 15) { // Panjang 8-15 
        JOptionPane.showMessageDialog(this, "Nomor telepon harus memiliki panjang antara 8 hingga 15 karakter."); 
        return false; 
    } 
    return true; 
} 
 
private void clearInputFields() { 
    txtNama.setText(""); 
    txtNomorTelepon.setText(""); 
    cmbKategori.setSelectedIndex(0); 
}
     
    private void editContact() { 
     int selectedRow = tblKontak.getSelectedRow(); 
    if (selectedRow == -1) { 
        JOptionPane.showMessageDialog(this, "Pilih kontak yang ingin diperbarui.", "Kesalahan", JOptionPane.WARNING_MESSAGE); 
        return; 
    } 

    try {
        // Ambil data dari baris yang dipilih
        String namaTable = model.getValueAt(selectedRow, 1).toString();
        String nomorTeleponTable = model.getValueAt(selectedRow, 2).toString();
        
        // Cari ID berdasarkan nama dan nomor telepon dari database
        List<Kontak> contacts = controller.getAllContacts();
        int idToEdit = -1;
        
        for (Kontak contact : contacts) {
            if (contact.getNama().equals(namaTable) && contact.getNomorTelepon().equals(nomorTeleponTable)) {
                idToEdit = contact.getId();
                break;
            }
        }
        
        if (idToEdit != -1) {
            String nama = txtNama.getText().trim(); 
            String nomorTelepon = txtNomorTelepon.getText().trim(); 
            String kategori = (String) cmbKategori.getSelectedItem(); 

            if (!validatePhoneNumber(nomorTelepon)) { 
                return; 
            } 

            if (controller.isDuplicatePhoneNumber(nomorTelepon, idToEdit)) { 
                JOptionPane.showMessageDialog(this, "Kontak nomor telepon ini sudah ada.", "Kesalahan", JOptionPane.WARNING_MESSAGE); 
                return; 
            } 

            controller.updateContact(idToEdit, nama, nomorTelepon, kategori); 
            loadContacts(); 
            JOptionPane.showMessageDialog(this, "Kontak berhasil diperbarui!"); 
            clearInputFields(); 
        } else {
            showError("Gagal menemukan kontak untuk diedit");
        }
    } catch (SQLException ex) { 
        showError("Gagal memperbarui kontak: " + ex.getMessage()); 
    } 
} 
 
private void populateInputFields(int selectedRow) { 
    // Ambil data dari JTable 
    String nama = model.getValueAt(selectedRow, 1).toString(); 
    String nomorTelepon = model.getValueAt(selectedRow, 2).toString(); 
    String kategori = model.getValueAt(selectedRow, 3).toString(); 
 
    // Set data ke komponen input 
    txtNama.setText(nama); 
    txtNomorTelepon.setText(nomorTelepon); 
    cmbKategori.setSelectedItem(kategori); 
}

    private void deleteContact() { 
        int selectedRow = tblKontak.getSelectedRow(); 
    if (selectedRow != -1) { 
        try {
            // Ambil data dari baris yang dipilih
            String nama = model.getValueAt(selectedRow, 1).toString();
            String nomorTelepon = model.getValueAt(selectedRow, 2).toString();
            
            // Cari ID berdasarkan nama dan nomor telepon
            List<Kontak> contacts = controller.getAllContacts();
            int idToDelete = -1;
            
            for (Kontak contact : contacts) {
                if (contact.getNama().equals(nama) && contact.getNomorTelepon().equals(nomorTelepon)) {
                    idToDelete = contact.getId();
                    break;
                }
            }
            
            if (idToDelete != -1) {
                controller.deleteContact(idToDelete); 
                loadContacts(); 
                JOptionPane.showMessageDialog(this, "Kontak berhasil dihapus!"); 
                clearInputFields(); 
            } else {
                showError("Gagal menemukan kontak untuk dihapus");
            }
        } catch (SQLException e) { 
            showError(e.getMessage()); 
        } 
    } else {
        JOptionPane.showMessageDialog(this, "Pilih kontak yang akan dihapus!");
    }
}
    
    private void searchContact() { 
        String keyword = txtPencarian.getText().trim(); 
        if (!keyword.isEmpty()) { 
            try { 
                List<Kontak> contacts = controller.searchContacts(keyword); 
                model.setRowCount(0); // Bersihkan tabel 
                for (Kontak contact : contacts) { 
                    model.addRow(new Object[]{ 
                            contact.getId(), 
                            contact.getNama(), 
                            contact.getNomorTelepon(), 
                            contact.getKategori() 
                    }); 
                } 
                if (contacts.isEmpty()) { 
                JOptionPane.showMessageDialog(this, "Tidak ada kontak ditemukan."); 
                } 
            } catch (SQLException ex) { 
                showError(ex.getMessage()); 
            } 
        } else { 
            loadContacts(); 
        } 
    }
    
    private void exportToCSV() { 
        JFileChooser fileChooser = new JFileChooser(); 
        fileChooser.setDialogTitle("Simpan File CSV"); 
        int userSelection = fileChooser.showSaveDialog(this); 
        if (userSelection == JFileChooser.APPROVE_OPTION) { 
            File fileToSave = fileChooser.getSelectedFile(); 
 
            // Tambahkan ekstensi .csv jika pengguna tidak menambahkannya 
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) { 
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv"); 
            } 
 
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) { 
                writer.write("ID,Nama,Nomor Telepon,Kategori\n"); // Header CSV 
                for (int i = 0; i < model.getRowCount(); i++) { 
                    writer.write( 
                        model.getValueAt(i, 0) + "," + 
                        model.getValueAt(i, 1) + "," + 
                        model.getValueAt(i, 2) + "," + 
                        model.getValueAt(i, 3) + "\n" 
                    ); 
                } 
                JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke " + fileToSave.getAbsolutePath()); 
            } catch (IOException ex) { 
                showError("Gagal menulis file: " + ex.getMessage()); 
            } 
        } 
    } 
 
    private void importFromCSV() { 
        showCSVGuide(); 
 
        int confirm = JOptionPane.showConfirmDialog( 
        this, 
        "Apakah Anda yakin file CSV yang dipilih sudah sesuai dengan format?", 
        "Konfirmasi Impor CSV", 
        JOptionPane.YES_NO_OPTION 
    ); 
 
    if (confirm == JOptionPane.YES_OPTION) { 
        JFileChooser fileChooser = new JFileChooser(); 
        fileChooser.setDialogTitle("Pilih File CSV"); 
        int userSelection = fileChooser.showOpenDialog(this); 
 
        if (userSelection == JFileChooser.APPROVE_OPTION) { 
            File fileToOpen = fileChooser.getSelectedFile(); 
 
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) { 
                String line = reader.readLine(); // Baca header 
                if (!validateCSVHeader(line)) { 
                    JOptionPane.showMessageDialog(this, "Format header CSV tidak valid. Pastikan header adalah: ID,Nama,Nomor Telepon,Kategori", "Kesalahan CSV", JOptionPane.ERROR_MESSAGE); 
                        return; 
                } 
 
                int rowCount = 0; 
                int errorCount = 0; 
                int duplicateCount = 0; 
                StringBuilder errorLog = new StringBuilder("Baris dengan kesalahan:\n"); 
 
                while ((line = reader.readLine()) != null) { 
                    rowCount++; 
                    String[] data = line.split(","); 
 
                    if (data.length != 4) { 
                        errorCount++; 
                        errorLog.append("Baris ").append(rowCount + 1).append(": Format kolom tidak sesuai.\n"); 
                        continue; 
                    } 
 
                    String nama = data[1].trim(); 
                    String nomorTelepon = data[2].trim(); 
                    String kategori = data[3].trim(); 
 
                    if (nama.isEmpty() || nomorTelepon.isEmpty()) { 
                        errorCount++; 
                        errorLog.append("Baris ").append(rowCount + 1).append(": Nama atau Nomor Telepon kosong.\n"); 
                        continue; 
                    } 
 
                    if (!validatePhoneNumber(nomorTelepon)) { 
                        errorCount++; 
                        errorLog.append("Baris ").append(rowCount + 1).append(": Nomor Telepon tidak valid.\n"); 
                        continue; 
                    } 
 
                    try { 
                        if (controller.isDuplicatePhoneNumber(nomorTelepon, null)) { 
                            duplicateCount++; 
                            errorLog.append("Baris ").append(rowCount + 1).append(": Kontak sudah ada.\n"); 
                            continue; 
                        } 
                    } catch (SQLException ex) { 
                        
    Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(Level.SEVERE , null, ex); 
                    } 
 
                    try { 
                        controller.addContact(nama, nomorTelepon, kategori); 
                    } catch (SQLException ex) { 
                        errorCount++; 
                        errorLog.append("Baris ").append(rowCount + 1).append(": Gagal menyimpan ke database - ").append(ex.getMessage()).append("\n"); 
                    } 
                } 
 
                loadContacts(); 
 
                if (errorCount > 0 || duplicateCount > 0) { 
                    errorLog.append("\nTotal baris dengan kesalahan: ").append(errorCount).append("\n"); 
                    errorLog.append("Total baris duplikat: ").append(duplicateCount).append("\n"); 
                    JOptionPane.showMessageDialog(this, 
    errorLog.toString(), "Kesalahan Impor", JOptionPane.WARNING_MESSAGE); 
                } else { 
                    JOptionPane.showMessageDialog(this, "Semua data berhasil diimpor."); 
                } 
            } catch (IOException ex) { 
                showError("Gagal membaca file: " + ex.getMessage()); 
            } 
        } 
    } 
} 
 
    private void showCSVGuide() { 
        String guideMessage = "Format CSV untuk impor data:\n" + 
            "- Header wajib: ID, Nama, Nomor Telepon, Kategori\n" + 
            "- ID dapat kosong (akan diisi otomatis)\n" + 
            "- Nama dan Nomor Telepon wajib diisi\n" + 
            "- Contoh isi file CSV:\n" + 
            "  1, Andi, 08123456789, Teman\n" + 
            "  2, Budi Doremi, 08567890123, Keluarga\n\n" + 
            "Pastikan file CSV sesuai format sebelum melakukan impor."; 
        JOptionPane.showMessageDialog(this, guideMessage, "Panduan Format CSV", JOptionPane.INFORMATION_MESSAGE); 
    } 
 
    private boolean validateCSVHeader(String header) { 
        return header != null && 
    header.trim().equalsIgnoreCase("ID,Nama,Nomor Telepon,Kategori"); 
    }

    private void loadContacts() { 
    try { 
        model.setRowCount(0); 
        List<Kontak> contacts = controller.getAllContacts(); 
 
        int rowNumber = 1; 
        for (Kontak contact : contacts) { 
            model.addRow(new Object[]{ 
                    rowNumber++, 
                    contact.getNama(), 
                    contact.getNomorTelepon(), 
                    contact.getKategori() 
            }); 
        } 
    } catch (SQLException e) { 
        showError(e.getMessage()); 
    } 
} 
 
private void showError(String message) { 
    JOptionPane.showMessageDialog(this, message, "Error", 
JOptionPane.ERROR_MESSAGE); 
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        txtNomorTelepon = new javax.swing.JTextField();
        txtPencarian = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKontak = new javax.swing.JTable();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(30, 30, 46));

        lblJudul.setBackground(new java.awt.Color(255, 255, 255));
        lblJudul.setForeground(new java.awt.Color(255, 255, 255));
        lblJudul.setText("APLIKASI PENGELOLAAN KONTAK");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblJudul)
                .addGap(183, 183, 183))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblJudul)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(30, 30, 46));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Nama Kontak");

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Nomor Telepon");

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Kategori");

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Pencarian");

        txtNama.setBackground(new java.awt.Color(50, 50, 70));

        txtNomorTelepon.setBackground(new java.awt.Color(50, 50, 70));

        txtPencarian.setBackground(new java.awt.Color(50, 50, 70));
        txtPencarian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPencarianKeyTyped(evt);
            }
        });

        cmbKategori.setBackground(new java.awt.Color(50, 50, 70));
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga ", "Teman ", "Kantor" }));

        tblKontak.setBackground(new java.awt.Color(255, 255, 255));
        tblKontak.setForeground(new java.awt.Color(0, 0, 0));
        tblKontak.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKontak.setGridColor(new java.awt.Color(180, 180, 190));
        tblKontak.setSelectionBackground(new java.awt.Color(0, 120, 215));
        tblKontak.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblKontak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKontakMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblKontak);

        btnTambah.setBackground(new java.awt.Color(0, 123, 255));
        btnTambah.setForeground(new java.awt.Color(0, 0, 0));
        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnEdit.setBackground(new java.awt.Color(40, 167, 69));
        btnEdit.setForeground(new java.awt.Color(0, 0, 0));
        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnHapus.setBackground(new java.awt.Color(220, 53, 69));
        btnHapus.setForeground(new java.awt.Color(0, 0, 0));
        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnExport.setBackground(new java.awt.Color(255, 193, 7));
        btnExport.setForeground(new java.awt.Color(0, 0, 0));
        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnImport.setBackground(new java.awt.Color(108, 117, 125));
        btnImport.setForeground(new java.awt.Color(0, 0, 0));
        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtNomorTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbKategori, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(txtPencarian, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(btnTambah, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(6, 6, 6)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 13, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNomorTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPencarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnImport)
                    .addComponent(btnExport))
                .addGap(40, 40, 40))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
         addContact(); 
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        editContact();
    }//GEN-LAST:event_btnEditActionPerformed

    private void tblKontakMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKontakMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblKontak.getSelectedRow(); 
        if (selectedRow != -1) { 
        populateInputFields(selectedRow); 
        } 
    }//GEN-LAST:event_tblKontakMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        deleteContact();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void txtPencarianKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPencarianKeyTyped
        // TODO add your handling code here:
        searchContact();
    }//GEN-LAST:event_txtPencarianKeyTyped

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
         exportToCSV();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        // TODO add your handling code here:
        importFromCSV(); 
    }//GEN-LAST:event_btnImportActionPerformed

    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PengelolaanKontakFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JTable tblKontak;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNomorTelepon;
    private javax.swing.JTextField txtPencarian;
    // End of variables declaration//GEN-END:variables
}
