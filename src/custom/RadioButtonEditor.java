package custom;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;

public class RadioButtonEditor extends DefaultCellEditor implements ItemListener{
	private JRadioButton button;
	
	public RadioButtonEditor(JCheckBox checkBox) {
		super(checkBox);
		button = new JRadioButton();
		button.setOpaque(false);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if(value == null) {
			return null;
		}
		
		button = (JRadioButton) value;
		button.addItemListener(this);
		return (Component) value;
	}
	
	@Override
	public Object getCellEditorValue() {
		button.removeItemListener(this);
		return button;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
	}
}
