package ca.redapp.ui;

import ca.redapp.ui.component.RButton;
import ca.redapp.ui.component.RComboBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.EncryptionUtils;
import ca.redapp.util.GeoValidator;
import ca.redapp.util.RPreferences;
import ca.redapp.util.SpotWXUtils;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.redapp.util.SpotWXUtils.DownloadSpotWXModelData;
import static ca.redapp.util.SpotWXUtils.GetModelNameWithDescription;
import static java.awt.SystemColor.text;

public class SpotWXImportDialog extends JDialog {
    private final Main app;
    private final RPreferences prefs;
    private String apiKey;
    private boolean confirmed = false;


    // UX elements
    private final JPanel bottomLeftPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private final JPanel bottomRightPanel = new JPanel();
    private RButton cancelButton;
    private final RButton importButton = new RButton(Main.resourceManager.getString("ui.spotwx.btn.import"));

    private final JPanel mainContentPanel = new JPanel(new GridBagLayout());

    //Step 1
    private final RLabel latitudeLabel = new RLabel(Main.resourceManager.getString("ui.label.header.latitude"));
    private final RLabel longitudeLabel = new RLabel(Main.resourceManager.getString("ui.label.header.longitude"));
    private RTextField latitudeText;
    private RTextField longitudeText;
    private final RLabel coordinateErrorLabel  = new RLabel();
    private final RButton getModelsButton = new RButton(Main.resourceManager.getString("ui.spotwx.getmodels"));
    private final RLabel spotWXDefaultKeyNote1Label = new RLabel(Main.resourceManager.getString("ui.spotwx.defaultkeynote1"));
    private final RLabel spotWXDefaultKeyNote2Label = new RLabel(Main.resourceManager.getString("ui.spotwx.defaultkeynote2"));

    //Step 2
    private final RLabel modelsLabel = new RLabel(Main.resourceManager.getString("ui.spotwx.selectmodels"));
    private RComboBox<String> modelsComboBox;
    private final List<String> modelNames = new ArrayList<String>();

    //positional values
    private double Latitude = 0;
    private double Longitude = 0;

    private Date forecastDate;

    //This will hold the available models to populate the comobo box
    private DefaultComboBoxModel comboModel;

    //Output data!
    public String tempFileName;
    public String selectedModelName;


    public SpotWXImportDialog(Main app, double latitude, double longitude, Date date) {
        super(app.frmRedapp);
        this.app = app;
        prefs = new RPreferences(Preferences.userRoot().node("ca.hss.app.redapp.ui.Main"));
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.forecastDate = date;


        this.apiKey = prefs.getString("SpotAPIKey", "");
        if (this.apiKey.isEmpty()) {
            //this.apiKey = Main.resourceManager.getString("spotwx.api.defaultkey");
            ResourceBundle keyResourceBundle = ResourceBundle.getBundle("key");
            String encDefaultKey =keyResourceBundle.getString("spotwx.api.defaultkey");
            EncryptionUtils encryptionUtils = new EncryptionUtils(prefs);
            try {
                this.apiKey = encryptionUtils.decrypt(encDefaultKey);
            } catch (Exception ex){
                ;
            }

        }

        initialize();


        setDialogPosition(app.getForm());
    }

    private void initialize() {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle(Main.resourceManager.getString("ui.dlg.title.spotwximport"));
        SetIcons();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        getContentPane().setLayout(new BorderLayout());


        latitudeText = new RTextField();
        latitudeText.setText(Double.toString(this.Latitude));
        latitudeText.setCaretPosition(0);
        longitudeText = new RTextField();
        longitudeText.setText(Double.toString(this.Longitude));
        longitudeText.setCaretPosition(0);
        AddUXElements();

        GetModelList();

        this.pack();
    }

    private void AddUXElements() {
        /*
         Bottom of the dialog content
        */
        bottomPanel.setLayout(new BorderLayout());

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);


        bottomRightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        bottomRightPanel.setLayout(new BorderLayout());
        bottomPanel.add(bottomRightPanel, BorderLayout.EAST);
        importButton.setBounds(0, 0, 212, 41);
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GetCoordinatesValid()) {
                    Latitude = Double.parseDouble(latitudeText.getText());
                    Longitude = Double.parseDouble(longitudeText.getText());
                    String model = modelNames.get(modelsComboBox.getSelectedIndex());
                    selectedModelName = modelsComboBox.getSelectedItem().toString();
                    tempFileName = DownloadSpotWXModelData(Latitude, Longitude, model, forecastDate, apiKey);
                   confirmed = true;
                   dispose();
                } else {
                    ToggleUXModelVisibility();
                }


            }
        });
        bottomRightPanel.add(importButton, BorderLayout.PAGE_END);


        bottomLeftPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        bottomLeftPanel.setLayout(new BorderLayout());
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);

        cancelButton = new RButton(Main.resourceManager.getString("ui.spotwx.btn.cancel"), RButton.Decoration.Close);
        cancelButton.setBounds(0, 0, 121, 41);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpotWXImportDialog.this.setVisible(false);
                confirmed = false;
                dispose();
            }
        });
        bottomLeftPanel.add(cancelButton, BorderLayout.PAGE_START);


        //Input fields
        //mainContentPanel.setLayout(new GridBagLayout());
        getContentPane().add(mainContentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3,10,3,10);

        //Latitude
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        mainContentPanel.add(latitudeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2;
        mainContentPanel.add(latitudeText, gbc);

        //Longitude
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy + 1;
        gbc.weightx = 1.0;
        mainContentPanel.add(longitudeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 2;
        mainContentPanel.add(longitudeText, gbc);

        gbc.gridx = 0;
        gbc.gridy = gbc.gridy + 1;
        gbc.gridwidth = 2;
        mainContentPanel.add(coordinateErrorLabel, gbc);
        coordinateErrorLabel.setHorizontalAlignment(JLabel.CENTER);
        coordinateErrorLabel.setForeground(Color.red);



        String defaultKey = "";
        try {

            ResourceBundle keyResourceBundle = ResourceBundle.getBundle("key");
            String encDefaultKey =keyResourceBundle.getString("spotwx.api.defaultkey");
            EncryptionUtils encryptionUtils = new EncryptionUtils(prefs);
            defaultKey = encryptionUtils.decrypt(encDefaultKey);


        } catch (Exception ex) {
            ;
        }

        if(Objects.equals(apiKey, defaultKey)){
            spotWXDefaultKeyNote1Label.setForeground(Color.blue);
            spotWXDefaultKeyNote2Label.setForeground(Color.blue);

            gbc.gridx = 0;
            gbc.gridy = gbc.gridy + 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            mainContentPanel.add(spotWXDefaultKeyNote1Label, gbc);
            gbc.gridx = 0;
            gbc.gridy = gbc.gridy + 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            mainContentPanel.add(spotWXDefaultKeyNote2Label, gbc);
        }

        // Get models button
        gbc.gridx = 1;
        gbc.gridy = gbc.gridy + 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        getModelsButton.setHorizontalAlignment(JButton.RIGHT);
        getModelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GetModelList();
            }
        });
        mainContentPanel.add(getModelsButton, gbc);




        //model picker label
        gbc.gridx = 1;
        gbc.gridy = gbc.gridy + 1;
        gbc.gridwidth = 1;
        mainContentPanel.add(modelsLabel, gbc);
        modelsLabel.setVisible(false);

        //Model picker
        gbc.gridx = 1;
        gbc.gridy = gbc.gridy + 1;
        gbc.gridwidth = 1;
        String[] temp = {""};
        comboModel = new DefaultComboBoxModel<String>(temp);
        modelsComboBox = new RComboBox<String>();
        modelsComboBox.setModel(comboModel);
        modelsComboBox.addActionListener((f) -> {
            int i = modelsComboBox.getSelectedIndex();

        });
        mainContentPanel.add(modelsComboBox, gbc);




        ToggleUXModelVisibility();

        //Output details

    }

    private void GetModelList() {
        if (GetCoordinatesValid()) {
            Latitude = Double.parseDouble(latitudeText.getText());
            Longitude = Double.parseDouble(longitudeText.getText());
            List<String> models = SpotWXUtils.fetchModelListFromAPI(Latitude, Longitude, apiKey);

            if(models.size() == 1){
                JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.spotwx.err.nomodels") + "\n" + models.get(0), "Error", JOptionPane.WARNING_MESSAGE);
            }
            else if (!models.isEmpty()) {
                comboModel.removeAllElements();
                modelNames.clear();
                for (String model : models) {
                    String modelDescription = GetModelNameWithDescription(model);
                    comboModel.addElement(modelDescription);
                    modelNames.add(model);
                }
            } else {
                JOptionPane.showMessageDialog(null, Main.resourceManager.getString("ui.spotwx.err.nomodels"), "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
        ToggleUXModelVisibility();
    }

    private boolean GetCoordinatesValid() {
        coordinateErrorLabel.setText("");
        try {
            Latitude = Double.parseDouble(latitudeText.getText());
            if (!GeoValidator.isValidLatitude(Latitude)) {
                coordinateErrorLabel.setText(Main.resourceManager.getString("ui.label.header.latitude.error"));
                return false;
            }


        } catch (Exception ex) {
            coordinateErrorLabel.setText(Main.resourceManager.getString("ui.label.header.latitude.error"));
            return false;
        }
        try {
            Longitude = Double.parseDouble(longitudeText.getText());
            if (!GeoValidator.isValidLongitude(Longitude)) {
                coordinateErrorLabel.setText(Main.resourceManager.getString("ui.label.header.longitude.error"));
                return false;
            }

        } catch (Exception ex) {
            coordinateErrorLabel.setText(Main.resourceManager.getString("ui.label.header.longitude.error"));
            return false;
        }
        return true;
    }

    private void ToggleUXModelVisibility(){
        //shows or hides the model-related items based on whether we have models loaded from the api
        int size = comboModel.getSize();
        modelsComboBox.setVisible (comboModel.getSize() > 1);
        modelsLabel.setVisible(modelsComboBox.isVisible());
        importButton.setEnabled(modelsComboBox.isVisible());
        this.pack();
    }

    private void SetIcons() {
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.resourceManager.getImagePath("ui.icon.window.redapp"))));
        icons.add(Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.resourceManager.getImagePath("ui.icon.window.redapp20"))));
        icons.add(Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.resourceManager.getImagePath("ui.icon.window.redapp40"))));
        setIconImages(icons);
    }

    public boolean isConfirmed() {
        return confirmed;
    }


    private void setDialogPosition(Window dlg) {
        if (dlg == null)
            return;
        int width = getWidth();
        int height = getHeight();
        int x = dlg.getX();
        int y = dlg.getY();
        int rwidth = dlg.getWidth();
        int rheight = dlg.getHeight();
        x = (int) (x + (rwidth / 2.0) - (width / 2.0));
        y = (int) (y + (rheight / 2.0) - (height / 2.0)) + 30;
        setLocation(x, y);
    }
}
