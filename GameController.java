public class GameController {
    private final RideLine model;
    private final GameView view;

    public GameController(RideLine model, GameView view) {
        if (model == null) {
            throw new NullPointerException("Model cannot be null");
        }
        if (view == null) {
            throw new NullPointerException("View cannot be null");
        }
        this.model = model;
        this.view = view;
    }

    public static Action parseAction(String raw) {
        if (raw == null || raw.length() >= GameView.MAX_LINE_READ) {
            throw new IllegalArgumentException("Type wait, punch, or fastpass.");
        }
        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("wait")) {
            return Action.WAIT;
        }
        if (trimmed.equalsIgnoreCase("punch")) {
            return Action.PUNCH;
        }
        if (trimmed.equalsIgnoreCase("fastpass")) {
            return Action.FAST_PASS;
        }
        throw new IllegalArgumentException("Type wait, punch, or fastpass.");
    }

    public static int parseLineSize(String arg) {
        if (arg == null) {
            throw new IllegalArgumentException("Line size must be a whole number from 3 to 100.");
        }
        try {
            int size = Integer.parseInt(arg.trim());
            if (size < RideLine.MIN_LINE || size > RideLine.MAX_LINE) {
                throw new IllegalArgumentException("Line size must be a whole number from 3 to 100.");
            }
            return size;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Line size must be a whole number from 3 to 100.");
        }
    }

    public void run() {
        view.showWelcome(model.snapshot());

        while (!model.isGameOver()) {
            LineSnapshot current = model.snapshot();
            view.showState(current);

            Action validAction = null;
            while (validAction == null) {
                String rawInput = view.readCommand();
                if (rawInput == null) {
                    view.showSummary(model.snapshot(), false);
                    return;
                }

                try {
                    Action action = parseAction(rawInput);
                    if (action == Action.PUNCH && !current.canPunch()) {
                        view.showMessage("You're at the front, just wait.");
                    } else if (action == Action.FAST_PASS && !current.canUseFastPass()) {
                        view.showMessage("Fast pass already used or not available.");
                    } else {
                        validAction = action;
                    }
                } catch (IllegalArgumentException e) {
                    view.showMessage(e.getMessage());
                }
            }

            TurnResult result = model.takeTurn(validAction);
            view.showResult(result);
        }

        view.showSummary(model.snapshot(), true);
    }
}
