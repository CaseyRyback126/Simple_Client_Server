package org.example;

import java.util.ArrayList;

public class CustomerManagerSingleton {
    private static ArrayList<CustomerManager> clients;
    private CustomerManagerSingleton(){

    }

    public static ArrayList<CustomerManager> getInstance(){
        if (clients == null){
            clients = new ArrayList<>();
        }
        return clients;
    }
}
