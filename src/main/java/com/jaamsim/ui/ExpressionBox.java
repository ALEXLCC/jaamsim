/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2018 JaamSim Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jaamsim.Commands.KeywordCommand;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.Graphics.EntityLabel;
import com.jaamsim.Graphics.OverlayEntity;
import com.jaamsim.Graphics.Region;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.KeywordIndex;
import com.jaamsim.input.Parser;

public class ExpressionBox extends JDialog {

	private final Input<?> input;
	private final JTextArea editArea;
	private final JTextField msgText;
	private final JButton acceptButton;
	private final JButton cancelButton;
	private int result;

    public static final int CANCEL_OPTION = 1;  // Cancel button is clicked
    public static final int APPROVE_OPTION = 0; // Accept button is clicked
    public static final int ERROR_OPTION = -1;  // Error occurs or the dialog is dismissed

    private final Insets noMargin = new Insets( 0, 0, 0, 0 );
    private final Insets smallMargin = new Insets( 1, 1, 1, 1 );
    private final Dimension separatorDim = new Dimension(11, 20);
    private final Dimension gapDim = new Dimension(5, separatorDim.height);

	public ExpressionBox(Input<?> in, String str) {
		super((JDialog)null, "Expression Builder", true);

		// Button bar
		JToolBar buttonBar = new JToolBar();
		buttonBar.setMargin(smallMargin);
		buttonBar.setFloatable(false);
		buttonBar.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );

		buttonBar.add(Box.createRigidArea(gapDim));
		addOperatorButtons(buttonBar);
		buttonBar.addSeparator(separatorDim);
		addObjectButtons(buttonBar);
		getContentPane().add( buttonBar, BorderLayout.NORTH );

		// Initial text
		input = in;
		ArrayList<String> tokens = new ArrayList<>();
		Parser.tokenize(tokens, str, true);

		// Dialog box
		setPreferredSize(new Dimension(800, 300));
		setIconImage(GUIFrame.getWindowIcon());
		setAlwaysOnTop(true);

		// Input text
		editArea = new JTextArea();
		editArea.setFont(UIManager.getDefaults().getFont("TabbedPane.font"));
		editArea.setText(Input.getValueString(tokens, true));
		JScrollPane scrollPane = new JScrollPane(editArea);
		scrollPane.setBorder(new EmptyBorder(10, 10, 0, 10));
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		// Error message text
		JLabel msgLabel = new JLabel( "Message:" );
		msgText = new JTextField("", 60);
		msgText.setEditable(false);

