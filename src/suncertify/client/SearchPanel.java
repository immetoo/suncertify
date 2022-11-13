/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */


package suncertify.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import suncertify.models.HotelRoom;

/**
 * 
 * @author willemc
 *
 */
public class SearchPanel implements ActionListener,DocumentListener {

	private JPanel searchPanel = null;
	private JButton searchButton = null;
	private JButton clearButton = null;
	private HotelRoomTableModel model = null;
	private JTextField f0;
	private JTextField f1;
	private JTextField f2;
	private JTextField f3;
	
	public SearchPanel(HotelRoomTableModel model) {
		this.model=model;
		searchPanel = new JPanel();
		searchPanel.setLayout(new SpringLayout());
		TitledBorder titledBorder = BorderFactory.createTitledBorder("Search");
		titledBorder.setTitleJustification(TitledBorder.LEFT);
        titledBorder.setTitlePosition(TitledBorder.TOP);
        searchPanel.setBorder(titledBorder);
		createPanel();
	}
	
	public JComponent getJComponent() {
		return searchPanel;
	}
	
	
	private void createPanel() {
		
		JPanel leftPanel = searchPanel;
		
        JLabel l0 = new JLabel();
        l0.setHorizontalAlignment(JLabel.TRAILING);
        l0.setText("Custumber Number:");
        leftPanel.add(l0);
        
        f0 = new JTextField(15);
        f0.getDocument().addDocumentListener(this);
        leftPanel.add(f0);
        
        JLabel l1 = new JLabel();
        l1.setHorizontalAlignment(JLabel.TRAILING);
        l1.setText("Name:");
        leftPanel.add(l1);
        
        f1 = new JTextField(15);
        f1.getDocument().addDocumentListener(this);
        leftPanel.add(f1);
        
        JLabel l2 = new JLabel();
        l2.setHorizontalAlignment(JLabel.TRAILING);
        l2.setText("Location:");
        leftPanel.add(l2);
        
        f2 = new JTextField(15);
        f2.getDocument().addDocumentListener(this);
        leftPanel.add(f2);
        
        //searchPanel.add(leftPanel,BorderLayout.LINE_START);
        
        // === Center
        
        JPanel centerPanel = searchPanel;
        
        JLabel l3 = new JLabel();
        l3.setHorizontalAlignment(JLabel.TRAILING);
        l3.setText("Size:");
        centerPanel.add(l3);
        
        class IntTextDocument extends PlainDocument {
			private static final long	serialVersionUID	= 7151907649485446049L;

			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        	    if (str == null) {
        	      return;
        	    }
        	    String oldString = getText(0, getLength());
        	    String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
        	    try {
        	      Integer.parseInt(newString + "0");
        	      super.insertString(offs, str, a);
        	    } catch (NumberFormatException e) {
        	    }
        	  }
        	}
        
        f3 = new JTextField(new IntTextDocument(),null,15);
        f3.getDocument().addDocumentListener(this);
        centerPanel.add(f3);
        
        
        JLabel l4 = new JLabel();
        l4.setHorizontalAlignment(JLabel.TRAILING);
        l4.setText("Smoking:");
        centerPanel.add(l4);
        
        JCheckBox c1 = new JCheckBox();
        centerPanel.add(c1);
        
        JLabel l5 = new JLabel();
        l5.setHorizontalAlignment(JLabel.TRAILING);
        l5.setText("Price:");
        centerPanel.add(l5);
        
        //JTextField f4 = new JTextField(15);
        //f4.getDocument().addDocumentListener(this);
        
        String labels[] = { "10", "20", "50", "100","150", "200", "300", "400","500", "800" };
        JComboBox comboBox = new JComboBox(labels);
        comboBox.setMaximumRowCount(5);
        comboBox.setEditable(true);
        
        centerPanel.add(comboBox);
        
        
        JLabel l6 = new JLabel();
        l6.setHorizontalAlignment(JLabel.TRAILING);
        l6.setText("Date:");
        centerPanel.add(l6);
        
        JTextField f5 = new JTextField(15);
        //f4.getDocument().addDocumentListener(this);
        centerPanel.add(f5);
        
        //searchPanel.add(centerPanel,BorderLayout.CENTER);
        
        
        // South
        
        JLabel l7 = new JLabel();
        centerPanel.add(l7);
        JLabel l8 = new JLabel();
        centerPanel.add(l8);
        
        JPanel buttonPane = searchPanel;
        //buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        //buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        //buttonPane.add(Box.createHorizontalGlue());
        
       
        searchButton = new JButton();
        searchButton.setText("Search");
        searchButton.addActionListener(this);
        
        buttonPane.add(searchButton);
        //buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        
        clearButton = new JButton();
        clearButton.setText("Clear");
        clearButton.addActionListener(this);
        
        buttonPane.add(clearButton);

        //searchPanel.add(buttonPane,BorderLayout.SOUTH);
        
        //Layout the panel.
        makeCompactGrid(searchPanel,
                                        3, 6, 		//rows,  cols
                                        6, 6,       //initX, initY
                                        6, 6);      //xPad,  yPad
	}
	
	private void updateSearch() {
		HotelRoom room = new HotelRoom();
		room.setName(f1.getText());
		room.setLocation(f2.getText());
		
		model.updateByCriteria(room);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==searchButton) {
			updateSearch();
		}
		if (e.getSource()==clearButton) {
			f0.setText(null);
			f1.setText(null);
			f2.setText(null);
			f3.setText(null);
			updateSearch();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		//updateSearch();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		updateSearch();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		updateSearch();
	}
	
	
	public static void makeCompactGrid(Container parent,
            int rows, int cols,
            int initialX, int initialY,
            int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}
		
		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}
		
		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
	
	 private static SpringLayout.Constraints getConstraintsForCell(
             int row, int col,
             Container parent,
             int cols) {
		 SpringLayout layout = (SpringLayout) parent.getLayout();
		 Component c = parent.getComponent(row * cols + col);
		 return layout.getConstraints(c);
	 }
}