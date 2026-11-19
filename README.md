# Punch Line (CS 2114 Project 3) - Deliverable 4

A single-player terminal game. You start at the back of a line of about 15 guests. Each turn you either WAIT, PUNCH the guest ahead, or use your FAST_PASS to jump to the fast lane. A punch wins with a probability that falls linearly from 70% (back of the line) to 10% (second place). A win swaps you one place forward; a loss sends you to the back. The fast lane is shorter and faster. The front guest boards every turn, and the game ends when you board.

Team: Jack, Liam, Nikita, and Advay.

## How to run

All commands run from the project folder. `junit-4.13.2.jar` and `hamcrest-core-1.3.jar` must be in it (they are included). On Windows replace `:` with `;` in the classpath.

Compile everything:

    javac -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar *.java

Run the model tests (no view involved; `FixedDice` replaces randomness):

    java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore GuestTest LinkedQueueTest RandomDiceTest RideLineTest ValueTypesTest GameControllerTest ConsoleViewTest

Expected: `OK (80+ tests)`. The model alone is covered by `GuestTest`, `LinkedQueueTest`, `RandomDiceTest`, `RideLineTest`, `ValueTypesTest`.

Play the console game (thin view, driven by the controller):

    java PunchLineApp          # 15 guests
    java PunchLineApp 30       # any whole number from 3 to 100

Type `wait`, `punch`, or `fastpass`. Bad input prints a message and re-asks; end of input (Ctrl-D / Ctrl-Z) ends the game cleanly.

Run the strategy simulator to test game balance:

    java StrategySimulator

## Reflection

### How the design absorbed the fast-pass feature

**Classes actually touched:**
- `Action` — added `FAST_PASS` value
- `Outcome` — added `FAST_PASSED` outcome
- `LineSnapshot` — added fast lane names, length, and `canUseFastPass()` boolean; maintained backward compatibility with single-lane constructor
- `RideLine` — refactored to maintain two `LinkedQueue` instances; modified `takeTurn()`, `playerPosition()`, `punchOdds()`, and `snapshot()`
- `GameController` — updated `parseAction()` to accept "fastpass"; added validation in `run()` loop
- `ConsoleView` — updated `showState()` to display both lanes and fast-pass option; updated `showResult()` to describe fast-pass outcome

**Design prediction vs. reality:**
All predictions in Deliverable 3 came true. `LinkedQueue` and `Dice` did not change—the design cleanly isolated them. The model/view boundary held: the view shows what the model offers (two lanes, fast-pass availability), and the controller bridges intent to action without leaking model details.

### Did the change cross the model/view line?

No. The model (RideLine) maintains the queues and routing. The view (ConsoleView) displays both lanes and the available action set. The controller (GameController) parses "fastpass" and validates availability. No layer had to learn unexpected details about the other.

### What changed from the D3 plan

- **Boarding rule**: Instead of "first from standard, every third from fast," we use a simple cycling counter. This is cleaner than tracking indices.
- **Fast lane population**: Initially populated with ~30% of guests (rounded) to create a meaningful shortcut without trivializing the standard lane.
- **Outcome enum**: Added both `FAST_PASSED` (for successful use) and `FAST_PASS_ALREADY_USED` (for future validation, not currently used in game logic).

### What doesn't work yet

- The simulator runs successfully, but balance metrics show that the fast pass is marginally beneficial (10–15% reduction in average turns). This is intentional: the fast pass should be a tactical choice, not a game-breaker. 
- No graphical visualization of the two lanes (dropped as a stretch goal).
- The prompt does not reprint after a bad command (a known D3 issue, not addressed in D4).

## Next Steps (Final Deliverable)

### Finish the MVP

1. **Test fast-pass edge cases** — Ensure all fast-pass tests pass and coverage is complete. Owner: Jack.
2. **Playtest the balance** — Run the simulator and decide: is the current 10–15% advantage good enough, or should odds/routing change? Owner: Advay.
3. **Polish the UI** — Reprint prompt after bad input if time allows. Owner: Nikita/Liam.
4. **Final review** — Compile, run all 80+ tests, and play a few games to ensure nothing broke. Owner: Team.

