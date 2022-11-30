# Wordle Solver KT
Fast, comprehensive solver for the Wordle game, written in Kotlin.

The New York Times Wordle game has 14,855 guessable words; this solver solves 91.5% within 6 tries.
Average solution time is 5.256 milliseconds.

For example, solving the word `skier`:
* lares => ⬛⬛🟨🟩⬛, 50 words remaining
* seter => 🟩⬛⬛🟩🟩, 19 words remaining
* siker => 🟩🟨🟨🟩🟩, 1 word remaining
* skier => 🟩🟩🟩🟩🟩, SOLVED!

## Setup
### Install Java and Gradle
I recommend installing Java and Gradle via [SDK Man](https://sdkman.io). Follow instructions there to download; then:
```shell
sdk install java 19-open
sdk install gradle 7.5.1
```

### Run Tests
```shell
./gradlew test
```

### Play and Solve
```shell
./gradlew Wordle
```

## How It Works
### 1. Generate Average Eliminated Per Word
We need a "best word" to guess with at first. As an offline process, try each word as an opening guess word against
each word as a game word. Count the number of guesses eliminated -- based on the response, how many words are no longer
available as a solution? Average the number of guesses eliminated per word.

This **average eliminated** will help us guess later. It will inform our starting word; the current winner is `lares`.
Also, we can cut down on guesses to make later.

Average Eliminated is generated by running `CreateMostEliminatedPipeline`, an Apache Beam pipeline. The output is Avro.
Download this Avro file to the `resources` dir to reference.

### 2. Identify Best Guess Word
There are 4 possible strategies for identifying best guess word, described below. Default strategy is `Balanced` -- it uses `Simple` for available count > 269, `TestAllScored` for
available count > 16, and `TestAllFull` for 16 or fewer. This strategy balances speed for larger available counts, with
improving guess accuracy as guesses are eliminated.

You can select strategy when you run `WordleSolver`. `Balanced` is found to offer the best performance/accuracy
tradeoff; though the other strategies exist if you'd like to test.

#### Simple
Order available guesses by **average eliminated** descending. Pick the top guess. This strategy is fast to run so we use
it for large data sets. It requires more guesses.

#### TestAllScored
Best guess word is identified by running all available guesses as game word against all guesses as solution word.
Test score is:
* 2 for Correct
* 1 for OtherSlot
* 0 for NotPresent

The available guess with the highest average score is selected.

This strategy is significantly faster to run than `TestAllFull`, but slower than `Simple`. Guess accuracy
(average guess count) is nearly as good as `TestAllFull`.

#### TestAllFull
Test all guesses against each other. The word which eliminates the most guesses on average is selected.

This strategy is comparatively slow so recommended only for small guess sets. It is a little more accurate than
`TestAllScored`.

#### Variety Guess
I often found that I got something like this:
* bates => ⬛🟩🟩🟩🟩

B is not present, but the other slots are `a`, `t`, `e`, `s`. This leaves a ton of guesses: dates, fates, gates,
hates, kates, lates, mates ... you get the idea.

A "variety guess" is intended to eliminate a lot of letters. Go through available guesses; prioritize guesses
containing letters which we have not seen yet (I.E. has never shown Correct or OtherSlot in guess results). The guess
must contain at least two such letters; thus it will eliminate at least two words.

A variety guess may not be found; in such a case we fall through to normal guess selection.

### 3. Eliminate Letters
We track a Letter Map -- a map containing all available letters per slot. It starts with `A - Z` in all 5 slots.
Upon receiving a guess result we update the letters per slot:
* Correct => Set that slot to one available letter
* OtherSlot => Remove that letter from this slot
* NotPresent => Remove that letter from all slots, except slots which are Correct

### 4. Eliminate Guesses
Given available letters, traverse the Word Tree to find available guesses.

## Performance
Running `SolveAllWords` plays the game for all words, then generates a JSON file like below from the most recent run.
```json
{
  "totalCount": 14855,
  "guessDistribution": {
    "1": 1,
    "2": 188,
    "3": 2042,
    "4": 5021,
    "5": 4337,
    "6": 1998,
    "7": 806,
    "8": 320,
    "9": 106,
    "10": 25,
    "11": 6,
    "12": 3,
    "13": 2
  },
  "averageTime": 5.256277347694379,
  "minTime": 0,
  "maxTime": 394,
  "timeDistribution": {
    "0": 45,
    "10": 12854,
    "20": 1832,
    "30": 90,
    "40": 17,
    "50": 6,
    "60": 6,
    "70": 2,
    "80": 2,
    "400": 1
  },
  "maxGuessWords": [
    "jills",
    "kacks",
    "cacks",
    "momos",
    "zills",
    "gangs",
    "jacks",
    "jests",
    "jowls",
    "mojos",
    "sills",
    "eales",
    "eases",
    "faxes"
  ]
}
```
`guessDistribution` shows number of words requiring each number of guesses (the 1 is "lares", always the first guess
word). New experiments can be tested against previous to ensure they shift guess counts down.

We care about performance, that's why we use Kotlin. `averageTime` and `timeDistribution` are in milliseconds -- 5.256
is pretty good.

`maxGuessWords` gives us words which took a lot of guesses. We can test them and see where guessing logic can be
improved.

## Future Improvements
This is the first version. Some things to improve in the future.

### Command Line Interface
At present, you can solve by running `WordlePlayerTest#testSolve()`. Solve all words by running `SolveAllWords`.
I need to create a command line interface. Also, users should be able to play the game from command line.

### Common Words
At present the game uses all 14,855 guessable words. This includes a ton which you've probably never seen -- Can you
use `syver` in a sentence?? More importantly, it's too many possibilities for 6 guesses. I think that by narrowing
down to common words, the solver will be able to solve nearly all within 6 guesses. For future work.
