package xal.app.magnetdbmanager;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class FXDialog {
	public enum Response { YES, NO, CANCEL };
	private static Response buttonSelected = Response.NO;
	
	static class Dialog extends Stage{
		public Dialog(String title, String message, Stage owner){
			setTitle(title);
			initStyle(StageStyle.UTILITY);
			initModality(Modality.APPLICATION_MODAL);
			initOwner(owner);
			setResizable(false);
			
			Parent p;
			try {
				p = FXMLLoader.load(getClass().getResource("./resources/FXDialog.fxml"));
				Scene scene = new Scene(p);
				setScene(scene);
				
				Label messageLabel = (Label) p.lookup("#messageLabel");
				messageLabel.setText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.setOnCloseRequest(new EventHandler<WindowEvent>(){
				@Override
				public void handle(WindowEvent event) {
					((Stage)event.getSource()).close();
				}
			});
		}
		
		public void showDialog() {
	        sizeToScene();
	        centerOnScreen();
	        showAndWait();
	    }
		
		
	}
	
	public static Response showConfirmDialog(String title, String message, Stage owner){
		final Dialog dialog = new Dialog(title, message, owner);
		
		ImageView iconImage = (ImageView) dialog.getScene().lookup("#iconImageView");
		iconImage.setImage(new Image(dialog.getClass().getResourceAsStream("./resources/Button-help-icon.png")));
		
		Button yesButton = (Button) dialog.getScene().lookup("#yesButton");
		yesButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				buttonSelected = Response.YES;
				dialog.close();
			}
			
		});
		
		Button noButton = (Button) dialog.getScene().lookup("#noButton");
		noButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				buttonSelected = Response.NO;
				dialog.close();
			}
			
		});
		
		dialog.showDialog();
		return buttonSelected;
	}
	
	public static void showWarningDialog(String title, String message, Stage owner){
		final Dialog dialog = new Dialog(title, message, owner);
		
		ImageView iconImage = (ImageView) dialog.getScene().lookup("#iconImageView");
		iconImage.setImage(new Image(dialog.getClass().getResourceAsStream("./resources/Button-info-icon.png")));
		
		Button yesButton = (Button) dialog.getScene().lookup("#yesButton");
		yesButton.setText("Ok");
		
		Button noButton = (Button) dialog.getScene().lookup("#noButton");
		noButton.setVisible(false);
		
		yesButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				dialog.close();
			}
			
		});
		
		dialog.showDialog();
	}
	
	public static void showErrorDialog(String title, String message, Stage owner){
		final Dialog dialog = new Dialog(title, message, owner);
		
		ImageView iconImage = (ImageView) dialog.getScene().lookup("#iconImageView");
		iconImage.setImage(new Image(dialog.getClass().getResourceAsStream("./resources/Button-info-icon.png")));
		
		Button yesButton = (Button) dialog.getScene().lookup("#yesButton");
		yesButton.setText("Ok");
		
		Button noButton = (Button) dialog.getScene().lookup("#noButton");
		noButton.setVisible(false);
		
		yesButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				dialog.close();
			}
			
		});
		
		dialog.showDialog();
	}

}
