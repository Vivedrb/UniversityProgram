package custom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class PopupDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JButton okButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PopupDialog dialog = new PopupDialog("This is a test message");
			dialog.setTitle("Error!");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setMinimumSize(new Dimension(200, 90));
			dialog.pack();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PopupDialog(String message) {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel popupTextLabel = new JLabel(message);
		popupTextLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		popupTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(popupTextLabel);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
		okButton = new JButton("Ok");
		okButton.setBorder(new EmptyBorder(5, 15, 5, 15));
		okButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		okButton.setActionCommand("Ok");
		buttonPane.add(okButton);
		
		buttonListener();
		setModal(true);
	}
	
	private void buttonListener() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
}
