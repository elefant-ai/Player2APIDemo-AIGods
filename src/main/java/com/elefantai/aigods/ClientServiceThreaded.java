package com.elefantai.aigods;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.server.MinecraftServer;

public class ClientServiceThreaded {
    private static final ExecutorService IO_POOL = Executors.newCachedThreadPool();

    public static CompletableFuture<Character> updateNewCharacter(Player2ExampleMod mod) {
        return CompletableFuture
                .supplyAsync(Player2APIService::getSelectedCharacter, IO_POOL)
                .thenApplyAsync(newChar -> {
                    Character current = mod.getCharacter();
                    if (current != null && current.name.equals(newChar.name)) {
                        return current;
                    }
                    // now change on main thread:
                    MinecraftServer server = Player2ExampleMod.server;
                    server.execute(() -> {
                        mod.setCharacter(newChar);

                        String newPrompt = Utils.replacePlaceholders(
                                Player2ExampleMod.getInitialPrompt(),
                                Map.of("characterDescription", newChar.description,
                                        "characterName", newChar.name));

                        ConversationHistory hist = mod.getConversationHistory();
                        if (hist == null)
                            mod.setConversationHistory(new ConversationHistory(newPrompt));
                        else
                            hist.setBaseSystemPrompt(newPrompt);

                        System.out.printf("Switched character to %s%n", newChar.name);
                    });
                    return newChar;
                }, IO_POOL);
    }

    public static void processPlayerMessage(Player2ExampleMod mod, String rawMsg) {
        mod.sendUserMessage(rawMsg);

        updateNewCharacter(mod)
                .thenComposeAsync(newChar -> {
                    String processed = mod.addPlayerStatusToUsrMessage(rawMsg);
                    mod.addProcessedUserMessage(processed);
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            return Player2APIService.completeConversation(mod.getConversationHistory());
                        } catch (Exception ex) {
                            throw new CompletionException(ex);
                        }
                    }, IO_POOL);
                }, Player2ExampleMod.server)
                .thenAcceptAsync(resp -> {
                    String respAsString = resp.toString();
                    System.out.println("Handling LLM Response (main thread): " + respAsString);
                    mod.addAssistantResponse(respAsString);

                    String commandString = Utils.getStringJsonSafely(resp, "command");
                    String msgForChat = Utils.getStringJsonSafely(resp, "message");

                    mod.executeCommandString(commandString);
                    mod.sendCharacterMessage(msgForChat);
                    if (mod.getShouldSpeak()) {
                        CompletableFuture.runAsync(
                                () -> Player2APIService.textToSpeech(msgForChat, mod.getCharacter()),
                                IO_POOL).exceptionally(ex -> {
                                    System.err.println("ERROR");
                                    ex.printStackTrace();
                                    return null;
                                });
                    }
                }, Player2ExampleMod.server)
                .exceptionally(ex -> {
                    System.err.println("ERROR");
                    ex.printStackTrace();
                    return null;
                });
    }

    public static void startSTT() {
        CompletableFuture.runAsync(Player2APIService::startSTT, IO_POOL)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public static void stopSTTAndProcess(Player2ExampleMod mod) {
        CompletableFuture
                .supplyAsync(Player2APIService::stopSTT, IO_POOL)
                .thenAcceptAsync(
                        sttText -> {
                            if (sttText.isEmpty())
                                return;
                            processPlayerMessage(mod, sttText);
                        },
                        Player2ExampleMod.server)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