		// Buttons
		acceptButton = new JButton("Accept");
		cancelButton = new JButton("Cancel");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new FlowLayout(FlowLayout.CENTER) );
		buttonPanel.add(msgLabel);
		buttonPanel.add(msgText);
		buttonPanel.add(acceptButton);
		buttonPanel.add(cancelButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		pack();

		// Window closed event
		this.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent e ) {
				result = ERROR_OPTION;
				setVisible(false);
				undoEdits();
				dispose();
			}
		} );

		// Accept button
		acceptButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				result = APPROVE_OPTION;
				setVisible(false);
				dispose();
			}
		} );

		// Cancel button
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				result = CANCEL_OPTION;
				setVisible(false);
				undoEdits();
				dispose();
			}
		} );

		// Listen for changes to the text
		editArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				tryParse();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				tryParse();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
	    });
	}

	private void addOperatorButtons(JToolBar buttonBar) {

		// Operators label
		buttonBar.add( new JLabel("Operators:") );

		// "+" operator button
		JButton addButton = new JButton( "+" );
		addButton.setMargin(noMargin);
		addButton.setToolTipText(GUIFrame.formatToolTip(
				"+ operator", "Numbers: add the second number to the first.\n"
						+ "Strings: concatenates the second string to the first."));
		addButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" + ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( addButton );

		// "-" operator button
		JButton minusButton = new JButton( "-" );
		minusButton.setMargin(noMargin);
		minusButton.setToolTipText(GUIFrame.formatToolTip(
				"- operator", "Subtracts the second number from the first."));
		minusButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" - ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( minusButton );

		// "*" operator button
		JButton multButton = new JButton( "*" );
		multButton.setMargin(noMargin);
		multButton.setToolTipText(GUIFrame.formatToolTip(
				"* operator", "Multiplies the first number by the second."));
		multButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" * ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( multButton );

		// "/" operator button
		JButton divButton = new JButton( "/" );
		divButton.setMargin(noMargin);
		divButton.setToolTipText(GUIFrame.formatToolTip(
				"/ operator", "Divides the first number by the second."));
		divButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" / ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( divButton );

		// "^" operator button
		JButton expButton = new JButton( "^" );
		expButton.setMargin(noMargin);
		expButton.setToolTipText(GUIFrame.formatToolTip(
				"^ operator", "Raises the first number to the power the second."));
		expButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" ^ ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( expButton );

		// "?" operator button
		JButton condButton = new JButton( "?" );
		condButton.setMargin(noMargin);
		condButton.setToolTipText(GUIFrame.formatToolTip(
				"? operator", "The conditional or ternary operator takes three arguments. "
						+ "The first argument is a number that represents a Boolean condition "
						+ "(zero = false, non-zero = true). "
						+ "The second and third arguments can be numbers, strings, entities, "
						+ "etc. as long as both are the same type. "
						+ "The second argument is returned if the condition is true. "
						+ "The third argument is returned if the condition is false."));
		condButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				editArea.insert(" ? (  ) : (  ) ", editArea.getCaretPosition());
				editArea.requestFocusInWindow();
				editArea.setCaretPosition(editArea.getCaretPosition() - 10);
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( condButton );
	}

	private void addObjectButtons(final JToolBar buttonBar) {

		// Objects label
		buttonBar.add( new JLabel("Objects:") );

		// Entity button
		final JButton entityButton = new JButton( "Entity" );
		entityButton.setMargin(noMargin);
		entityButton.setToolTipText(GUIFrame.formatToolTip(
				"Entity", "Inserts a selected entity."));
		entityButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
				JPopupMenu entityMenu = new JPopupMenu();
				ArrayList<String> entNameList = new ArrayList<>();
				for (DisplayEntity each: Entity.getClonesOfIterator(DisplayEntity.class)) {
					if (each.testFlag(Entity.FLAG_GENERATED))
						continue;

					if (each instanceof OverlayEntity || each instanceof Region || each instanceof EntityLabel)
						continue;

					entNameList.add(each.getName());
				}
				Collections.sort(entNameList, Input.uiSortOrder);

				for (final String entName : entNameList) {
					JMenuItem item = new JMenuItem(entName);
					item.addActionListener( new ActionListener() {

						@Override
						public void actionPerformed( ActionEvent event ) {
							String str = String.format("[%s]", entName);
							editArea.insert(str, editArea.getCaretPosition());
							editArea.requestFocusInWindow();
						}
					} );
					entityMenu.add(item);
				}
				entityMenu.show(entityButton, 0, buttonBar.getPreferredSize().height);
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( entityButton );

		// Unit button
		JButton unitButton = new JButton( "Unit" );
		unitButton.setMargin(noMargin);
		unitButton.setToolTipText(GUIFrame.formatToolTip(
				"Unit", "Inserts a selected unit."));
		unitButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent event ) {
			}
		} );
		buttonBar.add(Box.createRigidArea(gapDim));
		buttonBar.add( unitButton );
	}

	private void tryParse() {
		try {
			Entity ent = EditBox.getInstance().getCurrentEntity();
			String str = editArea.getText().replace("\n", " ");
			ArrayList<String> tokens = new ArrayList<>();
			Parser.tokenize(tokens, str, true);
			KeywordIndex kw = new KeywordIndex(input.getKeyword(), tokens, null);
			InputAgent.storeAndExecute(new KeywordCommand(ent, kw));
			msgText.setText("");
			acceptButton.setEnabled(true);
		}
		catch (Exception e) {
			msgText.setText(e.getMessage());
			acceptButton.setEnabled(false);
		}
	}

	private void undoEdits() {
		InputAgent.undo();
	}

	public int showDialog() {

		// Position the editor at the centre of the screen
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setLocation((winSize.width - getWidth())/2, (winSize.height - getHeight())/2);

		// Show the dialog box and wait for editing to finish
		this.setVisible(true);

		// Return how the editing was completed
		return result;
	}

	public String getInputString() {
		return editArea.getText();
	}

}
