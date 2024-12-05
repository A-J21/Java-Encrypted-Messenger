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
### Server  
