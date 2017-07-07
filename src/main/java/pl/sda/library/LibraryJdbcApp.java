package pl.sda.library;

import javax.swing.SwingUtilities;

import pl.sda.library.view.JdbcAppView;

public class LibraryJdbcApp {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void createAndShowGUI() throws Exception {
		new JdbcAppView();
	}

}
