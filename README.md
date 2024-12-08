# Beacon Server and Client Communication System  

## Overview  
This project demonstrates a secure client-server communication system implemented in Java. The system includes a **server** that handles multiple client connections and facilitates message broadcasting, and a **client** application that connects to the server, encrypts messages, and ensures secure communication.  

## Features  
- **Multi-client Support**: Server supports multiple clients simultaneously.  
- **Authentication**: Clients authenticate using unique User IDs.  
- **Message Encryption**: Messages are securely encrypted using XOR and SHA-256-based keys.  
- **Broadcast Messaging**: Server broadcasts messages to all connected clients except the sender.  
- **Timestamped Messages**: Each message includes a timestamp.  

## Technologies Used  
- **Java**: Programming language for both server and client applications.  
- **Socket Programming**: For communication between server and clients.  
- **SHA-256**: Secure hashing algorithm used to generate encryption keys.  
- **XOR Encryption**: Lightweight encryption/decryption method for secure messaging.  

## File Structure  
- **BeaconServer.java**: Handles server operations, including client management and broadcasting.  
- **Client.java**: Client-side application for connecting to the server and sending/receiving messages.  
- **Message.java**: Represents a message with metadata like content, sender, and timestamp.  

## How to Run  

1. **Login**: Enter any desired **User ID** in the corresponding field. This **User ID** will be visible to other users.
2. **Server Address**: Enter the **server address** (IP or domain). If testing locally, use `"localhost"` or `"127.0.0.1"` (without quotes). If no address is entered, the default behavior will use `"127.0.0.1"`.
3. **Login**: After filling out the **User ID** and **Server Address**, click **Login** or press **Enter** while in one of the text fields.
4. **Message Sending**: Once logged in, you can compose and send messages. These messages will be visible to all other logged-in users, and their **User IDs** will appear in the **user list** on the right.
   - Messages are **encrypted** but can be read by all users logged into the server.
   - The server can see the encrypted message and the sender’s information, but it **cannot read** the plaintext message content.
5. **Disconnect**: To disconnect and return to the login screen, click the **Disconnect** button at the top left. You can also close the window to terminate the program.
6. **Terminate Server (if running)**: If you are running the server, you can terminate it by using the key combination **Ctrl + C** in the terminal window where the server is running.


