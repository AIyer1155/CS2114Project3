# Punch Line (CS 2114 Project 3)

A single-player terminal game. You start at the back of a line of about 15 guests. Each turn you either WAIT or PUNCH the guest ahead of you. A punch wins with a probability that falls linearly from 70% (back of the line) to 10% (second place). A win swaps you one place forward; a loss sends you to the back. The front guest boards every turn, and the game ends when you board.

Team: Jack, Liam, Nikita, and Advay.

## How to run

All commands run from the project folder. `junit-4.13.2.jar` and `hamcrest-core-1.3.jar` must be in it (they are included). On Windows replace `:` with `;` in the classpath.

Compile everything:

    javac -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar *.java

Run the model tests (no view involved; `FixedDice` replaces randomness):

    java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore GuestTest LinkedQueueTest RandomDiceTest RideLineTest ValueTypesTest GameControllerTest ConsoleViewTest

Expected: `OK (65 tests)`. The model alone is covered by `GuestTest`, `LinkedQueueTest`, `RandomDiceTest`, `RideLineTest`, `ValueTypesTest`.

Play the console game (thin view, driven by the controller):

    java PunchLineApp          # 15 guests
    java PunchLineApp 30       # any whole number from 3 to 100

Type `wait` or `punch`. Bad input prints a message and re-asks; end of input (Ctrl-D / Ctrl-Z) ends the game cleanly.

## Reflection

### What changed from the specification

| Area | Spec said | Built | Why |
|---|---|---|---|
| Classes and signatures | 13 types: Action, Outcome, Dice, RandomDice, Guest, LinkedQueue, RideLine, TurnResult, LineSnapshot, GameView, ConsoleView, GameController, PunchLineApp | Same 13, same public signatures | Nothing in the first slice forced a signature change. |
| `ConsoleView.readCommand` | Reads at most 101 characters of a line | Reads the whole line with `readLine()`, then cuts it to 101 | `BufferedReader.readLine()` is simpler. The cut keeps the controller's length check working, but a multi-megabyte line is still read into memory first. This is a known gap (see below). |
| Re-prompt | After a bad command the prompt is shown again | The error message is shown; the "Choose action" prompt is not reprinted | The inner re-ask loop lives in `GameController.run`, which only calls `showState` once per turn. |
| Validation location | Constructors and `RideLine` check sizes, odds and names; the controller parses text; the view only reads | Same | Matches. `GameController.parseAction` and `parseLineSize` are `static` so tests can call them without a view. |
| Tests | Test plan with a `FixedDice` helper and tests per class | These did not exist when the prototype was first written; they now do (65 tests, plus `FixedDice` and `ScriptedView` helpers) | Written after the code instead of alongside it. This was a process miss. |
| Swap and send-to-back | Done with only `enqueue` and `dequeue` | Done by cycling the whole line through `enqueue`/`dequeue` | This is O(L) per swap, with L at most 100. Acceptable, and it keeps `LinkedQueue` free of index methods. |

### Did the model stand alone?

Yes. `RideLine`, `LinkedQueue`, `Guest`, `Dice`, `RandomDice` and the value types (`Action`, `Outcome`, `TurnResult`, `LineSnapshot`) compile and run without `GameView`, `ConsoleView` or `GameController`; the 65 tests exercise them with no console. No model class holds a reference to a view. Two things to note:

- The model does depend on its own value types (`TurnResult`, `LineSnapshot`). That is intended: they are how it reports out.
- `GameView` contains a constant `MAX_LINE_READ = 101` which `GameController.parseAction` also uses. The controller therefore depends on a view-side constant; it would be cleaner in the controller.

### What happened to the unknowns from the scope

- Swapping two places in a singly linked queue: solved by cycling (above).
- Linear odds by position: solved, formula `p(k, L) = f + (b - f)(k - 2)/(L - 2)` for L >= 3, with the L = 2 case giving `f`. Tested at the back, second place, mid-line and with custom odds.
- Testing random behaviour: solved with the `Dice` interface and the `FixedDice` test helper.
- Game balance: still open. A simulation of the real model (same odds and line shrinkage) found that always waiting takes 15 turns in a 15-guest line, while the strategy "punch whenever you are in place 8 or worse, otherwise wait" averages about 12.9 turns, roughly 14% faster. Punching helps, but not by much, and nothing yet makes the choice a real dilemma. We have not decided whether that is good enough.

### What does not work yet

- `readCommand` reads an entire over-long line before cutting it, so a huge single line is held in memory. It does not crash on the 1,000,000-character case we tested.
- The prompt is not reprinted after a bad command.
- The balance question above.
- No automated test covers `PunchLineApp.main` argument handling. We checked it by hand: a non-numeric size prints the size-range message to stderr and exits with code 1.

## Next Steps (Deliverable 4)

### Finish the MVP, in order

1. Make `readCommand` read a bounded number of characters instead of a whole line (`ConsoleView.readCommand`). Owner: Nikita.
2. Reprint the prompt after a bad command (`GameController.run`, with a `GameView` call to re-show it). Owner: Liam.
3. Move `MAX_LINE_READ` out of `GameView` into `GameController` and update `ConsoleView`. Owner: Liam.
4. Add a test for `PunchLineApp` argument handling and exit codes. Owner: Jack.
5. Playtest and decide on balance, using the simulation numbers above. Owner: Advay.

