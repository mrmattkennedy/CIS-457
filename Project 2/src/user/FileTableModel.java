package user;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class FileTableModel extends AbstractTableModel {
	
	private ArrayList<String[]> files;
	private String[] columnNames = {"Speed", "Host", "Username", "Filename"};
	
	public FileTableModel() {
		files = new ArrayList<String[] >();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	@Override
	public int getRowCount() {
		return files.size();
	}
	
	public void repaintTable() 
	{
		fireTableRowsInserted(0, files.size() - 1);
		fireTableRowsUpdated(0, files.size() - 1);
	}
	
	public void addFile(String speed, String host, String file, String username)
	{
		for (int i = 0; i < files.size(); i++)
			if(files.get(i)[0].equals(speed) &&
					files.get(i)[1].equals(host) &&
					files.get(i)[2].equals(file) &&
					files.get(i)[3].equals(username))
				return;

		String[] temp = {speed, host, file, username};
		files.add(temp);
		repaintTable();
	}

	public void removeVariable(int index)
	{
		files.remove(index);
		repaintTable();
	}
	
	public void removeAllVariables() {
		files.clear();
	}

	@Override
	public Object getValueAt(int row, int column) {
		String[] file = files.get(row);
		return file[column];
	}

}
