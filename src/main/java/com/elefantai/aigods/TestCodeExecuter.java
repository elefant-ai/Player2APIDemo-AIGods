package com.elefantai.aigods;

public class TestCodeExecuter {
    // to test, run this:
    public static void main(String[] args) {
        String code = generateCode();
        System.out.println("Generated code: " + code);
        try {
            CodeExecutor.executeCode(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateCode() {
        return "package com.elefantai.player2api;\n\n" +
                "class DynamicClass {\n" +
                "    public static void execute() {\n" +
                "        Player2ExampleMod.sendChat(\"Hello from code\");\n" +
                "        System.out.println(\"Direct message from code\");\n" +

                "    }\n" +
                "}";
    }
}
