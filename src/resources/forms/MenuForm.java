package resources.forms;


import main.Issue;
import main.TerminalX;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class MenuForm extends JFrame {
    private ArrayList<Issue> issues;
    private JButton submitIssueButton;
    private JButton logOutButton;
    private JComboBox<String> sortByComboBox;
    private JCheckBox showArchivedCheckBox;
    private JPanel content;
    private JButton viewIssueButton;
    private JLabel userLabel;
    private JList issueList;

    public void drawIssues() {
        DefaultListModel model = new DefaultListModel();

        for (Issue issue : issues) {
            model.addElement(TerminalX.prettifyUUID(issue.id) + " " +
                    String.format("%23s", "(" + issue.status.getName() + ")") + " " +
                    issue.title);
        }

        issueList.setModel(model);
    }

    public MenuForm(String name) {
        issues = TerminalX.getIssues(TerminalX.SortMethod.valueOf(sortByComboBox.getSelectedItem().toString()), showArchivedCheckBox.isSelected());
        setContentPane(content);
        userLabel.setText("Signed in as " + name);

        drawIssues();

        submitIssueButton.addActionListener(actionEvent -> {
            TerminalX.openSubmitIssueForm();
        });
        logOutButton.addActionListener(actionEvent -> {
            TerminalX.logout();
        });
        sortByComboBox.addActionListener(actionEvent -> {
            issues = TerminalX.getIssues(TerminalX.SortMethod.valueOf(sortByComboBox.getSelectedItem().toString()), showArchivedCheckBox.isSelected());
            drawIssues();
        });
        showArchivedCheckBox.addActionListener(actionEvent -> {
            issues = TerminalX.getIssues(TerminalX.SortMethod.valueOf(sortByComboBox.getSelectedItem().toString()), showArchivedCheckBox.isSelected());
            drawIssues();
        });
        issueList.addListSelectionListener(actionEvent -> {
            if (!issueList.isSelectionEmpty())
                viewIssueButton.setEnabled(true);
        });
        issueList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    TerminalX.openIssueDetailsForm(issues.get(issueList.getSelectedIndex()));
                }
            }
        });
        viewIssueButton.addActionListener(actionEvent -> {
            TerminalX.openIssueDetailsForm(issues.get(issueList.getSelectedIndex()));
        });
    }
}
