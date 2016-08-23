package xal.app.ringbetameasurement;

import java.util.*;

import javax.swing.table.AbstractTableModel;

public class BpmTableModel extends AbstractTableModel {
	static final long serialVersionUID = 0;
	List allBPMs;
	BetaPanel betaPanel;
	int rowSize = 0;
	
	private String[] columnNames;

	private Object[][] data;

	/** Container for row labels */
	private ArrayList rowNames = new ArrayList(rowSize);

	public BpmTableModel(List bpms, String[] colNames, BetaPanel panel) {
		allBPMs = bpms;
		rowSize = allBPMs.size();
		columnNames = colNames;
		data = new Object[rowSize][columnNames.length];
        betaPanel = panel;
	}
	
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public String getRowName(int row) {
		return (String) rowNames.get(row);
	}

	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		if (col < 5) {
			return false;
		} else {
			return true;
		}
	}

	/** method to add a row name */
	public void addRowName(String name, int row) {
		rowNames.add(row, name);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return (String) rowNames.get(rowIndex);
		} else {
			return data[rowIndex][columnIndex];
		}
	}

	public void setValueAt(Object value, int row, int col) {
		if (col > 4) {
			data[row][col] = (Object) value;
			if (betaPanel.bpmTable != null ) {
				// exclude the checked BPM
				if (((Boolean) value).booleanValue()) {
					if (!betaPanel.badBPMs.contains(new Integer(row)))
						betaPanel.badBPMs.add(new Integer(row));
				}
				// include the un-checked BPM
				else {
					if (betaPanel.badBPMs.contains(new Integer(row)))
						betaPanel.badBPMs.remove(new Integer(row));
				}
			} 
			
			
/*			if (tunePanel.bpmTable != null && ((Boolean) value).booleanValue()) {
				tunePanel.setSelectedBPM((String) getRowName(row));
				if (tunePanel.hasTune) {
					tunePanel.plotBPMData(row);
//					selectedBPMName.setText(selectedBPM);
				}
				for (int i = 0; i < tunePanel.bpmTable.getSelectedRow(); i++) {
					setValueAt(new Boolean(false), i, 5);
				}
				for (int i = tunePanel.bpmTable.getSelectedRow() + 1; i < allBPMs
						.size()
						&& i != tunePanel.bpmTable.getSelectedRow(); i++) {
					setValueAt(new Boolean(false), i, 5);
				}

			}
*/		} else if (col <= 4 && col > 0){
			data[row][col] = value;
		} else {
			data[row][col] = (String) value;
		}
		fireTableCellUpdated(row, col);
		return;
	}

}
