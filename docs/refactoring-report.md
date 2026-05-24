# Refactoring Report

## What Behavior I Characterized Before Refactoring

Before extracting any classes, I added 83 characterization tests across four
focused suites to pin the behavior I needed to protect:

**`CardTest` (30 checks):** Every card code the deck builder produces was
tested for correct color, rank, number, point value, code round-trip, and
toString. This locked down the primitive string card representation before
replacing it with a value object.

**`RulesTest` (26 checks):** All six legal-play paths from the original
`Main.isLegal()` — color match, number match, action-type match, wild always
legal, called-color after wild, and illegal combinations — were pinned before
extraction. Scoring and dominant-color helpers were covered too. One quirk
captured: when a hand contains only wilds, `dominantColor()` returns `"R"` not
`"B"` because all color counts are zero and red wins the tie-break first.

**`GameStateTest` (17 checks):** Deck composition (108 cards: 76 number, 24
action, 8 wild), initial deal of 7 cards per player, the rule that the up-card
is never a wild, reshuffle-from-discard when the deck empties, the wild
fallback when both piles are empty, turn-direction wrapping in both directions,
and score accumulation.

**`BotStrategyTest` (10 checks):** The exact priority order
(Draw Two → Skip → Number → Wild), called-color respect, draw when nothing is
playable, and dominant-color selection.

All 83 checks run with one command:

```bash
scripts/test.sh
```

## Worst Design Problems Found

**One giant class.** The original `Main` mixed card representation, legality
checks (duplicated three times), bot logic, console I/O, deck management,
scoring, and the full game loop in a single file. Any change risked breaking
unrelated behavior.

**Primitive card representation.** Cards were raw strings like `"R5"` or
`"G+2"`. Color, rank, number, and point value were re-parsed by scattered
helper methods throughout the class, making the logic fragile and impossible
to test in isolation.

**Duplicated legality logic.** The `isLegal` check appeared in three separate
places in the game loop. A rule change required finding and updating all three
copies consistently.

**Global mutable state.** Deck, discard, hands, scores, current player, and
direction were all static fields on `Main`. Running two independent games in
the same process was impossible.

**Console I/O tangled with game logic.** `System.out` calls and `Scanner`
reads were scattered throughout the game loop, making rule logic impossible to
test without a terminal.

**Bot decisions mixed with rule knowledge.** The bot's card-selection loop
re-implemented legality checks inline rather than calling a shared rule
evaluator, so bot behavior and game rules could silently diverge.

**Scoring mixed with game completion.** The winner check and the score
calculation happened in the same block, making either hard to change
independently.

## Refactorings Performed

**Extract Class — `Card`.**
The primitive string card was replaced with an immutable value object. All
card knowledge — parsing, color, rank, number, point value — lives in one
place and is fully testable without any game infrastructure.

**Extract Class — `Rules`.**
The three duplicated legality checks were unified into a single
`Rules.isLegal()` method. Scoring (`scoreHand`) and dominant-color selection
(`dominantColor`) moved here too. Rules are now testable in complete isolation
with no CLI, no scanner, and no game loop.

**Extract Class — `GameState`.**
All global static fields were moved into a single owned object. The game loop
operates on this state; multiple independent game instances can now coexist
without interfering. `resetForNewRound()` handles deck-building and dealing,
preserving the original shuffle and deal order exactly.

**Extract Class — `BotStrategy`.**
Bot card selection and color selection were extracted into a stateless utility
class that delegates legality checking to `Rules`. The strategy is testable
with just a hand and an up-card, and a smarter bot can be substituted without
touching the game loop.

**Extract Class — `ConsoleView`.**
All `System.out` calls and `Scanner` reads were consolidated here. `GameEngine`
calls the view for all output and human input and never writes to `System.out`
directly. This boundary makes the display replaceable without touching rule
logic.

**Extract Class — `GameEngine`.**
The main game loop was extracted from `Main` into its own class. It
orchestrates turn flow by delegating to `GameState`, `Rules`, `BotStrategy`,
and `ConsoleView`. No `System.out` calls appear in `GameEngine`.

**Thin `Main`.**
`Main` now only parses arguments, builds collaborators, and runs rounds.
The `--self-test` path was updated to run all four characterization suites
via `TestRunner` so `scripts/test.sh` continues to work unchanged and now
runs all 83 checks instead of the original 9.

**Add Test Infrastructure — `TestRunner` and four suites.**
A lightweight `TestRunner` (no framework dependency) was added to `src/` so
the provided `compile.sh` picks it up with `src/*.java`. All four test classes
live in `src/` for the same reason.

## Behavior Intentionally Preserved

All behavior documented in `docs/rules.html` was preserved exactly, including
these quirks:

- All hands are visible in the terminal during a human game.
- A human can type `draw` even when holding a legal card.
- Typing an invalid card index causes a penalty card and turn loss.
- Bot players automatically play a drawn card when it is legal.
- With two players, Reverse acts as Skip.
- The up-card at the start of a round is never a wild.
- When both deck and discard are empty, `drawFromDeck()` returns a wild card
  as a safety fallback.

## Risks That Remain

**Package-visible fields on `GameState`.** Fields like `deck`, `discard`,
`upCard`, and `calledColor` are package-visible. `GameEngine` accesses them
directly, so `GameState` cannot enforce its own invariants.

**Switch statement for card effects.** `GameEngine.applyCardEffect()` switches
on `Card.Rank`. Adding a new card type requires modifying this method rather
than extending it.

**`BotStrategy` has no interface.** The bot is a static utility class.
Swapping in a different strategy requires changing call sites in `GameEngine`
rather than substituting an object.

**`ConsoleView` is a concrete dependency.** `GameEngine` takes a `ConsoleView`
directly. Replacing the display with a GUI or a test double requires either
subclassing or changing the constructor.