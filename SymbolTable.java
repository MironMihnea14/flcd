package com.company;
import java.util.Map;

// I chose to use the Hash Table Representation
// Symbol table uses a map as container
// The hash function is the ASCII sum of all chars
// To prevent collisions, i simply increment the hashKey until i no longer have collisions
// The pos method returns the hashKey if the element is not already in the map or -1 otherwise
// add method adds the Key Value Tuple to the map

class SymbolTable {
    private Map<Number, String> hashTable;

    private int ASCIIsum(String token) {
        int sum = 0;
        int tokenLength = token.length();

        for (int i = 0; i < tokenLength; i++) {
            sum += token.charAt(i);
        }

        return sum;
    }

    private int position(String token) {
        int hashKey = ASCIIsum(token);
        String hashedValue = hashTable.get(hashKey);
        if (hashedValue != null) {
            // if token is already found
            if (token.equals(hashedValue)) {
                return -1;
            }
            while (hashTable.containsKey(hashKey)) {
                hashKey += 1;
            }
            return hashKey;
        }
        return -1;
    }

    private void add(String token, Number hashKey) {
        if (hashKey.equals(-1)) {
            return;
        }
        hashTable.put(hashKey, token);
    }
}

public class Main {

    public static void main(String[] args) {
	// write your code here
    }
}

// 1. b
