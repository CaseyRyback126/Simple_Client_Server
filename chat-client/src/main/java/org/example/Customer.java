package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Customer {
    private static int lastId = 0;
    private final int id;
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final String name;
    private static final Logger logger = Logger.getLogger(Customer.class.getName());


    public Customer(int id, Socket socket, String name) {
        this.id = assignId();
        this.socket = socket;
        this.name = name;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    private int assignId() {
        return ++lastId;
    }

    /**
     * Отправить сообщение
     */
    public void sendMessage(int recipientId) {
        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                bufferedWriter.write(recipientId + ": " + name + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public  void sendMessage(){
        sendMessage(0);// 0 - идентификатор для рассылки всем
    }

    /**
     * Слушатель для входящих сообщений
     */
    public void listenForMessage() {
        new Thread(() -> {
            String message;
            while (socket.isConnected()) {
                try {
                    message = bufferedReader.readLine();
                    int recipientId = extractRecipientId(message);
                    if (recipientId == this.id) {
                        showMessage(message);
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    private void showMessage(String message) {
        String content = extractMessageContent(message);
        System.out.println(content);
    }

    private String extractMessageContent(String message) {
        String[] parts = message.split(":");
        return parts[2]; // возвращаем текст сообщения
    }

    private int extractRecipientId(String message) {
        int recipientId = -1;

        try {
            String[] parts = message.split(":");
            if(parts.length != 3) {
                throw new IllegalArgumentException("Некорректный формат сообщения");
            }

            recipientId = Integer.parseInt(parts[0]);

        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE,"Ошибка распознавания id: " + message, e);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE,"Ошибка чтения сообщения: " + message, e);
        }

        return recipientId;
    }

    /**
     * Завершение работы всех потоков, закрытие клиентского сокета
     *
     * @param socket         клиентский сокет
     * @param bufferedReader буфер для чтения данных
     * @param bufferedWriter буфер для отправки данных
     */
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Завершаем работу клиентского сокета
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
