package admin.formlistener;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import admin.ModifyCourseForm;
import custom.CustomComboBox;
import custom.CustomTableModel;
import custom.PopupDialog;
import dbconnect.DBConnect;

public class ModifyCourseFormListener implements ActionListener{
	private JPanel formPane;
	private CustomTableModel tableModel;
	private JTextField courseIdTextField;
	private JTextField courseNameTextField;
	private CustomComboBox professorComboBox;
	private JComboBox<String> semesterComboBox;
	private JComboBox<String> dayComboBox;
	private JTextField startTimeTextField;
	private JTextField endTimeTextField;
	private JComboBox<String> startTimeComboBox;
	private JComboBox<String> endTimeComboBox;
	private JTextArea descriptionTextArea;
	private JComboBox<Integer> maxStudentsComboBox;
	
	
	
	public ModifyCourseFormListener(JPanel formPane, CustomTableModel tableModel, JTextField courseIdTextField, JTextField courseNameTextField, CustomComboBox professorComboBox, 
										JComboBox<String> semesterComboBox, JComboBox<String> dayComboBox, JTextField startTimeTextField, JTextField endTimeTextField, 
										JComboBox<String> startTimeComboBox, JComboBox<String> endTimeComboBox, JTextArea descriptionTextArea, JComboBox<Integer> maxStudentsComboBox) {
		this.formPane = formPane;
		this.tableModel = tableModel;
		this.courseIdTextField = courseIdTextField;
		this.courseNameTextField = courseNameTextField;
		this.professorComboBox = professorComboBox;
		this.semesterComboBox = semesterComboBox;
		this.dayComboBox = dayComboBox;
		this.startTimeTextField = startTimeTextField;
		this.endTimeTextField = endTimeTextField;
		this.startTimeComboBox = startTimeComboBox;
		this.endTimeComboBox = endTimeComboBox;
		this.descriptionTextArea = descriptionTextArea;
		this.maxStudentsComboBox = maxStudentsComboBox;
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!isAccountSelected()) {
			showPopupMessage("No Course Selected.", "Error!");
			return;
		}
		if(isFieldEmpty()) {
			showPopupMessage("One or MoreFields Are Empty.", "Error!");
			return;
		}
		if(!isInformationNew()) {
			showPopupMessage("No Changes Made.", "");
			return;
		}
		if(!isTimeInputValid(startTimeTextField.getText().toCharArray())) {
			showPopupMessage("Invalid Course Start Time", "Error!");
			return;
		}
		if(!isTimeInputValid(endTimeTextField.getText().toCharArray())) {
			showPopupMessage("Invalid Course End Time", "Error!");
			return;
		}
		if(doesCourseAlreadyExist()) {
			showPopupMessage("Course Already Exist in Time Slot.", "Error!");
			return;
		}
		boolean queryIsSuccessful = updateCourseToDB();
		if(queryIsSuccessful) {
			showPopupMessage("Course Successfuly Updated", "");
			updateTableModel();
		}
	}
	
	
	/*
	 * Helper Methods
	 */
	
	private boolean updateCourseToDB() {
		String courseId = courseIdTextField.getText();
		String name = courseNameTextField.getText();
		String semester = (String) semesterComboBox.getSelectedItem();
		String startTime = startTimeTextField.getText() + " " + startTimeComboBox.getSelectedItem();
		String endTime = endTimeTextField.getText() + " " + endTimeComboBox.getSelectedItem();
		String day = (String) dayComboBox.getSelectedItem();
		String description = descriptionTextArea.getText();
		String maxStudents = maxStudentsComboBox.getSelectedItem()+"";
		int professorId = professorComboBox.getSelectedItemId();
		
		try {
			Connection connection = DBConnect.connection;
			String query = String.format("UPDATE courses SET course_name = '%s', course_semester = '%s', start_time = '%s', end_time = '%s', " + 
											"course_day = '%s', course_description = '%s', max_students = '%s', professor_id = '%s' WHERE course_id = '%s'", 
											name, semester, startTime, endTime, day, description, maxStudents, professorId, courseId);
			Statement stm = connection.createStatement();
			int rows = stm.executeUpdate(query);
			
			stm.close();
			if(rows > 0) {
				return true;
			}
			return false;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean doesCourseAlreadyExist() {
		String id = courseIdTextField.getText();
		String semester = (String) semesterComboBox.getSelectedItem();
		String startTimeString = startTimeTextField.getText() + " " + startTimeComboBox.getSelectedItem();
		String endTimeString = endTimeTextField.getText() + " " + endTimeComboBox.getSelectedItem();
		String day = (String) dayComboBox.getSelectedItem();
		String professorId = professorComboBox.getSelectedItemId()+"";
		
		LocalTime newStartTime = getLocalTimeObject(startTimeString);
		LocalTime newEndTime = getLocalTimeObject(endTimeString);
		
		// courseList contains the start_time and end_time of each course with the same semester, day, and professor
		// as the course we are trying to modify.
		// If there are no courses with the same semester and day, we don't have to check if there's a time conflict.
		// Otherwise check if the start/end time conflict with other courses.
		List<String[]> courseList = getCoursesAtTime(id, semester, day, professorId);
		
		for(String[] course : courseList) {
			LocalTime startTime = getLocalTimeObject(course[0]);
			LocalTime endTime = getLocalTimeObject(course[1]);
			
			// If the time slot is before or after the existing course time slot, return false. Otherwise return true since there's a conflict.
			if(newStartTime.isBefore(startTime) && newEndTime.isBefore(startTime)) {
				return false;
			}
			if(newStartTime.isAfter(endTime) && newEndTime.isAfter(endTime)) {
				return false;
			}
			return true;
		}
		
		return false;
	}
	 
	// Check if the admin changed any course information.
	private boolean isInformationNew() {
		String id = courseIdTextField.getText();
		String name = courseNameTextField.getText();
		String semester = (String) semesterComboBox.getSelectedItem();
		String startTime = startTimeTextField.getText() + " " + startTimeComboBox.getSelectedItem();
		String endTime = endTimeTextField.getText() + " " + endTimeComboBox.getSelectedItem();
		String day = (String) dayComboBox.getSelectedItem();
		String description = descriptionTextArea.getText();
		String maxStudents = maxStudentsComboBox.getSelectedItem()+"";
		String professor = professorComboBox.getSelectedItemString();
		
		// rowData is the information of the course selected from the table
		// formData is the information of the course from the form that the admin can edit.
		String[] rowData = tableModel.getRowDataAtIndex(tableModel.getSelectedRowIndex());
		String[] formData = {id, name, semester, startTime, endTime, day, description, maxStudents, professor};
		
		for(int i = 0; i < rowData.length; i++) {
			if(!rowData[i].equals(formData[i])) {
				return true;
			}
		}
		return false;
		
	}
	
	private boolean isFieldEmpty() {
		Component[] components = formPane.getComponents();
		
		for(Component component : components) {
			if(component instanceof JTextField textField) {
				if(textField.getText().isBlank()) {
					return true;
				}
			}
			
			if(component instanceof JComboBox comboBox) {
				if(comboBox.getSelectedItem() == null) {
					return true;
				}
			}
		}
		
		// Return false if no courses were returned from the Database.
		return false;
	}
	
	private boolean isTimeInputValid(char[] timeCharArray) {
		for(char digit : timeCharArray) {
			if(digit == '-') {
				return false;
			}
		}
		
		String dateString = String.valueOf(timeCharArray);
	
		try {
			DateFormat sdf = new SimpleDateFormat("hh:mm");
			sdf.setLenient(false);
			sdf.parse(dateString);
		}
		catch (ParseException e) {
			return false;
		}
		return true;
	}
	
	// Retrieves all courses with the same semester, day, and professor as the one selected to modify
	// This is then used to check if there will be a time conflict when modifying the time.
	private List<String[]> getCoursesAtTime(String courseId, String semester, String day, String professorId) {
		List<String[]> courseList = new ArrayList<>();
		
		try {
			Connection connection = DBConnect.connection;
			String query = String.format("SELECT start_time, end_time FROM courses WHERE course_semester = '%s' AND course_day = '%s' AND professor_id = '%s' AND course_id != '%s'", 
											semester, day, professorId, courseId);
			Statement stm = connection.createStatement();
			ResultSet resultSet = stm.executeQuery(query);
			
			while(resultSet.next()) {
				courseList.add(new String[]{resultSet.getString("start_time"), resultSet.getString("end_time")});
			}
			stm.close();
			return courseList;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// Update the table on the admin view
	private void updateTableModel() {
		List<String[]> courseInfo = new ArrayList<>();
		
		try {
			Connection connection = DBConnect.connection;
			String query = String.format("SELECT *, professors.first_name, professors.last_name FROM courses, professors "
											+ "WHERE professors.professor_id = courses.professor_id ORDER BY course_id");
			Statement stm = connection.createStatement();
			ResultSet resultSet = stm.executeQuery(query);
			
			while(resultSet.next()) {
				String id = ""+resultSet.getInt("course_id");
				String courseName = resultSet.getString("course_name");
				String semester = resultSet.getString("course_semester");
				String startTime = resultSet.getString("start_time");
				String endTime = resultSet.getString("end_time");
				String day = resultSet.getString("course_day");
				String description = resultSet.getString("course_description");
				String maxStudents = ""+resultSet.getInt("max_students");
				String professorName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
				
				courseInfo.add(new String[]{"", id, courseName, semester, startTime, endTime, day, description, maxStudents, professorName});
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		tableModel.updateData(courseInfo.toArray(new String[0][0]));
		ButtonGroup buttonGroup = new ButtonGroup();
		
		for(int i = 0; i < tableModel.getRowCount(); i++) {
			JRadioButton radioButton = new JRadioButton();
			buttonGroup.add(radioButton);
			tableModel.setValueAt(radioButton, i, 0);
		}
		
		tableModel.fireTableDataChanged();
		clearTextFields();
	}
	
	private void clearTextFields() {
		for(Component component : formPane.getComponents()) {
			if(component instanceof JTextField textField) {
				textField.setText("");
			}
			if(component instanceof JComboBox comboBox) {
				comboBox.removeAllItems();
			}
		}
		
		// startTime/endTime text fields and combo boxes are in another JPanel
		// so they have to be removed manually
		descriptionTextArea.setText("");
		startTimeTextField.setText("");
		endTimeTextField.setText("");
		startTimeComboBox.removeAllItems();
		endTimeComboBox.removeAllItems();
	}
	
	// Convert string time to a LocalTime object
	private LocalTime getLocalTimeObject(String stringDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
		return LocalTime.parse(stringDate, formatter);
	}
	
	private boolean isAccountSelected() {
		if(tableModel.getSelectedRowIndex() == -1) {
			return false;
		}
		return true;
	}
	
	private void showPopupMessage(String message, String title) {
		JFrame frame = (JFrame) formPane.getTopLevelAncestor();
		Point location = new Point();
		int x = frame.getX() + (frame.getWidth() / 2);
		int y = frame.getY() + (frame.getHeight() / 2);
		location.setLocation(x, y);
		
		PopupDialog dialog = new PopupDialog(message);
		dialog.setLocation(location);
		dialog.pack();
		dialog.setTitle(title);
		dialog.setVisible(true);
	}
}
