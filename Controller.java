package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.scene.layout.StackPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    @FXML private CodeArea codeArea;
    @FXML private AnchorPane anchorPane;
    @FXML private ChoiceBox choiceBoxFont;
    @FXML private ChoiceBox choiceBox;
    @FXML private TextArea consoleArea;

    private ListView tipBox;
    private ObservableList<String> userData;
    private ObservableList<String> tipData;
    private boolean isBlackTheme = false;
    private FileChooser fileChooser;
    private int caretPosition;
    private String currentValue;
    private ArrayList<Integer> openedBraces;
    private ArrayList<Integer> closedBraces;
    private final ChangeListener<String> listener = (i, oldV, newV) -> {
        if(newV != null)
            codeArea.replaceText(caretPosition - currentValue.length(), caretPosition, newV);
        tipBox.getSelectionModel().clearSelection();
        tipBox.setItems(null);
        tipBox.setVisible(false);
    };

    public void initialize() {
        openedBraces = new ArrayList<>();
        closedBraces = new ArrayList<>();
        fileChooser = new FileChooser();
        consoleArea.setDisable(true);
        setChoiceBox();
        setChoiceBoxFont();
        initializeKeywords();
        setCodeArea();
        initTipBox();
    }

    private void countBraces() {
        consoleArea.setText("");
        openedBraces.clear();
        closedBraces.clear();
        char[] text = codeArea.getText().toCharArray();
        for(int i = 0; i != text.length; ++i) {
            if(text[i] == '{')
                openedBraces.add(i);
            else if(text[i] == '}')
                closedBraces.add(i);
        }
        if(openedBraces.size() != closedBraces.size()) {
            consoleArea.setText("Неравное кол-во фигурных скобок");
            return;
        }
        openedBraces.addAll(closedBraces);
        for(int i = 0, size = openedBraces.size() - 1; i != size; )
            if(openedBraces.get(i) > openedBraces.get(++i)) {
                consoleArea.setText("Нарушен баланс фигурных скобок");
                return;
            }
    }

    public void saveFileAsPressed(ActionEvent actionEvent) {
        File selectedFile = fileChooser.showSaveDialog(null);
        try {
            FileWriter fileWriter = new FileWriter(selectedFile);
            fileWriter.write(codeArea.getText());
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("IOException handled");
        }
    }

    public void saveButtonPressed(ActionEvent actionEvent) {
        List<String> list = new ArrayList<>();
        list.add(codeArea.getText());
        if(StringConstants.pathToOpenedFile.length() != 0)
            try {
                Files.write(Paths.get(StringConstants.pathToOpenedFile), list);
            }
            catch (IOException ex) {
                System.out.println("IOException 3");
            }
    }


    public void openFilePressed(ActionEvent actionEvent) throws IOException {
        File selectedFile = fileChooser.showOpenDialog(null);
        userData = FXCollections.observableArrayList();
        if(selectedFile != null) {
            Path path = Paths.get(selectedFile.getAbsolutePath());
            userData = FXCollections.observableArrayList(Files.readAllLines(path));
            codeArea.clear();
            for(String i: userData)
                codeArea.appendText(i + "\n");
        }
    }

    private void initTipBox() {
        tipBox = new ListView();
        tipBox.setVisible(false);
        tipBox.setPrefSize(100, 100);
        tipBox.getSelectionModel().selectedItemProperty().addListener(listener);
        anchorPane.getChildren().add(tipBox);
    }

    private void initializeKeywords() {
        StringConstants.KEYWORDS = getKeywords();
        StringConstants.KEYWORD_PATTERN = "\\b(" + String.join("|", StringConstants.KEYWORDS) + ")\\b";
        StringConstants.PATTERN = Pattern.compile(
                "(?<KEYWORD>" + StringConstants.KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + StringConstants.PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + StringConstants.BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + StringConstants.BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + StringConstants.SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + StringConstants.STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + StringConstants.COMMENT_PATTERN + ")"
        );
    }

    private void setTipBox() {
        caretPosition = codeArea.caretPositionProperty().getValue();
        tipData = FXCollections.observableArrayList();
        updateCurrentValue();
        int i;
        int size;
        for(i = 0, size = Plugins.getPlugins(StringConstants.pathToFolder).size(); i < size; ) {
            if(Plugins.getPlugins(StringConstants.pathToFolder).get(i).getFileName().toString().startsWith(choiceBox.getValue().toString()))
                break;
            i++;
        }
        for (String keyword: Plugins.getKeywords().get(i)) {
            if(keyword.startsWith(currentValue)) {
                tipData.add(keyword);
            }
        }
        if(currentValue.equals("") || tipData.size() == 0) {
            tipBox.setVisible(false);
            return;
        }
        tipBox.setVisible(true);
        tipBox.setItems(tipData);
        tipBox.relocate(codeArea.caretBoundsProperty().getValue().get().getMaxX(), codeArea.caretBoundsProperty().getValue().get().getMaxY() - 20);
    }

    private void updateCurrentValue() {
        currentValue = codeArea.getText(codeArea.getText(0, caretPosition).lastIndexOf(getSymbol()) + 1, caretPosition);
    }

    private void setCodeArea() {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.multiPlainChanges().successionEnds(Duration.ofMillis(200))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        codeArea.replaceText(0, 0, StringConstants.sampleCode);
        codeArea.setOnKeyTyped(e -> {
            setTipBox();
            countBraces();
        });
    }

    private void setChoiceBoxFont() {
        String[] fonts = new String[] {"10", "12", "14", "16", "18", "20", "24", "36", "48"};
        userData = FXCollections.observableArrayList(fonts);
        choiceBoxFont.setItems(userData);
        choiceBoxFont.setValue("12");
        choiceBoxFont.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            choiceBoxFont.setValue(newValue);
            codeArea.setStyle("-fx-font-size: " + newValue + "px;" +
                    "-fx-border-color: #2FC4C7");
            if(isBlackTheme)
                codeArea.setStyle("-fx-font-size: " + newValue + "px;" +
                        "-fx-border-color: #2FC4C7;" +
                        "-fx-text-fill: white");
        });
    }

    private void setChoiceBox() {
        userData = FXCollections.observableArrayList();
        for(Path i: Plugins.getPlugins(StringConstants.pathToFolder))
            userData.add(i.getFileName().toString().substring(0,
                    i.getFileName().toString().lastIndexOf(".")));
        choiceBox.setItems(userData);
        choiceBox.setValue("Java");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            choiceBox.setValue(newValue);
            initializeKeywords();
        });
    }

    private String[] getKeywords() {
        String[] strings = new String[0];
        for(List<String> i: Plugins.getKeywords())
            if(i.get(0).substring(0, i.get(0).length() - 1).equals(choiceBox.getValue())) {
                strings = i.toArray(new String[i.size()]);
                return strings;
            }
        return strings;
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = StringConstants.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private String getSymbol() {
        int caretPosition = codeArea.caretPositionProperty().getValue();
        String var;
        if(codeArea.getText(0, caretPosition).lastIndexOf(" ") > codeArea.getText(0, caretPosition).lastIndexOf("\n")) {
            if(codeArea.getText(0, caretPosition).lastIndexOf(" ") > codeArea.getText(0, caretPosition).lastIndexOf("\t"))
                var =  " ";
            else var = "\t";
        }
        else if(codeArea.getText(0, caretPosition).lastIndexOf("\n") > codeArea.getText(0, caretPosition).lastIndexOf("\t"))
            var = "\n";
        else var = "\t";
        return var;
    }

    public void play(ActionEvent actionEvent) {
    }
}
