package xal.app.magnetdbmanager;

import xal.extension.application.Application;

public class Tools {

	/**
	 * 
	 * @param value The string will be parsed.
	 * @return the parsed result.
	 */
	static double parseDouble(String value){
		return parseDouble(value, "", "", false);
	}
	
	/**
	 * 
	 * @param valuevalue The string will be parsed.
	 * @param title Title of the error dialog.
	 * @param name The name of the variable to parsed.
	 * @return the parsed result.
	 */
	static double parseDouble(String value, String title, String name){
		return parseDouble(value, title, name, true);
	}
	
	/**
	 * 
	 * @param value The string will be parsed.
	 * @param title Title of the error dialog.
	 * @param name The name of the variable to parsed.
	 * @param showdialog If there is parsing error, a dialog will be shown.
	 * @return the parsed result.
	 */
	static double parseDouble(String value, String title, String name, boolean showdialog){
		double result;
		try{
			result = Double.parseDouble(value);
		} catch(NumberFormatException e){
			result = Double.NaN;
//			if(showdialog) FXDialog.showErrorDialog(title, name + " must be a number!", null);
			if(showdialog) Application.displayError(title, name + " must be a number!");
		}
		return result;
	}
}
