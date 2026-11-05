public class PunchLineApp {
    public static void main(String[] args) {
        int lineSize = 15;

        if (args.length > 1) {
            System.err.println("Line size must be a whole number from 3 to 100.");
            System.exit(1);
        } else if (args.length == 1) {
            try {
                lineSize = GameController.parseLineSize(args[0]);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        RideLine model = new RideLine(lineSize, "You", new RandomDice());
        ConsoleView view = new ConsoleView(System.in, System.out);
        GameController controller = new GameController(model, view);

        controller.run();
    }
}
