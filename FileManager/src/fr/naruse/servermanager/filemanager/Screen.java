package fr.naruse.servermanager.filemanager;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.api.events.plugin.PluginFileManagerEvent;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.plugin.Plugins;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;

public class Screen {

    public static int READ_INTERVAL = 5000;
    private static boolean isStarted = false;

    private static void logReaderRunnable(){
        if(FileManager.EXECUTOR_SERVICE.isTerminated() || FileManager.EXECUTOR_SERVICE.isShutdown()){
            return;
        }
        FileManager.EXECUTOR_SERVICE.submit(() -> {
            while (true){
                for (ServerProcess serverProcess : FileManager.get().getAllServerProcess()) {
                    Screen screen = serverProcess.getScreen();
                    screen.read();
                }

                Utils.sleep(READ_INTERVAL);
            }
        });
    }

    private final GlobalLogger.Logger logger = new GlobalLogger.Logger("");
    private final ConcurrentLinkedDeque<String> printedLines = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String> allPrintedLines = new ConcurrentLinkedDeque<>();
    private final Attribute screenColor = Attribute.TEXT_COLOR(Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256));
    private final ServerProcess serverProcess;
    private final List<WindowLog> windowLogList = new ArrayList<>();

    private boolean isAttached = false;

    public Screen(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
        this.logger.setTag(serverProcess.getName());
        this.logger.setAttribute(this.screenColor);
        this.logger.setHideTimeAndThread(true);

        if(!isStarted){
            isStarted = true;
            logReaderRunnable();
        }
    }

    public void attachToScreen() {
        this.isAttached = true;
        GlobalLogger.info(Attribute.CYAN_TEXT(), "Attached to screen '"+this.serverProcess.getName()+"'");
        this.read();
    }

    private void read(){
        if(this.serverProcess.getLogFile().exists()){
            try {
                List<String> newLines = new ArrayList<>();

                Files.lines(Paths.get(this.serverProcess.getLogFile().toURI())).forEach(line -> {
                    if(!this.allPrintedLines.contains(line)){
                        this.allPrintedLines.add(line);
                        newLines.add(line);
                    }

                    Level level;
                    if (line.contains("ERROR") || line.contains("SEVERE")) {
                        level = Level.SEVERE;
                    } else if (line.contains("DEBUG") || line.contains("OFF")) {
                        level = Level.OFF;
                    } else if (line.contains("WARN") || line.contains("WARNING")) {
                        level = Level.WARNING;
                    } else {
                        level = Level.INFO;
                    }

                    for (int i = 0; i < this.windowLogList.size(); i++) {
                        WindowLog windowLog = this.windowLogList.get(i);
                        if(!windowLog.isVisible()){
                            this.windowLogList.remove(windowLog);
                        }else if(!windowLog.getPrintedLines().contains(line)){
                            windowLog.getPrintedLines().add(line);
                            windowLog.log(line, level);
                        }
                    }

                    if(this.isAttached && !this.printedLines.contains(line)) {
                        this.printedLines.add(line);

                        this.logger.log(level, line);
                    }
                });

                if(!newLines.isEmpty()){
                    Plugins.fireEvent(new PluginFileManagerEvent.AsyncConsoleOutputEvent(this.serverProcess, newLines));
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }else{
            this.detachFromScreen();
        }
    }

    public void detachFromScreen(){
        if(this.isAttached){
            GlobalLogger.info(Attribute.CYAN_TEXT(), "Detached from screen '"+this.serverProcess.getName()+"'");
        }
        this.isAttached = false;
        this.printedLines.clear();
    }

    public void newWindow() {
        this.windowLogList.add(new WindowLog());
    }

    public boolean isAttached() {
        return isAttached;
    }

    private static final String SPACE = "space";
    private static final String BACK_SPACE = "back space";

    private class WindowLog extends JFrame{

        private final JTextPane jTextPane = new JTextPane();
        private final JScrollPane jScrollPane = new JScrollPane(jTextPane);
        private final List<String> printedLines = new ArrayList<>();
        private final Style style = jTextPane.addStyle("style", null);
        private final JTextField jTextField = new JTextField("Command Prompt");

        public WindowLog() {
            this.setTitle("Process '"+serverProcess.getName()+"' - Log Console & Command Prompt");
            this.setSize(880, 520);

            this.setLayout(new BorderLayout());

            JTextArea textArea = new JTextArea();
            this.jTextPane.setForeground(textArea.getForeground());
            this.jTextPane.setFont(textArea.getFont());
            this.jTextPane.setEditable(false);
            this.jTextPane.setBackground(Color.GRAY);

            int condition = JComponent.WHEN_FOCUSED;
            InputMap taInputMap = textArea.getInputMap(condition);
            ActionMap taActionMap = textArea.getActionMap();

            taInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), SPACE);
            taInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), BACK_SPACE);
            taActionMap.put(SPACE, new KeyAction(textArea, SPACE));
            taActionMap.put(BACK_SPACE, new KeyAction(textArea, BACK_SPACE));


            this.jTextField.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {
                    String command = jTextField.getText();
                    if(command == null || command.isEmpty()){
                        return;
                    }
                    if(command.equals("Command Prompt")){
                        jTextField.setText("");
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            this.jTextField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}

                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER){ // Key Enter
                        String command = jTextField.getText();
                        if(command == null || command.isEmpty()){
                            return;
                        }

                        Optional<Server> optional = ServerList.getByNameOptional(serverProcess.getName());
                        if(!optional.isPresent()){
                            log("[ComplexScreen CONSOLE] Server '"+serverProcess.getName()+"' not found! It was maybe stopped.", Level.SEVERE);
                        }else{
                            optional.get().sendPacket(new PacketExecuteConsoleCommand(command));
                        }
                        jTextField.setText("");
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {}
            });

            this.add(this.jScrollPane, BorderLayout.CENTER);
            this.add(this.jTextField, BorderLayout.SOUTH);

            this.setVisible(true);
            this.setLocationRelativeTo(null);
            this.toFront();
            this.requestFocus();
            this.setAlwaysOnTop(true);
            this.setAlwaysOnTop(false);
        }

        public void log(String info, Level level) {
            info = info+"\n";
            if(level == Level.INFO || level == Level.OFF){
                this.log(info);
                return;
            }

            StyledDocument doc = jTextPane.getStyledDocument();
            StyleConstants.setForeground(this.style, this.getColor(level));
            try {
                doc.insertString(doc.getLength(), info, this.style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            this.validate();
            scrollDown();
        }

        private void log(String info) {
            append(info);
            this.validate();
            scrollDown();
        }

        private void append(String s) {
            try {
                Document doc = jTextPane.getDocument();
                doc.insertString(doc.getLength(), s, null);
            } catch(BadLocationException exc) {
                exc.printStackTrace();
            }
        }

        public void scrollDown(){
            this.jScrollPane.getVerticalScrollBar().setValue(this.jScrollPane.getVerticalScrollBar().getMaximum());
        }

        private Color getColor(Level level){
            if(level == Level.SEVERE){
                return new Color(126, 0, 11);
            }else if(level == Level.WARNING){
                return new Color(249, 250, 52);
            }
            return null;
        }

        public List<String> getPrintedLines() {
            return printedLines;
        }
    }

    private class KeyAction extends AbstractAction {
        private PlainDocument textAreaDocument;
        private String title;

        public KeyAction(JTextArea textArea, String title) {
            this.textAreaDocument = (PlainDocument) textArea.getDocument();
            this.title = title;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (title.equals(SPACE)) {
                try {
                    textAreaDocument.insertString(textAreaDocument.getLength(), " ",
                            null);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            } else if (title.equals(BACK_SPACE)) {
                if (textAreaDocument.getLength() == 0) {
                    return;
                }
                try {
                    textAreaDocument.remove(textAreaDocument.getLength() - 1, 1);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