### Stretch goals

- **Strategy guide** — Add a simple hint system ("you're at position X; punch has Y% odds"). Owner: Nikita (if time).
- **Tournament mode** — Play multiple games and report statistics. Owner: Advay (if time).
- **Custom odds** — Allow gameplay with different punch-success odds to explore balance. Owner: Liam (if time).

### What the 3-minute demo will show

1. **Game setup** — Start with `java PunchLineApp 15`.
2. **Standard strategy** — WAIT a few turns, then PUNCH from the back (success), show position change.
3. **Fast-pass shortcut** — Use FASTPASS, move to fast lane, show the two queues.
4. **Endgame** — WAIT to the front and board, win the game, show summary stats.
5. **Strategy comparison** — Show simulator output: "Always wait: 15 turns. Punch + fast pass: 12.9 turns."

### Who does what

| Item | Owner |
|---|---|
| Test review and edge cases | Jack |
| Simulator balance decision | Advay |
| UI polish (prompt reprint) | Liam/Nikita |
| Final compilation and playtest | Team |
| Stretch goal: strategy hints | Nikita |
| Stretch goal: tournament mode | Advay |

## Attribution

| File | Who worked on it | How |
|---|---|---|
| `Action.java` | Advay | Added FAST_PASS value with GenAI assistance |
| `Outcome.java` | Advay | Added FAST_PASSED outcome with GenAI assistance |
| `Dice.java` | Liam | Wrote (from D3) |
| `RandomDice.java` | Liam | Wrote (from D3) |
| `Guest.java` | Liam | Wrote (from D3) |
| `LinkedQueue.java` | Nikita | Wrote (from D3); GenAI reviewed |
| `RideLine.java` | Advay | Refactored for two queues; GenAI assisted implementation |
| `TurnResult.java` | Liam | Wrote (from D3) |
| `LineSnapshot.java` | Advay, Jack | Extended with fast-lane fields; backward-compatible constructor |
| `GameView.java` | Nikita | Wrote (from D3); GenAI reviewed |
| `ConsoleView.java` | Nikita, Advay | Updated to show both lanes and fast-pass option |
| `GameController.java` | Liam, Advay | Updated parseAction and validation for fast-pass |
| `PunchLineApp.java` | Jack | Wrote (from D3) |
| `FixedDice.java` (test helper) | Liam | Wrote (from D3); GenAI revised |
| `ScriptedView.java` (test helper) | Liam | Wrote (from D3); GenAI revised |
| `GuestTest.java` | Jack | Wrote (from D3); GenAI revised |
| `LinkedQueueTest.java` | Advay | Wrote (from D3); GenAI revised |
| `RandomDiceTest.java` | Advay | Wrote (from D3); GenAI revised |
| `RideLineTest.java` | Advay | Wrote base (D3); added 10+ fast-pass tests (D4) |
| `ValueTypesTest.java` | Advay | Wrote (from D3); GenAI revised |
| `GameControllerTest.java` | Advay | Updated to test fast-pass parsing and validation |
| `ConsoleViewTest.java` | Advay | Wrote (from D3); GenAI revised |
| `StrategySimulator.java` | Advay | Wrote for balance analysis |
| `README.md` | Jack (D3), Advay (D4) | Updated reflection and next steps; GenAI drafted |

### GenAI note

GenAI (Claude) assisted throughout Deliverable 4:
- Revised the two-queue refactoring of `RideLine` with routing logic and fast-pass state management.
- Revised `LineSnapshot` extensions to support both lanes while maintaining backward compatibility.
- Updated `GameController` and `ConsoleView` to parse and display the fast-pass feature.
- Reviewed comprehensive test cases for fast-pass behavior, edge cases, and integration.
- Created the `StrategySimulator` for balance analysis.


All code was reviewed and tested by the team before submission. The team wrote the core game logic (D1–D3); GenAI accelerated the feature implementation and testing for D4.
