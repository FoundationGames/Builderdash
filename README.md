# Builderdash

Building-related party minigames for Minecraft, using Plasmid API on Fabric

**Server Side Only!** Vanilla clients can join any server or LAN-hosting-client with this mod installed.

### Pictionary

- Open a game of Pictionary with `/builderdash pictionary`!
- In this mode, each round will have one player build a prompt while the rest guess.
- Quicker guesses earn more points for both the guessers and builder.
- A hint will appear to guessers showing the letter count, slowly revealing a few letters from the prompt.
- As players guess, the time available for remaining players and the builder will diminish.
- **Adding custom prompt words:**
    - Compile a list of words separated by commas. EX: `Apple,Banana,Pear`
      - Word lists made for the game skribbl.io should work automatically, no reformatting needed!
    - If you want words to have aliases, specify them like such: `Hamburger=Burger,French Fries=Fries=Chips`. The first alias will be used as the hint, although players can guess any of them.
    - Run one of the following commands **(in a command block to allow high character counts)**:
      - `/builderdash pictionary addwords My,Word,List,Etc` to add your words to the built-in list
      - `/builderdash pictionary setwords My,Word,List,Etc` to replace the built-in list (and existing words) with your custom list
      - `/builderdash pictionary resetwords` will reset the word list to default
    - Run the game through `/builderdash pictionary`. **NOTE:** Running the game through `/game open` will not apply custom words.
    - Custom words are saved in each world. Other players can add their own custom words through these commands.

### Telephone

- Open a game of Telephone with `/builderdash telephone`!
- In this mode, players try to communicate prompts through a prompt and a series of builds and guesses.
  - The first round asks players to come up with an original prompt.
  - The second round will shuffle the prompts among the players, and each player has to build the prompt they receive.
  - The builds are then shuffled and distributed in the third round, where players now have to guess the prompt from the build.
  - This process repeats for as many players are in the game, unless playing in `double` mode.
  - Once finished, each original prompter gets to present their "gallery", to see how their prompt was (mis)interpreted through the builds and guesses.
- `/builderdash telephone double` will start a double-length game, where there are double as many rounds as there usually would be. This means that players might re-encounter their original prompt.
- `/builderdash telephone fast` will start a fast game, where building time is reduced by more than half. It's less likely that a prompt will be able to survive a game like this.
- `/builderdash telephone double fast` will start a double-length fast game. This might be the most fun way to play a double-length game.
- **Disconnected from a game of Telephone?** Rejoin it with `/game join builderdash:telephone` (game name might be different in fast/double mode). If you were originally in the game, you will be let back in and can resume where you left off.
