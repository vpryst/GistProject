package com.oshmidt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class App {

	private static Scanner scanner;

	private static User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	private static App app;

	public static GistManager gistManager = new GistManager();

	public static void main(String[] args) throws IOException {
		// resource = ResourceBundle.getBundle("strings", Locale.getDefault());
		app = new App();
		user = new User();
		scanner = new Scanner(System.in);
		while (true) {
			System.out.println("");
			System.out.print(Messages.getString("typeCommand"));
			String command = scanner.nextLine();
			app.doCommand(command);
		}
	}

	public void doCommand(String command) throws IOException {
		if (command.equals("login")) {
			readLogin();
		} else if (command.equals("exit")) {
			System.out.println(Messages.getString("leaveMessage"));
			System.exit(0);
		} else if (command.equals("status")) {
			System.out.println(Messages.getString("login")
					+ getUser().getLogin());
			System.out.println(Messages.getString("pass")
					+ new String(getUser().getPassword()));
		} else if (command.toLowerCase().equals("creategist")) {
			gistManager.createNewGist(getUser());
		} else if (command.toLowerCase().equals("loadgists")) {
			gistManager.loadGists(getUser());
		} else if (command.toLowerCase().equals("showgists")) {
			gistManager.showGists();
		} else if (command.toLowerCase().equals("savelp")) {
			saveLoginAndPassword();
		} else if (command.toLowerCase().equals("loadlp")) {
			loadLoginAdnPassword();
		} else if (command.toLowerCase().equals("loadfiles")) {
			gistManager.loadFiles(getUser().getLogin());
		} else if (command.toLowerCase().equals("uploadfiles")) {
			gistManager.uploadFiles(getUser());
		} else if (command.equals("") || command.equals("help")) {
			showHelp();
		} else {
			System.out.println(Messages.getString("unknownCommand"));
			System.out.println();
		}
	}

	public void saveLoginAndPassword() {
		Properties prop = new Properties();
		try {
			if (getUser().getLogin() != null) {
				prop.setProperty("login", getUser().getLogin());
			}
			if (getUser().getPassword() != null) {
				prop.setProperty("password",
						new String(getUser().getPassword()));
			}
			prop.store(new FileOutputStream("config.properties"), null);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void loadLoginAdnPassword() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("config.properties"));
			getUser().setLogin(prop.getProperty("login"));
			getUser().setPassword(prop.getProperty("password").toCharArray());

			System.out.println(getUser().getLogin());
			System.out.println(getUser().getPassword());

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void readLogin() {
		System.out.print(Messages.getString("typeLogin"));
		getUser().setLogin(scanner.nextLine());
		readPassword();
	}

	public void readPassword() {
		System.out.print(Messages.getString("typePassword"));
		getUser().setPassword(scanner.nextLine().toCharArray());
	}

	public void showHelp() {
		System.out.println(Messages.getString("commandList"));
		System.out.println(Messages.getString("statusDescription"));
		System.out.println(Messages.getString("loginDescription"));
		System.out
				.println(Messages.getString("uploadLoginPasswordDescription"));
		System.out.println(Messages.getString("loadLoginPasswordDescription"));
		System.out.println(Messages.getString("loadGistsDescription"));
		System.out.println(Messages.getString("showGistsDescription"));
		System.out.println(Messages.getString("exit"));
	}

}