### Change feature: fast-pass lane

A second queue (the fast lane) and a routing rule. Every third boarding slot is taken from the fast lane; the rest come from the standard lane. The player has one `FAST_PASS` action per game that moves them to the back of the fast lane, which is shorter, so they board sooner. This adds model state (a second `LinkedQueue`, a per-game "used" flag) and logic (routing, a new action), and the player reaches it through the controller and view.

**Prediction: classes that will change**

- `Action`: new value `FAST_PASS`.
- `RideLine`: constructor (build the second queue), `takeTurn` (new branch and the 1-in-3 routing), `playerPosition`, `punchOdds` (which lane's length to use), `snapshot` (report both lanes).
- `Outcome` and `TurnResult`: probably one new outcome (`FAST_PASSED`), and possibly a field for which lane boarded.
- `LineSnapshot`: constructor and a new getter for the fast-lane names.
- `GameController`: `parseAction` (accept `fastpass`) and `run` (refuse it if already used).
- `ConsoleView`: `showState` (show both lanes, offer the option) and `showResult` (describe it).

**Prediction: classes that should not change**

`LinkedQueue`, `Guest`, `Dice`, `RandomDice`, `PunchLineApp`, and the signatures of the `GameView` interface. If `LinkedQueue` or `Dice` has to change, the design was wrong and we will say so in the Deliverable 4 reflection. Before committing we asked GenAI which classes the feature would force us to edit (see GenAI note).

### Stretch goals

- Committing to: a strategy simulator (a small driver that plays many games with a fixed strategy using `RandomDice`) to support the balance decision. It is model-only and needs no view.
- Dropping: guests who punch back, and any graphical line display. Neither fits the time we have.

### New tests needed

- `RideLineTest`: fast-pass used once, second use throws, fast-pass when already at the front of a lane, routing of every third boarding with `FixedDice`, odds computed with two lanes, line of the minimum size, game ends from either lane.
- `LinkedQueueTest`: no change expected. If one is needed, that is a design signal.
- `GameControllerTest`: `fastpass` accepted in any case, `fast pass` and `fastpas` rejected with the existing message, a second `fastpass` shows a message and does not call the model.
- `ConsoleViewTest`: `showState` with two lanes, with the fast-pass option available and used.
- `ValueTypesTest`: new `LineSnapshot` and `TurnResult` fields, including null and copy checks.

### Who does what

| Item | Owner |
|---|---|
| Items 1 and 3 above | Nikita (view), Liam (controller) |
| Items 2 and 4 above | Liam, Jack |
| Balance and simulator | Advay |
| Fast-pass: `RideLine`, `Action`, `Outcome`, `TurnResult`, `LineSnapshot` | Advay, with Jack on the second queue |
| Fast-pass: `GameController` | Liam |
| Fast-pass: `ConsoleView` | Nikita |
| Fast-pass tests | Each owner tests their own class; Jack reviews |

## Attribution

Fill in the Who and How columns with what actually happened. Entries in brackets are placeholders, not facts.

| File | Who worked on it | How |
|---|---|---|
| `Action.java` | [Nikita] | [wrote]|
| `Outcome.java` | [Jack] | [wrote] |
| `Dice.java` | [Liam] | [wrote] |
| `RandomDice.java` | [Liam] | [wrote] |
| `Guest.java` | [Liam] | [ wrote ] |
| `LinkedQueue.java` | [Nikita], GenAI Revised | |
| `RideLine.java` | [Liam] | [wrote ] |
| `TurnResult.java` | [Liam] | [wrote ] |
| `LineSnapshot.java` | [Jack] | [ wrote] |
| `GameView.java` | [Nikita], GenAI Revised | |
| `ConsoleView.java` | [Nikita], GenAI Revised | |
| `GameController.java` | [Jack] | [wrote] |
| `PunchLineApp.java` | [Jack] | [wrote] |
| `FixedDice.java` (test helper) | [Liam], GenAI | revised by Claude |
| `ScriptedView.java` (test helper) | [Liam], GenAI | revised by Claude|
| `GuestTest.java` | [Jack], GenAI | revised by Claude from the spec test plan |
| `LinkedQueueTest.java` | [Advay], GenAI revised | Same |
| `RandomDiceTest.java` | [Advay], GenAI revised| Same |
| `RideLineTest.java` | [Advay], GenAI revised| Same|
| `ValueTypesTest.java` | [Advay], GenAI revised | Same |
| `GameControllerTest.java` | [Advay], GenAI revised | Same |
| `ConsoleViewTest.java` | [Advay], GenAI revised | Same |
| `README.md` | [Jack], Gen AI revised |

### GenAI note

We used Claude to revise all our drafted code and graded our prototype against the Deliverable 3 rubric, then for a README and JUnit tests for the model. It also ran a simulation of our model to give us balance numbers. We compiled the code and ran all 65 tests ourselves.
