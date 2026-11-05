public interface GameView {
    int MAX_LINE_READ = 101;

    void showWelcome(LineSnapshot snapshot);
    void showState(LineSnapshot snapshot);
    void showResult(TurnResult result);
    void showMessage(String text);
    void showSummary(LineSnapshot snapshot, boolean finished);
    String readCommand();
}
