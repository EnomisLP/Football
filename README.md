# Football Social Club

## Overview

The **Football Social Club** is a web application designed for football enthusiasts, enabling them to explore various football players, teams, and coaches in detail. The system allows users to interact with each other by following other users, players, coaches, or clubs, as well as manage their own football team. Registered users can also write, edit, and delete articles, with the ability to perform advanced queries. The application uses **MongoDB**, **Neo4j**, and **Spring Boot** for data management and backend services.

---

## Features

- **User Registration & Login**: Users can register, log in, and access personalized features.
- **Player Management**: Users can add football players to their teams and view detailed information about them.
- **Following System**: Users can follow and unfollow users, put like to coaches, clubs.
- **Advanced Queries**: Users can perform complex queries to retrieve detailed football data.
- **Article Creation & Management**: Users can write, edit, and delete articles about football.
- **Admin Privileges**: Admins have control over the entire application, including:
  - **CRUD operations** on players, coaches, and clubs.
  - **Global updates** applied to MongoDB and Neo4j databases.
  - Admin can also delete, update, and add new players, coaches, or clubs.

---

## Actors

1. **Admin**:
   - Can **create, edit, and delete** players, clubs, and coaches.
   - Admin can manage **global data** in MongoDB and Neo4j databases.

2. **Registered User**:
   - Can **follow/unfollow** players, coaches, clubs, and users.
   - Can view detailed data about football players, coaches, and clubs.
   - Can **create, edit, and delete** their own articles.

3. **Unregistered User**:
   - Can **search and view** details of players, coaches, clubs, and users but cannot perform any interactive operations like following or posting articles.

---

## Datasets

1. **EA Sports FC 24 Complete Player Dataset**:
   - **Source**: [Kaggle Dataset - EA Sports FC 24](https://www.kaggle.com/datasets/stefanoleone992/ea-sports-fc-24-complete-player-dataset?select=male_teams.csv)
   - **Description**: Contains detailed information about football players with 80 attributes for each.
   - **Volume**: 100 MB
   - **Variety**: Player data such as age, nationality, height, weight, club, etc.
   - **Velocity/Variability**: The dataset is updated periodically with new player data.

2. **Football News Articles**:
   - **Source**: [Kaggle Dataset - Football News](https://www.kaggle.com/datasets/hammadjavaid/football-news-articles?select=final-articles.csv)
   - **Description**: A collection of 12,000 football news articles scraped from multiple websites.
   - **Volume**: 43 MB
   - **Variety**: Includes articles from various football-related websites.
   - **Velocity/Variability**: The data has been scraped using **Selenium** and **BeautifulSoup** (bs4) from various sources like Goal.com, Skysports, and Tribuna.com.

---

## Technologies

- **MongoDB**: A NoSQL database used to store and manage player, coach, and team information.
- **Neo4j**: A graph database used to manage relationships between players, coaches, and teams.
- **Spring Boot**: The backend framework for the web application, responsible for creating APIs and handling business logic.

---

## Use Cases

- **Registered User**: A user can log in, add players to their team, follow other users, and interact with articles. They can also write and edit articles related to football.
- **Admin**: Admin users can manage all aspects of the system, including adding, updating, and deleting football players, coaches, and clubs. They also manage the consistency of data across MongoDB and Neo4j.
- **Unregistered User**: Can browse through football data (players, teams, coaches) and read articles but cannot perform any interactive actions like following others or posting articles.

---

## How It Works

1. **User Registration**: Users sign up to access additional features such as following users and creating articles.
2. **Player and Coach Management**: Users can explore detailed player and coach profiles, including statistics, past teams, etc.
3. **Article Management**: Users can contribute to the community by writing and managing articles on football-related topics.
4. **Admin Role**: Admins have full control of the backend data and can modify records in MongoDB and Neo4j as needed.

---

## Architecture

- **Frontend**: Must be implemented
- **Backend**: Spring Boot REST APIs to handle user requests, player data, and articles.
- **Database**: 
  - MongoDB for storing player, coach, and team data.
  - Neo4j to handle relationships between players, coaches, and teams (e.g., players in a team, coaches managing teams).
  
---

## How to Contribute

1. **Fork the repository** on GitHub.
2. **Clone** the repository to your local machine.
3. **Create a branch** for your feature or fix.
4. **Commit** your changes and push them to your fork.
5. **Submit a pull request** with a detailed description of your changes.

---
