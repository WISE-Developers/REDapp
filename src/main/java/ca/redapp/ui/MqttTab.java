package ca.redapp.ui;
/***********************************************************************
 * REDapp - MqttTab.java
 * Copyright (C) 2015-2019 The REDapp Development Team
 * Homepage: http://redapp.org
 * 
 * REDapp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * REDapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDapp. If not see <http://www.gnu.org/licenses/>. 
 **********************************************************************/

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import ca.redapp.ui.component.RGroupBox;
import ca.redapp.ui.component.RLabel;
import ca.redapp.ui.component.RPasswordField;
import ca.redapp.ui.component.RTextArea;
import ca.redapp.ui.component.RTextField;
import ca.redapp.util.REDappLogger;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.DefaultListModel;

import java.awt.BorderLayout;

public class MqttTab extends REDappTab implements MqttCallback, IMqttActionListener {
	private static final long serialVersionUID = 1L;
	private Main app;
	private String myId;
	private boolean initialized = false;
	private ObjectMapper mapper = new ObjectMapper();
	private MqttAsyncClient client;
	private BusyDialog busyDialog = null;
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private DefaultListModel<MqttData> mqttMessages;
	private Object locker = new Object();
	
	public MqttTab(Main app) {
		this.app = app;

		myId = "redapp_" + getPID() + "-" + getHost().toUpperCase();
		
		initialize();
		
		txtAddress.setText(prefs.get("mqtt_address", ""));
		cmbProtocol.setSelectedIndex(prefs.getInt("mqtt_protocol", 0));
		txtUsername.setText(prefs.get("mqtt_username", ""));
	}
	
	private long getPID() {
		Long pid = null;
		try {
			String processName = ManagementFactory.getRuntimeMXBean().getName();
			pid = Long.parseLong(processName.split("@")[0]);
		}
		catch (Exception ex) { }
		if (pid == null) {
			pid = new Random(System.currentTimeMillis()).nextLong();
		}
		return pid;
	}
	
