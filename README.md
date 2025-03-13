
# Player 2 API Demo (AIGods)

This is a Minecraft Forge mod to demo the Payer2 API. 

**Important Note:** To use this mod (either as a developer or a user), you must have the [Player 2 app](https://player2.game/) open and running.



### What does this mod do
This mod allows the currently selected character from the Player2 app to act as an "AI God" that can execute commands and chat to the user in the game Minecraft.






# If you just want the mod:
### Step 1
Make sure you have the [Player 2 app](https://player2.game/) installed.
### Step 2
Download and install [forge](https://files.minecraftforge.net/net/minecraftforge/forge/).
### Step 3
Download the latest release of the mod (.jar file) [here](https://github.com/elefant-ai/Player2APIDemo-AIGods/releases).
### Step 4
Then install via forge (drag into .mincraft/mods)
### Step 5
Run the Player2 app, then open minecraft. Open a singleplayer world, and the character should greet you with a message. Simply type in chat, and the character will respond with either chat messages, commands, or both.

# Development Setup:
### Step 1:
Make sure you have the [Player 2 app](https://player2.game/) installed.

### Step 2: 
Make sure you have some JDK installed. On windows we tested with OpenJDK21.

### Step 3: 
Extract the zip folder into a folder, then change directory into that folder.

### Step 4: 
Depends on what IDE you wish to use:

IntelliJ:
   1. Open IDEA, and import project.
   2. Select your build.gradle file and have it import.
   3. Run the following command: `./gradlew genIntellijRuns`
   4. Refresh the Gradle Project in IDEA if required.

Eclipse:
   1. Run the following command: `./gradlew genEclipseRuns`
   2. Open Eclipse, Import > Existing Gradle Project > Select Folder 
      or run `gradlew eclipse` to generate the project.

If at any point you are missing libraries in your IDE, or you've run into problems you can 
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
(this does not affect your code) and then start the process again.

### Step 5:
Before running the game, make sure the Player2 app is open.

### Step 6: 
To run the game, either in IntelliJ or Eclipse, open up the Gradle tab, then under `Tasks/forgegradle runs` there should be a gradle option for `runClient`. Simply click that and minecraft should open.

## API Docs
Once the Player2 app is running, the docs can be found by opening [localhost:4315/docs](localhost:4315/docs) on any web browser.








# Mapping Names:

By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license, if you do not agree with it you can change your mapping names to other crowdsourced names in your 
build.gradle. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md

# Additional Resources: 
Elefant AI discord: https://elefant.gg/discord
Community Documentation: https://docs.minecraftforge.net/en/latest/gettingstarted/
LexManos' Install Video: https://youtu.be/8VEdtQLuLO0
Forge Forums: https://forums.minecraftforge.net/
Forge Discord: https://discord.minecraftforge.net/
