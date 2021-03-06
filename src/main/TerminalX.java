package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import resources.forms.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static main.TerminalX.SortMethod.Title;
import static main.TerminalX.SortMethod.Status;
import static main.TerminalX.SortMethod.ID;

public class TerminalX {
    public static ArrayList<User> users;
    private static ArrayList<Project> projects;
    private static ArrayList<Issue> issues;
    public static User verifiedUser;
    private static JFrame screen;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main (String[] args) throws IOException {
        users = gson.fromJson(new FileReader("./users.json"), new TypeToken<ArrayList<User>>(){}.getType());
        projects = gson.fromJson(new FileReader("./projects.json"), new TypeToken<ArrayList<Project>>(){}.getType());
        issues = gson.fromJson(new FileReader("./issues.json"), new TypeToken<ArrayList<Issue>>(){}.getType());

        System.out.println("Loaded the following users:");
        for (User user : users) {
            System.out.println(user.emailAddress + " (" + user.type.getName() + ")");
        }

        displayLogin();
    }

    public static void addIssue(Issue issue) throws IOException {
        issues.add(issue);

        FileWriter writer = new FileWriter("./issues.json");
        gson.toJson(issues, writer);
        writer.flush();
        writer.close();
    }

    public static void updateIssue(Issue issue) throws IOException {
        issues.remove(issues.indexOf(issues.stream().filter(x -> x.id.equals(issue.id)).findFirst().get()));
        issues.add(issue);
      
        FileWriter writer = new FileWriter("./issues.json");
        gson.toJson(issues, writer);
        writer.flush();
        writer.close();
    }
  
    public static void deleteIssue(Issue issue) throws IOException {
        issues.remove(issue);

        FileWriter writer = new FileWriter("./issues.json");
        gson.toJson(issues, writer);
        writer.flush();
        writer.close();
    }

    public static String prettifyUUID(UUID uuid) {
        return uuid.toString().substring(uuid.toString().length() - 7);
    }

    public static String prettifyDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        return formatter.format(date);
    }

    public static User getUserByUUID(UUID uuid) {
        return users.stream().filter(user -> user.id.equals(uuid)).findFirst().get();
    }

    public static UUID getUUIDByName(String name) {
        return users.stream().filter(user -> user.name.equals(name)).findFirst().get().id;
    }

    public static Project getProjectByUUID(UUID uuid) {
        return projects.stream().filter(project -> project.id.equals(uuid)).findFirst().get();
    }

    public enum SortMethod {
        Title,
        Status,
        ID
    }

    public static ArrayList<Issue> getIssues(SortMethod method, Boolean showArchived) {
        ArrayList<Issue> userIssues;

        if (verifiedUser.type == User.AccountType.Customer) {
            userIssues = issues.stream().filter(issue -> issue.reporter.equals(verifiedUser.id)).collect(Collectors.toCollection(ArrayList::new));
        } else {
            userIssues = issues;
        }

        if (!showArchived)
            userIssues = userIssues.stream().filter(issue -> !issue.status.equals(Issue.Status.Archived)).collect(Collectors.toCollection(ArrayList::new));

        Comparator comparator;

        switch (method) {
            case Title:
                comparator = Comparator.comparing((Issue a) -> a.title);
                break;
            case Status:
                comparator = Comparator.comparing((Issue a) -> a.status);
                break;
            case ID:
                comparator = Comparator.comparing((Issue a) -> prettifyUUID(a.id));
                break;
            default:
                comparator = null;
                break;
        }

        userIssues.sort(comparator);

        return userIssues;
    }

    private static void displayLogin() {
        screen = new JFrame("Login");
        screen.setContentPane(new LoginForm().getContentPane());
        screen.setMinimumSize(new Dimension(300, 200));
        screen.setMaximumSize(new Dimension(300, 200));
        screen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        screen.pack();
        screen.setVisible(true);
    }

    private static void displayMenu() {
        screen = new JFrame("Main Menu");
        screen.setContentPane(new MenuForm(verifiedUser.name).getContentPane());
        screen.setMinimumSize(new Dimension(650, 500));
        screen.setMaximumSize(new Dimension(650, 500));
        screen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        screen.pack();
        screen.setVisible(true);
    }

    private static void displaySubmitIssue() {
        screen = new JFrame("Submit Issue");
        screen.setContentPane(new SubmitIssueForm(verifiedUser.name).getContentPane());
        screen.setMinimumSize(new Dimension(650, 500));
        screen.setMaximumSize(new Dimension(650, 500));
        screen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        screen.pack();
        screen.setVisible(true);
    }

    private static void displayIssueDetails(Issue issue) {
        Container content;

        if (verifiedUser.type == User.AccountType.Customer) {
            content = new CustomerIssueDetailsForm(verifiedUser.name, issue).getContentPane();
        } else {
            content = new EmployeeIssueDetailsForm(verifiedUser.name, issue).getContentPane();
        }

        screen = new JFrame("Issue Details");
        screen.setContentPane(content);
        screen.setMinimumSize(new Dimension(650, 500));
        screen.setMaximumSize(new Dimension(650, 500));
        screen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        screen.pack();
        screen.setVisible(true);
    }

    public static void logout() {
        verifiedUser = null;
        screen.dispose();
        displayLogin();
    }

    public static boolean verifyLogin(String username, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        for (User user : users) {
            if (username.equals(user.username) && Arrays.equals(Hasher.hash(password), user.password)) {
                verifiedUser = user;
                break;
            }
        }

        if (verifiedUser != null) {
            System.out.println(verifiedUser.name + " successfully logged in!");
            screen.dispose();
            displayMenu();
            return true;
        } else {
            System.out.println("Invalid credentials...");
            return false;
        }
    }

    public static void openSubmitIssueForm() {
        screen.dispose();
        displaySubmitIssue();
    }

    public static void openIssueDetailsForm(Issue issue) {
        screen.dispose();
        displayIssueDetails(issue);
    }

    public static void openMenuForm() {
        screen.dispose();
        displayMenu();
    }
}