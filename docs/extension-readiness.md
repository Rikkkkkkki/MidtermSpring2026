# Extension Readiness

## Which Extension This Design Supports Best

**Adding a smarter bot strategy** is the most natural extension the current
design supports. `BotStrategy` is already isolated from the game loop and
depends only on `Rules` and `Card`, both of which are stable and well-tested.

## Where the Change Would Be Implemented

`BotStrategy` has two public methods:

```java
public static int chooseCard(List<Card> hand, Card upCard, String calledColor)
public static String chooseColor(List<Card> hand)
```

`GameEngine` calls these in exactly one place each. A smarter strategy could
be introduced by:

1. Extracting a `BotStrategy` interface with those two method signatures.
2. Renaming the current class to something like `DefaultBotStrategy`.
3. Implementing a new class (e.g. `SmartBotStrategy`) that implements the
   interface and reasons about, for example, hand size, opponent card counts,
   or discard pile contents.
4. Passing the chosen implementation into `GameEngine` via its constructor.

`Card`, `Rules`, and `GameState` already expose everything a smarter strategy
would need — hand contents, up-card, called color, and point values — without
any further structural changes.

## What Still Makes Change Difficult

**`BotStrategy` has no interface.** Because the current bot is a static utility
class, `GameEngine` calls it by class name. There is no substitution point yet.
Adding the interface above is a small but necessary step before any strategy
variation can be dropped in without modifying `GameEngine`.

**`GameState` fields are package-visible.** A strategy that wanted to inspect
the discard pile or estimate deck depth would read `state.discard` and
`state.deck` directly. This works but means `GameState` cannot protect its own
invariants, and a richer strategy could accidentally corrupt state.

**Card effects are handled by a switch in `GameEngine`.** Adding a new card
type means editing `applyCardEffect()`. If card effect logic were moved onto
`Card` itself (or into small handler objects), new card types could be added
without touching `GameEngine` at all.

**`calledColor` is a plain `String`.** It is an empty string when inactive and
a one-letter code when active. Any extension that reasons about wild state has
to know this convention implicitly. Wrapping it in a small value type or an
`Optional<String>` would make the contract explicit and reduce the chance of
bugs when extending wild behavior.