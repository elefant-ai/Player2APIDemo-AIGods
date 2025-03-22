package com.elefantai.aigods;

import java.util.Scanner;

public class TestSTT {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Listening for input ('start' or 'stop'):");

        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();

            if ("start".equals(input)) {
                Player2APIService.startSTT();
            } else if ("stop".equals(input)) {
                String result = Player2APIService.stopSTT();
                System.out.printf("Result: '%s'%n", result);
            } else if ("exit".equals(input)) {
                System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Invalid input. Type 'start', 'stop', or 'exit' to quit.");
            }
        }

        scanner.close();
    }
}
