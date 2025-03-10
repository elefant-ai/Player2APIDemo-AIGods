
# AI Gods Example Project



# Setup Process:


### Step 1: 
Make sure you have some JDK installed. On windows we tested with OpenJDK21.

### Step 2: 
Extract the zip folder into a folder, then change directory into that folder.

### Step 3: 
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

### Step 4: 
To run the game, either in IntelliJ or Eclipse, open up the Gradle tab, then under `Tasks/forgegradle runs` there should be a gradle option for `runClient`. Simply click that and minecraft should open.








# Mapping Names:

By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license, if you do not agree with it you can change your mapping names to other crowdsourced names in your 
build.gradle. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md

# Additional Resources: 

Community Documentation: https://docs.minecraftforge.net/en/latest/gettingstarted/
LexManos' Install Video: https://youtu.be/8VEdtQLuLO0
Forge Forums: https://forums.minecraftforge.net/
Forge Discord: https://discord.minecraftforge.net/
