package xal.app.newmtv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

class ButtonColumn extends AbstractCellEditor implements TableCellRenderer,
		TableCellEditor, ActionListener {
	private static final long serialVersionUID = 1L;
	protected JTable table;
	protected MTVWindow mywindow;
	protected JRadioButton renderButton;
	protected JRadioButton editButton;	
	protected String text;
	protected TunnerDialog _tunnerDialog;
	protected TableColumnModel columnModel;
	protected int selectedrow;

	public ButtonColumn(MTVWindow window, JTable table, int column) {		
		super();
		this.table = table;
		mywindow = window;
		renderButton = new JRadioButton();
		editButton = new JRadioButton();
		editButton.setFocusPainted(false);
		editButton.addActionListener(this);

		columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (hasFocus) {
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
		} else if (isSelected) {
			renderButton.setForeground(table.getSelectionForeground());
			renderButton.setBackground(table.getSelectionBackground());
		} else {
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
			// renderButton.setBackground(Color.WHITE);
		}

		renderButton.setText((value == null) ? " " : value.toString());
		return renderButton;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		selectedrow = row;
		//System.out.println("buttoncolum=================");
		//System.out.println("selectedrow: " + row);
		// editButton.setForeground(Color.RED);
		//editButton.setBackground(Color.MAGENTA);
		// editButton.setBackground(Color.PINK);
		text = (value == null) ? " " : value.toString();
		editButton.setText(text);
		return editButton;
	}

	public Object getCellEditorValue() {
		return text;
	}

	public void actionPerformed(ActionEvent e) {
		//System.out.println("********************");
		showTunnerDialog(selectedrow);// liyong add on 2016-1-6
		fireEditingStopped();
	}

	/** Show the solver config dialog */
	public void showTunnerDialog(int row) {
		editButton.setBackground(Color.MAGENTA);
		_tunnerDialog = new TunnerDialog(mywindow, row);
		// _tunnerDialog.setLocationRelativeTo(mywindow);
		// _tunnerDialog.setLocationRelativeTo(null);

		Toolkit kit = Toolkit.getDefaultToolkit();

		Dimension screenSize = kit.getScreenSize();

		int screenWidth = screenSize.width;

		int screenHeight = screenSize.height;

		int height = _tunnerDialog.getHeight();

		int width = _tunnerDialog.getWidth();

		_tunnerDialog.setLocation(1 * (screenWidth - width) / 5,
				3 * (screenHeight - height) / 4);

		_tunnerDialog.setVisible(true);
	}
}
