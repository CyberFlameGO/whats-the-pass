/*
 * Copyright 2020 DarksideCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.whatsthepass;

import me.darksidecode.kantanj.crypto.AES256Encryptor;
import me.darksidecode.kantanj.crypto.Encryptor;
import me.darksidecode.kantanj.formatting.CommonJson;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WhatsThePass {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("================================= Whats The Pass ================================");
        System.out.println();
        System.out.println("    Welcome! This utility allows you to restore your password or");
        System.out.println("other secret data using a previously saved file encrypted with answers");
        System.out.println("to secret questions that are only known to you. For detailed tutorial");
        System.out.println("please visit this program's GitHub page:");
        System.out.println();
        System.out.println("    https://github.com/MeGysssTaa/whats-the-pass");
        System.out.println();
        System.out.println("    Please choose what do you want to do this time:");
        System.out.println();
        System.out.println("        (1) generate an encrypted file with secret questions that");
        System.out.println("            can be used to restore your password or other secret");
        System.out.println("            data in the future;");
        System.out.println();
        System.out.println("        (2) restore your forgotten password or other secret data");
        System.out.println("            using a previously saved encrypted file and secret questions.");
        System.out.println();
        System.out.println("    >> Type 1 or 2 and hit ENTER.");
        System.out.println();
        System.out.println("================================================================================");
        System.out.println();

        String action = scanner.nextLine().trim();

        System.out.println();
        System.out.println("User input in this program is NOT HIDDEN. Make sure no one");
        System.out.println("is watching you so that everything you enter is kept secret!");
        System.out.println();

        switch (action) {
            case "1":
                processCreate(scanner);
                break;

            case "2":
                processRestore(scanner);
                break;

            default:
                System.err.println("Unrecognized action (you should have typed 1 or 2, see above).");
                break;
        }
    }

    private static void processRestore(Scanner scanner) {
        System.out.println(">> Enter the absolute (full) path to the recovery file you want");
        System.out.println("   to restore your password or other secret data from. If the file");
        System.out.println("   is in the same folder where WhatsThePass JAR is, then just specify");
        System.out.println("   the name of the file:");

        String filePath = scanner.nextLine().trim();
        System.out.println();

        File inputFile = new File(filePath);

        // Let's be more lenient to user input here (accept all of
        // these: 'recovery', 'recovery.wtp' and 'recovery.wtp.json').
        if (!(inputFile.isFile())) {
            inputFile = new File(filePath + ".wtp.json");

            if (!(inputFile.isFile())) {
                inputFile = new File(filePath + ".json");

                if (!(inputFile.isFile())) {
                    System.err.println("The specified file does not exist (or is actually a folder).");
                    return;
                }
            }
        }

        try {
            String inputJson = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
            RecoveryFile recoveryFile = CommonJson.fromJson(inputJson, RecoveryFile.class);
            String comment = recoveryFile.getComment();

            if (!(comment.isEmpty())) {
                System.out.println("There's an extra comment written in this recovery file. It may be useful:");
                System.out.println(comment);
                System.out.println();
            }

            List<String> questions = recoveryFile.getQuestions();
            List<String> salts = recoveryFile.getSalts();
            List<String> answers = new ArrayList<>();

            int questionNum = 0;

            for (String question : questions) {
                System.out.printf("QUESTION #%d: %s. Answer (NOT CASE-SENSETIVE):\n", ++questionNum, question);
                String answer = scanner.nextLine().trim().toLowerCase();

                if (answer.isEmpty()) {
                    System.err.println("Answer cannot be empty.");
                    return;
                }

                answers.add(answer);
                System.out.println();
            }

            System.out.println("Decrypting...");

            byte[] decryptedBytes = Base64.decodeBase64(recoveryFile.getEncryptedData()); // not decrypted yet

            try {
                for (int i = answers.size() - 1; i >= 0; i--) { // reverse order
                    byte[] salt = Base64.decodeBase64(salts.get(i));

                    Encryptor enc = new AES256Encryptor(answers.get(i).toCharArray(), salt);
                    decryptedBytes = enc.decrypt(decryptedBytes);
                }

                byte[] plainBytes = Base64.decodeBase64(decryptedBytes);
                String plainData = new String(plainBytes, StandardCharsets.UTF_8);

                System.out.println("Successfully recovered your secret data/password! Here it is:");
                System.out.println();
                System.out.println(plainData);
            } catch (Exception ex) {
                System.err.println("Failed to decrypt the data saved in the specified recovery file.");
                System.err.println("Perhaps one or more of your answers to the secret questions is/are incorrect?");
            }
        } catch (Exception ex) {
            System.err.println("Failed to read the specified recovery file. It is likely that it is invalid.");
            System.err.println("The error that occurred was:");
            System.err.println();

            ex.printStackTrace();
        }
    }

    private static void processCreate(Scanner scanner) {
        System.out.println(">> How many secret questions would you like to use to encrypt your data?");
        String questionsNumStr = scanner.nextLine().trim();

        int questionsNum;

        try {
            questionsNum = Integer.parseInt(questionsNumStr);
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number.");
            return;
        }

        if (questionsNum < 1) {
            System.err.println("The number of questions used for encryption cannot be less than one.");
            return;
        }

        System.out.println();

        List<String> questions = new ArrayList<>();
        List<String> encKeys = new ArrayList<>();

        for (int i = 0; i < questionsNum; i++) {
            System.out.printf(">> Enter question number #%d:\n", i + 1);
            String question = scanner.nextLine().trim();

            if (question.isEmpty()) {
                System.err.println("Question text cannot be empty.");
                return;
            }

            questions.add(question);

            System.out.printf(">> Enter answer to question #%d (NOT CASE-SENSETIVE):\n", i + 1);
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.isEmpty()) {
                System.err.println("Answer text cannot be empty.");
                return;
            }

            encKeys.add(answer);
            System.out.println();
        }

        System.out.println(">> Enter the data you want to encrypt using answers to these secret");
        System.out.println("   questions (for example, your password manager master password):");
        String plainData = scanner.nextLine();
        System.out.println();

        if (plainData.isEmpty()) {
            System.err.println("The data to encrypt cannot be empty.");
            return;
        }

        System.out.println(">> Enter an extra comment (or maybe a hint) that will be displayed");
        System.out.println("   when recovering data/password using this file (may be empty):");
        String comment = scanner.nextLine();
        System.out.println();

        System.out.println(">> Great! Now enter the name of the file where you want to save the");
        System.out.println("   encrypted data and secret questions (e.g. 'master-pass-recovery'):");
        String fileName = scanner.nextLine();
        System.out.println();

        File outputFile = new File(fileName + ".wtp.json"); // 'wtp' for 'whats the pass'

        if (outputFile.exists()) {
            System.err.printf("File '%s' already exists.\n", outputFile.getAbsolutePath());
            return;
        }

        System.out.println("Encrypting...");

        byte[] plainBytes = plainData.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = Base64.encodeBase64(plainBytes); // not encrypted yet

        SecureRandom srnd;

        try {
            srnd = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Failed to retrieve a strong instance of SecureRandom. The error that occurred was:");
            System.err.println();

            ex.printStackTrace();

            return;
        }

        List<String> salts = new ArrayList<>();
        byte[] salt = new byte[8];

        for (String key : encKeys) {
            srnd.nextBytes(salt);

            Encryptor enc = new AES256Encryptor(key.toCharArray(), salt);
            encryptedBytes = enc.encrypt(encryptedBytes);

            salts.add(Base64.encodeBase64String(salt));
        }

        String encryptedData = Base64.encodeBase64String(encryptedBytes);
        RecoveryFile recoveryFile = new RecoveryFile(questions, salts, encryptedData, comment);
        String outputJson = CommonJson.toJson(recoveryFile);

        try {
            Files.write(outputFile.toPath(), outputJson.getBytes(StandardCharsets.UTF_8));

            System.out.println("Created recovery file successfully!");
            System.out.println("Now you can use WhatsThePass with this file in case you ever forget the");
            System.out.println("secret data you entered (make sure to always remember the secret answers though):");
            System.out.println();
            System.out.println("    " + outputFile.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("An unexpected error occurred while attempting to save the output data:");
            System.err.println();

            ex.printStackTrace();
        }
    }

}
