/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */


package suncertify.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import suncertify.server.beans.HotelRoomManagerRemote;


/**
 * 
 * @author willemc
 *
 */
public class MainView {
	
	private HotelRoomManagerRemote hotelRoomManagerRemote = null;
	private JFrame frame = null;
	
	public MainView(HotelRoomManagerRemote hotelRoomManagerRemote) {
		if (hotelRoomManagerRemote==null) {
			throw new NullPointerException("Can't create MainView with null HotelRoomManager.");
		}
		this.hotelRoomManagerRemote=hotelRoomManagerRemote;
		
		frame = new JFrame();
		
		frame.setTitle("URLyBird HotelRoom Manager");
		//frame.setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/resources/images/logos/gabelfresser.gif")));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) { }
			public void windowClosed(WindowEvent e) { }
			public void windowClosing(WindowEvent e) { System.exit(0); }
			public void windowDeactivated(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
			public void windowOpened(WindowEvent e) { }
			});
		frame.pack();
		frame.setBounds(50,50,900,700);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		
		
		JMenuItem item3 = new JMenuItem("Connect");
		item3.setEnabled(false);
		fileMenu.add(item3);
		
		JMenuItem item2 = new JMenuItem("Preferences");
		item2.setEnabled(false);
		fileMenu.add(item2);
		
		fileMenu.addSeparator();
		
		JMenuItem item = new JMenuItem();
		item.setText("Quit");
		item.addActionListener(new ActionListener() {
			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(item);
		
		JMenu helpMenu = new JMenu("Help");
		JMenuItem help = new JMenuItem();
		help.setText("Help");
		help.setEnabled(false);
		helpMenu.add(help);
		
		helpMenu.addSeparator();
		
		JMenuItem itemAbout = new JMenuItem("About");
		itemAbout.setEnabled(false);
		helpMenu.add(itemAbout);
		
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);
		
		HotelRoomTableModel model = new HotelRoomTableModel(hotelRoomManagerRemote);
		SearchPanel searchPanel = new SearchPanel(model);
		
		frame.getContentPane().add(searchPanel.getJComponent(), BorderLayout.NORTH);
		
		JPanel southSide = new JPanel();
		southSide.setLayout(new BoxLayout(southSide, BoxLayout.LINE_AXIS));
		
		JButton searchButton = new JButton("Search [F1]");
		JButton addButton = new JButton("Add [F2]");
		JButton delButton = new JButton("Del [F3]");
		JButton editButton = new JButton("Edit [F4]");
		JButton bookButton = new JButton("Book [F5]");
		JButton exportButton = new JButton("Export [F6]");
		JButton helpButton = new JButton("Next [F7]");
		JButton printButton = new JButton("Print [F8]");
		
		searchButton.setEnabled(false);
		addButton.setEnabled(false);
		delButton.setEnabled(false);
		editButton.setEnabled(false);
		bookButton.setEnabled(false);
		exportButton.setEnabled(false);
		helpButton.setEnabled(false);
		printButton.setEnabled(false);
		
		southSide.add(searchButton);
		southSide.add(addButton);
		southSide.add(delButton);
		southSide.add(editButton);
		southSide.add(bookButton);
		southSide.add(exportButton);
		southSide.add(helpButton);
		southSide.add(printButton);
		
		frame.getContentPane().add(southSide, BorderLayout.SOUTH);
		
		// center
		
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableRowSorter sorter = new TableRowSorter(model);
		table.setRowSorter(sorter);
		
		table.getSelectionModel().addListSelectionListener(
		        new ListSelectionListener() {
		            public void valueChanged(ListSelectionEvent event) {
		                int viewRow = table.getSelectedRow();
		                if (viewRow < 0) {
		                    //Selection got filtered away.
		                    //statusText.setText("");
		                } else {
		                	System.out.println("Selected; "+viewRow+" ... ");
		                	//details.updateHotelRoom(model.getHotelRoom(viewRow));
		                }
		            }
		        });
		
		JScrollPane mainPanel = new JScrollPane();
		mainPanel.setWheelScrollingEnabled(true);
		mainPanel.setViewportView(table);
		mainPanel.getVerticalScrollBar().setUnitIncrement(10);
		mainPanel.getHorizontalScrollBar().setUnitIncrement(10);
		frame.getContentPane().add(mainPanel,BorderLayout.CENTER);
		
	}
	
	public void openView() {
		frame.setVisible(true);
	}
}