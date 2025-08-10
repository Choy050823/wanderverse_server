# Wanderverse Backend

<p align="center">
  <a href="https://wanderverse-travel.netlify.app/">
    <img src="https://placehold.co/150x150/203040/FFFFFF?text=Wanderverse" alt="Wanderverse Logo">
  </a>
</p>

A robust and scalable backend for the Wanderverse travel app, featuring a social-sharing platform, an AI-driven trip planner, and a personalized recommendation engine. Developed by Team Quacking Bakchoi.

---

### Table of Contents
1.  [About the Project](#about-the-project)
2.  [Features](#features)
3.  [Technology Stack](#technology-stack)
4.  [Architecture Overview](#architecture-overview)
5.  [Getting Started](#getting-started)
6.  [Team](#team)

---

### About the Project

The Wanderverse backend is the core engine for a unique mobile application that blends a practical travel platform with a casual puzzle game. Our primary goal is to provide a seamless and high-performance service that handles user data, post management, and advanced AI functionalities. It is designed to be modular and scalable, ensuring a great user experience even as the app grows.

---

### Features

This backend was built incrementally across three milestones, and includes the following key features:

#### Core Services
* **RESTful APIs**: Provides a full suite of RESTful endpoints for user management, post creation, and interaction (e.g., likes, comments).
* **JWT Authentication**: A secure system using JSON Web Tokens to handle user login, session management, and protect all sensitive API endpoints.
* **AWS Integration**: Leverages Amazon Web Services for a scalable and reliable cloud infrastructure.
    * **AWS S3**: For storing and retrieving user-uploaded photos.
    * **AWS RDS**: Hosts the PostgreSQL relational database.
    * **AWS EC2**: Serves the application, configured behind an Application Load Balancer for high availability.

#### Advanced Services
* **AI-Powered Trip Planner**: Utilizes the Google Gemini 2.0 Flash Lite model to act as an agentic AI for creating personalized and detailed travel itineraries. The AI uses **automatic tool calling** to retrieve real-time data from various Google APIs (e.g., Maps, Place Details) to enrich its output.
* **Personalized Recommendation Engine**: A sophisticated system that uses **Google Gemini Text Embedding** to generate semantic vectors of user preferences and posts. These vectors are stored in the **Qdrant vector database** to provide highly relevant, personalized content recommendations.
* **Post Search Engine**: Implements a semantic search engine using Google Gemini Text Embedding and Qdrant, allowing users to find posts and destinations with natural language queries rather than exact keywords.
* **Asynchronous Tasks**: Integrated with a messaging queue (**RabbitMQ/AmazonMQ**) to decouple synchronous operations from tasks like sending notifications or processing recommendation updates, ensuring a responsive user experience.
* **Caching**: Uses **Redis** (locally) and **AWS ElastiCache** (in production) to cache frequently accessed data, reducing database load and latency.
* **Security**: Includes implemented rate limiting and DDoS protection measures to safeguard the API from malicious activity.

---

### Technology Stack

* **Framework**: Java Spring Boot
* **Language**: Java
* **Database**: PostgreSQL
* **Vector Database**: Qdrant
* **AI Model**: Google Gemini 2.0 Flash Lite
* **Caching**: Redis / AWS ElastiCache
* **Messaging Queue**: RabbitMQ / AmazonMQ
* **Cloud Services**: AWS (S3, RDS, EC2, ALB)

---

### Architecture Overview

The backend is built as a set of interconnected microservices, ensuring modularity and scalability. Requests are routed through an AWS Application Load Balancer to multiple EC2 instances running the Spring Boot application. The application interacts with a PostgreSQL database on AWS RDS and stores media files in an S3 bucket. Asynchronous tasks are handled by a messaging queue, while a Redis cache improves the performance of frequent data lookups. The Qdrant vector database is integrated to power the advanced search and recommendation features.

---

### Getting Started

_Instructions on how to clone the repository, set up the development environment, configure AWS credentials, and run the backend locally would go here. This would typically include steps for setting up the PostgreSQL and Qdrant databases, and configuring environment variables for AWS keys and other secrets._

**Note**: When testing the application, please avoid using the NUS Wifi as the API endpoints may be blocked by the firewall.

---

### Team

**Team Quacking Bakchoi**
* Kuek Yi Hong
* Choy Min Han
