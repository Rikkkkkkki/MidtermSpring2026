# UNO Rules: Implemented Features

This document lists which UNO rules from `Final_Project_UNO_rules_reference.md` are implemented in this project, along with any variants or simplifications.

## Fully Implemented 

### Deck Composition
- ✅ Four colors (Red, Yellow, Green, Blue)
- ✅ One 0 card per color
- ✅ Two each of cards 1-9 per color
- ✅ Two Skip cards per color
- ✅ Two Reverse cards per color
- ✅ Two Draw Two cards per color
- ✅ Four Wild cards
- ✅ Four Wild Draw Four cards
- ✅ Total: 108 cards

### Legal Play Validation
- ✅ Match by color
- ✅ Match by number
- ✅ Match by action type (Skip on Skip, Reverse on Reverse, Draw Two on Draw Two)
- ✅ Wilds always legal
- ✅ Called color match (after wild, matching the called color is legal)
- ✅ Rejection of illegal plays

### Card Actions
- ✅ **Skip**: Next player loses turn; play continues to following player
- ✅ **Reverse**: Direction changes
    - With 3+ players: next player goes in opposite direction
    - With 2 players: acts as Skip (documented variant from original)
- ✅ **Draw Two**: Next player draws 2 cards and loses turn
- ✅ **Wild**: Player chooses next active color (R/Y/G/B)
- ✅ **Wild Draw Four**: Player chooses color; next player draws 4 cards and loses turn

### Draw & Pass Behavior
- ✅ Player can draw when holding no legal cards
- ✅ Drawn card may be played immediately if legal
- ✅ Bot auto-plays legal drawn cards
- ✅ Human is prompted whether to play drawn card
- ✅ Turn passes to next player if no card is played

### UNO Call & Penalty
- ✅ System detects when player reaches exactly 1 card
- ✅ UNO announcement displayed ("X says UNO!")
- ✅ Missed-UNO penalty: 2-card draw
    - Triggered when: player reaches 1 card, doesn't call UNO, and next player takes a turn
    - Penalty cards drawn at start of next player's turn
    - Penalty: draw 2 cards from deck

### Round & Game Flow
- ✅ Round ends when a player empties their hand
- ✅ Round winner declared and scored
- ✅ Remaining player cards tallied for winner's points
- ✅ Multiple rounds can be played in sequence
- ✅ Scores accumulate across rounds
- ✅ Game ends after specified number of rounds

### Scoring System
- ✅ Number cards: face value (0-9)
- ✅ Skip: 20 points
- ✅ Reverse: 20 points
- ✅ Draw Two: 20 points
- ✅ Wild: 50 points
- ✅ Wild Draw Four: 50 points
- ✅ Round winner receives sum of all opponent cards' values
- ✅ Scores displayed after each round
- ✅ Final scores shown at game end

### Data Persistence
- ✅ Game sessions stored in database
- ✅ Player records stored (score, win status)
- ✅ Historical game queries available
- ✅ Report commands for game statistics

## Variants & Simplifications

### Draw Two Stacking
-  **Not implemented**
- **Reason**: Adds complexity; basic project doesn't require it
- **Current behavior**: When Draw Two is played, exactly 2 cards are drawn (no cumulative stacking)

### Wild Draw Four Challenge
-  **Not implemented**
- **Reason**: Optional per rubric; basic project doesn't require it
- **Current behavior**: Wild Draw Four always succeeds; player chooses color, next player draws 4 and loses turn

### Two-Player Reverse Variant
-  **Implemented**: In 2-player games, Reverse acts as Skip (documented)
- **Reason**: Matches original project behavior
- **Rationale**: In 2-player, reversing would just return to the same player; Skip is more useful

### UNO Penalty Timing
-  **Implemented**: Penalty checked at START of next player's turn
- **Reason**: Simpler implementation; prevents immediate retaliation
- **Behavior**: If player has 1 card and didn't call UNO, they draw 2 when their turn next comes around

### Draw/Pass Variant
-  **Implemented**: Draw one, may play if legal
- **Reason**: More engaging than draw-and-pass
- **Behavior**:
    1. Player draws card
    2. If legal and bot: auto-play
    3. If legal and human: prompt to play
    4. If not played: turn passes

### Target Score
-  **Implemented**: Default 500 points
- **Configurability**: Not exposed via command-line args (can be changed in `Main.java`)
- **Reason**: Project requirements list multi-round games but don't mandate configurable target

### Bot Strategy
-  **Implemented**: Simple heuristic-based strategy
- **Card priority**: Draw Two > Skip > Number > Wild
- **Color selection**: Dominant color in hand (tiebreak: R > Y > G > B)
- **Reason**: Effective for gameplay without AI complexity

### Starting Card
-  **Implemented**: Ensures starting card is never a Wild
- **Reason**: Prevents cascade of wild cards at game start
- **Behavior**: If drawn Wild, discard and redraw until non-wild

## Not Implemented (Acceptable per Rubric)

### Official House Rules Not Included
- Jump-In rule
- Stacking rule for Draw cards
- Wild Draw Four challenge/stacking
- Play All rule (must play all playable cards)
- Uno Flip (two-sided deck variant)

### CLI Features Not Included
- Interactive menu for rule configuration
- Difficulty levels for bot
- Game replay from database
- Online multiplayer
- GUI interface

**Rationale**: All "not implemented" features are either explicitly marked as optional in the rubric or would require significant additional engineering beyond the scope of this course project.

## Compliance Checklist

| Requirement | Status | Notes |
|------------|--------|-------|
| Deck composition | ✅ | 108 cards, correct distribution |
| Legal play | ✅ | All match types supported |
| Skip | ✅ | Next player loses turn |
| Reverse | ✅ | Direction flip; Skip behavior with 2 players |
| Draw Two | ✅ | Next player draws 2 and loses turn |
| Wild | ✅ | Color selection implemented |
| Wild Draw Four | ✅ | Color + 4-card draw |
| Draw/Pass | ✅ | Draw one, may play if legal |
| UNO Call | ✅ | Auto-announce; penalty tracked |
| Scoring | ✅ | Correct card values; multi-round accumulation |
| Playable CLI | ✅ | Human + bots, clear prompts |
| Tested | ✅ | 96+ tests, all passing |
| Documented | ✅ | README, final-report, rules-supported (this file) |

## Testing Coverage

All implemented features are tested:
- `CardTest`: Card parsing, point values
- `RulesTest`: Legality, scoring, dominant color
- `BotStrategyTest`: Card/color selection
- `GameStateTest`: Deck, hands, turn order, score accumulation
- `FullGameTest`: Multi-round deterministic games

## File References

- **Game logic**: `GameEngine.java`, `GameState.java`, `Rules.java`
- **Card effects**: `GameEngine.applyCardEffect()` method
- **UNO tracking**: `GameState.unoCalledThisTurn`, `GameEngine.checkAndApplyUnoPenalty()`
- **Tests**: `src/test/java/*.java`