	public String getHost() {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		}
		catch (Exception ex) { }
		if (host == null) {
			host = Long.toHexString(new Random(System.currentTimeMillis()).nextLong());
		}
		return host;
	}
	
	private RLabel lblBrokerAddress;
	private RLabel lblProtocol;
	private RLabel lblUsername;
	private RLabel lblPassword;
	private RLabel lblMqttId;
	private RLabel lblClientId;
	private RLabel lblRebootId;
	private RLabel lblJobListId;
	private RTextField txtAddress;
	private RTextField txtUsername;
	private RTextField txtMqttId;
	private RTextField txtShutdownId;
	private RTextField txtRebootId;
	private RTextField txtJobListId;
	private RPasswordField txtPassword;
	private RTextArea txtPayload;
	private JButton btnConnect;
	private JButton btnSendShutdown;
	private JButton btnSendReboot;
	private JButton btnListJobs;
	private JButton btnClear;
	private JComboBox<String> cmbProtocol;
	private JComboBox<String> cmbJobListFilter;
	private JList<MqttData> messageList;
	private JButton btnReportActive;

	@Override
	protected void initialize() {
		if (initialized)
			return;
		initialized = true;

		setLayout(null);
		if (Launcher.javaVersion.major < 9)
			setBounds(0, 0, 971, 501);
		else
			setBounds(0, 0, 981, 506);

		if (Main.isWindows())
			setBackground(Color.white);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel.setBounds(10, 11, 228, 449);
		panel.setLayout(null);
		if (Main.isWindows())
			panel.setBackground(Color.white);
		add(panel);
		
		lblBrokerAddress = new RLabel(Main.resourceManager.getString("ui.label.mqtt.broker"));
		lblBrokerAddress.setBounds(10, 11, 179, 14);
		panel.add(lblBrokerAddress);
		
		txtAddress = new RTextField();
		txtAddress.setBounds(10, 29, 208, 20);
		txtAddress.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(txtAddress);
		txtAddress.setColumns(10);
		txtAddress.addChangeListener((e) -> {
			String text = txtAddress.getText();
			if (text.contains("://")) {
				cmbProtocol.setEnabled(false);
				text = text.toLowerCase();
				if (text.startsWith("tcp://"))
					cmbProtocol.setSelectedIndex(0);
				else if (text.startsWith("ws://"))
					cmbProtocol.setSelectedIndex(1);
				else if (text.startsWith("wss://"))
					cmbProtocol.setSelectedIndex(2);
			}
			else
				cmbProtocol.setEnabled(true);
		});
		
		lblProtocol = new RLabel(Main.resourceManager.getString("ui.label.mqtt.protocol"));
		lblProtocol.setBounds(10, 60, 179, 14);
		panel.add(lblProtocol);
		
		cmbProtocol = new JComboBox<>();
		cmbProtocol.setBounds(10, 78, 208, 20);
		cmbProtocol.addItem(Main.resourceManager.getString("ui.label.mqtt.tcp"));
		cmbProtocol.addItem(Main.resourceManager.getString("ui.label.mqtt.ws"));
		cmbProtocol.addItem(Main.resourceManager.getString("ui.label.mqtt.wss"));
		panel.add(cmbProtocol);
		
		lblUsername = new RLabel(Main.resourceManager.getString("ui.label.mqtt.username"));
		lblUsername.setBounds(10, 109, 179, 14);
		panel.add(lblUsername);
		
		txtUsername = new RTextField();
		txtUsername.setColumns(10);
		txtUsername.setBounds(10, 127, 208, 20);
		txtUsername.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(txtUsername);
		
		lblPassword = new RLabel(Main.resourceManager.getString("ui.label.mqtt.password"));
		lblPassword.setBounds(10, 158, 179, 14);
		panel.add(lblPassword);
		
		txtPassword = new RPasswordField();
		txtPassword.setColumns(10);
		txtPassword.setBounds(10, 176, 208, 20);
		txtPassword.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(txtPassword);
		
		lblMqttId = new RLabel(Main.resourceManager.getString("ui.label.mqtt.id"));
		lblMqttId.setBounds(10, 207, 179, 14);
		panel.add(lblMqttId);
		
		txtMqttId = new RTextField();
		txtMqttId.setHorizontalAlignment(SwingConstants.LEFT);
		txtMqttId.setColumns(10);
		txtMqttId.setBounds(10, 225, 208, 20);
		panel.add(txtMqttId);
		txtMqttId.setText(myId);
		
		btnConnect = new JButton(Main.resourceManager.getString("ui.label.mqtt.connect"));
		btnConnect.setBounds(129, 415, 89, 23);
		btnConnect.addActionListener((e) -> connect());
		panel.add(btnConnect);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setBounds(248, 11, 228, 449);
		add(tabbedPane);

		JPanel tabManage = new JPanel();
		if (Main.isWindows())
			tabManage.setBackground(new Color(255, 255, 255));
		tabbedPane.addTab(Main.resourceManager.getString("ui.label.mqtt.manage"), null, tabManage, null);
		tabManage.setLayout(null);
		
		RGroupBox panel_2 = new RGroupBox();
		panel_2.setText(Main.resourceManager.getString("ui.label.mqtt.shutdown"));
		panel_2.setLayout(null);
		panel_2.setBounds(10, 11, 201, 98);
		tabManage.add(panel_2);
		
		btnSendShutdown = new JButton(Main.resourceManager.getString("ui.label.mqtt.send"));
		btnSendShutdown.setEnabled(false);
		btnSendShutdown.setLocation(119, 64);
		btnSendShutdown.setSize(72, 23);
		panel_2.add(btnSendShutdown);
		btnSendShutdown.addActionListener((e) -> sendShutdown());
		
		txtShutdownId = new RTextField();
		txtShutdownId.setEnabled(false);
		txtShutdownId.setBounds(10, 33, 181, 20);
		txtShutdownId.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(txtShutdownId);
		txtShutdownId.setColumns(10);
		
		lblClientId = new RLabel(Main.resourceManager.getString("ui.label.mqtt.id"));
		lblClientId.setEnabled(false);
		lblClientId.setBounds(10, 15, 46, 14);
		panel_2.add(lblClientId);
		
		RGroupBox groupBox = new RGroupBox();
		groupBox.setLayout(null);
		groupBox.setText(Main.resourceManager.getString("ui.label.mqtt.restart"));
		groupBox.setBounds(10, 120, 201, 98);
		tabManage.add(groupBox);
		
		btnSendReboot = new JButton(Main.resourceManager.getString("ui.label.mqtt.send"));
		btnSendReboot.setEnabled(false);
		btnSendReboot.setBounds(119, 64, 72, 23);
		groupBox.add(btnSendReboot);
		btnSendReboot.addActionListener((e) -> sendReboot());
		
		txtRebootId = new RTextField();
		txtRebootId.setEnabled(false);
		txtRebootId.setColumns(10);
		txtRebootId.setBounds(10, 33, 181, 20);
		txtRebootId.setHorizontalAlignment(SwingConstants.LEFT);
		groupBox.add(txtRebootId);
		
		lblRebootId = new RLabel(Main.resourceManager.getString("ui.label.mqtt.id"));
		lblRebootId.setEnabled(false);
		lblRebootId.setBounds(10, 15, 46, 14);
		groupBox.add(lblRebootId);
		
		btnReportActive = new JButton(Main.resourceManager.getString("ui.label.mqtt.report"));
		btnReportActive.setEnabled(false);
		btnReportActive.setBounds(106, 385, 105, 23);
		btnReportActive.addActionListener((e) -> reportIn());
		tabManage.add(btnReportActive);
		
		RGroupBox groupBox_1 = new RGroupBox();
		groupBox_1.setLayout(null);
		groupBox_1.setText(Main.resourceManager.getString("ui.label.mqtt.jobs"));
		groupBox_1.setBounds(10, 229, 201, 128);
		tabManage.add(groupBox_1);
		
		btnListJobs = new JButton("Send");
		btnListJobs.setEnabled(false);
		btnListJobs.setBounds(119, 95, 72, 23);
		btnListJobs.addActionListener((e) -> getJobList());
		groupBox_1.add(btnListJobs);
		
		txtJobListId = new RTextField();
		txtJobListId.setEnabled(false);
		txtJobListId.setColumns(10);
		txtJobListId.setBounds(10, 33, 181, 20);
		txtJobListId.setHorizontalAlignment(SwingConstants.LEFT);
		groupBox_1.add(txtJobListId);
		
		lblJobListId = new RLabel("Client ID");
		lblJobListId.setEnabled(false);
		lblJobListId.setBounds(10, 15, 46, 14);
		groupBox_1.add(lblJobListId);
		
		cmbJobListFilter = new JComboBox<>();
		cmbJobListFilter.setEnabled(false);
		cmbJobListFilter.setBounds(10, 64, 181, 20);
		groupBox_1.add(cmbJobListFilter);
		
		cmbJobListFilter.addItem(Main.resourceManager.getString("ui.label.mqtt.all"));
		cmbJobListFilter.addItem(Main.resourceManager.getString("ui.label.mqtt.running"));
		cmbJobListFilter.addItem(Main.resourceManager.getString("ui.label.mqtt.queue"));
		cmbJobListFilter.addItem(Main.resourceManager.getString("ui.label.mqtt.complete"));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_1.setBounds(486, 11, 475, 234);
		add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		messageList = new JList<>();
		messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messageList.setLayoutOrientation(JList.VERTICAL);
		messageList.setVisibleRowCount(-1);
		messageList.addListSelectionListener((e) -> {
			if (messageList.getSelectedIndex() < 0)
				txtPayload.setText("");
			else {
				MqttData data = null;
				synchronized (mqttMessages) {
					data = (MqttData)mqttMessages.get(messageList.getSelectedIndex());
				}
				if (data.payload == null || data.payload.length == 0)
					txtPayload.setText("NO PAYLOAD");
				else {
					try {
						Object mapped = mapper.readValue(data.payload, Object.class);
						txtPayload.setText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapped));
						txtPayload.setCaretPosition(0);
					}
					catch (IOException ex) {
						txtPayload.setText("FAILED TO PARSE PAYLOAD:\r\n" + ex.getMessage());
					}
				}
			}
		});
		messageList.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int index = messageList.locationToIndex(e.getPoint());
					if (index >= 0 && messageList.getCellBounds(index, index).contains(e.getPoint())) {
						messageList.setSelectedIndex(index);
						JPopupMenu menu = new JPopupMenu();
						JMenuItem item = new JMenuItem("Copy Topic");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null) {
								StringSelection selection = new StringSelection(data.topic);
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
							}
						});
						menu.add(item);
						menu.addSeparator();
						item = new JMenuItem("Copy Topic Root");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null) {
								StringSelection selection = new StringSelection(data.getTopicRoot());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
							}
						});
						menu.add(item);
						item = new JMenuItem("Copy Sender ID");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null) {
								StringSelection selection = new StringSelection(data.getSenderId());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
							}
						});
						menu.add(item);
						item = new JMenuItem("Copy Target ID");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null) {
								StringSelection selection = new StringSelection(data.getReceiverId());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
							}
						});
						menu.add(item);
						item = new JMenuItem("Copy Type");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null) {
								StringSelection selection = new StringSelection(data.getMessageType());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
							}
						});
						menu.add(item);
						menu.addSeparator();
						item = new JMenuItem("Copy Payload");
						item.addActionListener(f -> {
							MqttData data = null;
							synchronized (mqttMessages) {
								data = (MqttData)mqttMessages.get(index);
							}
							if (data != null && data.payload != null && data.payload.length > 0) {
								try {
									Object mapped = mapper.readValue(data.payload, Object.class);
									String payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapped);
									StringSelection selection = new StringSelection(payload);
									Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
									clipboard.setContents(selection, selection);
								}
								catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						});
						menu.add(item);
						menu.show(messageList, e.getPoint().x, e.getPoint().y);
					}
				}
			}
		});
		
		mqttMessages = new DefaultListModel<>();
		messageList.setModel(mqttMessages);
		
		JScrollPane listScroll = new JScrollPane(messageList);
		panel_1.add(listScroll);
		
		txtPayload = new RTextArea();
		txtPayload.setEditable(false);
		
		JScrollPane payloadScroll = new JScrollPane(txtPayload);
		payloadScroll.setBounds(486, 287, 475, 173);
		add(payloadScroll);
		
		btnClear = new JButton("Clear");
		btnClear.setBounds(872, 253, 89, 23);
		btnClear.setEnabled(false);
		btnClear.addActionListener((e) -> {
			txtPayload.setText("");
			synchronized (mqttMessages) {
				mqttMessages.removeAllElements();
			}
		});
		add(btnClear);
	}
	
	private void connect() {
		//connect
		if (client == null) {
			String address = txtAddress.getText();
	        if (!address.startsWith("tcp://") && !address.startsWith("ws://") && !address.startsWith("wss://")) {
	        	switch (cmbProtocol.getSelectedIndex()) {
	        	case 0:
	        		address = "tcp://" + address;
	        		break;
	        	case 1:
	        		address = "ws://" + address;
	        		break;
	        	case 2:
	        		address = "wss://" + address;
	        		break;
	        	}
	        }
	        final String connectTo = address;
	        final String username = txtUsername.getText();
	        final String password = txtPassword.getText();
	        busyDialog = new BusyDialog(app.getForm());
			SwingUtilities.invokeLater(() -> {
				synchronized (this) {
					if (busyDialog != null)
						busyDialog.setVisible(true);
				}
			});
			new Thread(() -> {
				try {
					client = new MqttAsyncClient(connectTo, myId);
					MqttConnectOptions options = new MqttConnectOptions();
					if (username.length() > 0)
						options.setUserName(username);
					if (password.length() > 0)
						options.setPassword(password.toCharArray());
					client.setCallback(MqttTab.this);
					client.connect(options, null, MqttTab.this);
				}
				catch (MqttException e) {
				}
			}).start();
		}
		//disconnect
		else
			disconnect();
	}
	
	private void disconnect() {
		synchronized (this) {
			if (client != null && client.isConnected()) {
		        busyDialog = new BusyDialog(app.getForm());
				SwingUtilities.invokeLater(() -> {
					synchronized (this) {
						if (busyDialog != null)
							busyDialog.setVisible(true);
					}
				});
				try {
					IMqttToken token = client.disconnect();
					token.setUserContext("shutdown");
					token.setActionCallback(this);
				}
				catch (MqttException e) {
					REDappLogger.error("Unable to disconnect from the MQTT broker", e);
				}
			}
		}
	}
	
	@Override
	public void onClosing() {
		super.onClosing();
		if (client != null && client.isConnected()) {
			try {
				IMqttToken token = client.disconnect();
				token.waitForCompletion();
				client.close();
				client = null;
			}
			catch (MqttException e) {
				REDappLogger.error("Unable to disconnect from the MQTT broker", e);
			}
		}
	}
	
	public String buildTopic(String to, String message) {
		String topic = "psaas";
		topic += "/";
		topic += myId;
		topic += "/";
		topic += to;
		topic += "/";
		topic += message;
		return topic;
	}
	
	private void reportIn() {
		new Thread(() -> {
			synchronized (locker) {
				try {
					String topic = buildTopic("broadcast", "reportin");
					client.publish(topic, new byte[0], 0, false);
				}
				catch (MqttException e) {
					REDappLogger.error("Unable to send checkin request", e);
				}
			}
		}).start();
	}
	
	private void getJobList() {
		final String clientId = txtJobListId.getText();
		final int filter = cmbJobListFilter.getSelectedIndex();
		if (clientId.length() > 0) {
			new Thread(() -> {
				synchronized (locker) {
					try {
						String filterString;
						switch (filter) {
						case 1:
							filterString = "run";
							break;
						case 2:
							filterString = "queue";
							break;
						case 3:
							filterString = "complete";
							break;
						default:
							filterString = "all";
							break;
						}
						String topic = buildTopic(clientId, "requesthistory");
						ObjectNode payload = mapper.createObjectNode();
						payload.put("filter", filterString);
						client.publish(topic, mapper.writer().writeValueAsBytes(payload), 0, false);
					}
					catch (MqttException | JsonProcessingException e) {
						REDappLogger.error("Unable to send list job request", e);
					}
				}
			}).start();
		}
	}
	
	private void sendShutdown() {
		final String clientId = txtShutdownId.getText();
		if (clientId.length() > 0) {
			new Thread(() -> {
				synchronized (locker) {
					try {
						String topic = buildTopic(clientId, "manage");
						ObjectNode payload = mapper.createObjectNode();
						payload.put("request", "shutdown");
						client.publish(topic, mapper.writer().writeValueAsBytes(payload), 0, false);
					}
					catch (MqttException | JsonProcessingException e) {
						REDappLogger.error("Unable to send shutdown request", e);
					}
				}
			}).start();
		}
	}
	
	private void sendReboot() {
		final String clientId = txtRebootId.getText();
		if (clientId.length() > 0) {
			new Thread(() -> {
				synchronized (locker) {
					try {
						String topic = buildTopic(clientId, "manage");
						ObjectNode payload = mapper.createObjectNode();
						payload.put("request", "reboot");
						client.publish(topic, mapper.writer().writeValueAsBytes(payload), 0, false);
					}
					catch (MqttException | JsonProcessingException e) {
						REDappLogger.error("Unable to send reboot request", e);
					}
				}
			}).start();
		}
	}
	
	private void setConnected(final boolean connected) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				synchronized (this) {
					if (connected) {
						btnConnect.setText(Main.resourceManager.getString("ui.label.mqtt.disconnect"));
					}
					else {
						btnConnect.setText(Main.resourceManager.getString("ui.label.mqtt.connect"));
					}
					if (busyDialog != null) {
						busyDialog.setVisible(false);
						busyDialog = null;
					}
					boolean notConnected = !connected;
					lblBrokerAddress.setEnabled(notConnected);
					txtAddress.setEnabled(notConnected);
					lblProtocol.setEnabled(notConnected);
					cmbProtocol.setEnabled(notConnected);
					if (notConnected)
						txtAddress.setText(txtAddress.getText());
					lblUsername.setEnabled(notConnected);
					txtUsername.setEnabled(notConnected);
					lblPassword.setEnabled(notConnected);
					txtPassword.setEnabled(notConnected);
					txtMqttId.setEnabled(notConnected);
					
					lblClientId.setEnabled(connected);
					txtShutdownId.setEnabled(connected);
					btnSendShutdown.setEnabled(connected);
					lblRebootId.setEnabled(connected);
					txtRebootId.setEnabled(connected);
					btnSendReboot.setEnabled(connected);
					btnReportActive.setEnabled(connected);
					lblJobListId.setEnabled(connected);
					txtJobListId.setEnabled(connected);
					btnListJobs.setEnabled(connected);
					cmbJobListFilter.setEnabled(connected);
					btnClear.setEnabled(connected);
				}
			});
		}
		catch (InvocationTargetException | InterruptedException e) {
		}
	}

	@Override
	public void setInternetConnected(boolean conn) { }

	@Override
	public void settingsUpdated() { }

	@Override
	public void reset() { }

	@Override
	public boolean supportsReset() {
		return false;
	}

	@Override
	public void onLocationChanged() { }

	@Override
	public void onTimeZoneChanged() { }

	@Override
	public void onDateChanged() { }

	@Override
	public void onCurrentTabChanged() { }

	@Override
	public void connectionLost(Throwable arg0) {
		setConnected(client != null && client.isConnected());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) { }

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		final MqttData data = new MqttData();
		data.topic = arg0;
		data.arrival = LocalDateTime.now();
		data.payload = arg1.getPayload();
		SwingUtilities.invokeLater(() -> {
			synchronized (mqttMessages) {
				mqttMessages.add(0, data);
			}
		});
	}

	@Override
	public void onFailure(IMqttToken arg0, final Throwable arg1) {
		SwingUtilities.invokeLater(() -> {
			if (busyDialog != null) {
				busyDialog.setVisible(false);
				busyDialog = null;
			}
			JOptionPane.showMessageDialog(app.getForm(), "Failed to connect to MQTT broker: " + arg1.getLocalizedMessage());
		});
		try {
			client.close();
			client = null;
		}
		catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSuccess(IMqttToken arg0) {
		if (arg0.getUserContext() != null && arg0.getUserContext() instanceof String && ((String)arg0.getUserContext()).equals("shutdown")) {
			try {
				client.close();
				client = null;
			}
			catch (MqttException e) {
				e.printStackTrace();
			}
		}
		else {
			SwingUtilities.invokeLater(() -> {
				synchronized (this) {
					prefs.put("mqtt_address", client.getServerURI());
					prefs.putInt("mqtt_protocol", cmbProtocol.getSelectedIndex());
					prefs.put("mqtt_username", txtUsername.getText());
				}
			});
			try {
				client.subscribe("psaas/#", 0);
			}
			catch (MqttException e) {
				e.printStackTrace();
			}
		}
		setConnected(client != null && client.isConnected());
	}
	
	private static class MqttData {
		public String topic;
		
		public LocalDateTime arrival;
		
		public String getTopicRoot() {
			String[] split = topic.split("/");
			if (split.length >= 4) {
				return split[0];
			}
			return "";
		}
		
		public String getSenderId() {
			String[] split = topic.split("/");
			if (split.length >= 4) {
				return split[1];
			}
			return "";
		}
		
		public String getReceiverId() {
			String[] split = topic.split("/");
			if (split.length >= 4) {
				return split[2];
			}
			return "";
		}
		
		public String getMessageType() {
			String[] split = topic.split("/");
			if (split.length >= 4) {
				return split[3];
			}
			return "";
		}
		
		public byte[] payload;
		
		@Override
		public String toString() {
			return arrival.format(DateTimeFormatter.ISO_DATE_TIME) + ": " + topic;
		}
	}
}
