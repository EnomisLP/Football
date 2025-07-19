# Football Social Club

## Overview and Motivations

 Football is the world’s most popular sport, captivating billions with its dynamic
 interplay of skill, strategy, history, and passionate communities. In the digital
 era, the hungerfordata-driveninsights, advancedanalytics, andsociallyconnected
 experiences has transformed how fans, analysts, and professionals engage with
 the game. Yet, the landscape of football data remains fragmented-spread across
 disparate sources, siloed in varying formats, and often disconnected from the social
 and analytical features that modern users expect.
 **The Football Social Club** project was conceived to address this gap, building
 an integrated, scalable, and extensible platform that unites rich football datasets,
 advanced analytics, and community-driven features. By merging traditional data
 modelingwithstate-of-the-artNoSQLandgraphdatabasetechnologies,thisproject
 sets out to redefine how football data is collected, explored, and experienced.

---

## Actors and Requirments

1. **Unregistered Users (Guests)**
   **Functional Requirements**
   
 • FR-1:Must be able to create an account, specifying username, email,password
 and nationality.

 • FR-2: Must be able to search for basic information about teams, players,
 coaches, and users.
 
2. **Registered Users (Authenticated)**
 **Functional Requirements**
   
 • FR-3: A user must be able to log in and log out.
 
• FR-4: A user must be able to update personal information (Password) and to
 delete their account.
 
 • FR-5: A user must be able to create, edit, and delete their own articles.
 
 • FR-6: A user must be able to follow other users, like or unlike articles, teams,
 players and coaches.
 
 • FR-7: A user must be able to search for detailed information about articles,
 teams, players, coaches, and other users and to see its own ones.
 
 • FR-8: Users must be able to create and manages it’s own female/male team,
 by adding or removing players of any fifa version (corresponds to a version in
 a specific year) available.
 
 3.  **Administrators**
 **Functional Requirements**
 
 • FR-9 : Admins must be able to log in and log out.
 
 • FR-10: Admins must be able to view all users, teams, coaches, players, articles
 and their full details in batch.
 
 • FR-11: Admins must be able to delete user accounts.
 
 • FR-12: Admins must be able to moderate articles, by deleting them if neces
sary.

 • FR-13: Admins must be able to get analytics from the data.
 
 • FR-14: Admins must be able to access audit logs for all administrative actions.
 
 • FR-15: Admins must be able to manually sync data between MongoDB and
 Neo4j.
 
 • FR-16: Admins must be able to execute CRUD operations on teams, players,
 and coaches.
 
 • FR-17: Admins must be able to monitor system health and performance.
 
 4. **Non-Functional Requirements**
 
 • NFR-1: The system must ensure strong intra-db consistency for administra
tor write operations. User writes operations will follow an eventual consis
tency model to improve performance and scalability.

 • NFR-2: Databases must handle details,regarding players,teams,and coaches
 at maximum for 10 years.
 
 • NFR-3: All user passwords must be encrypted using bcrypt.
 
 • NFR-4: The application must be fast and return quick responses to its users
 in every query.
 
• NFR-5: The application must be user-friendly for all its users and easy to
 navigate

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

 • **Java 17** – Serves as the primary programming language for backend devel
opment, offering modern language features and long-term support.
 • **SpringBoot3.4.4**  – A robust framework used to build and manage the REST
ful API layer, simplifying dependency management and application configu
ration.
 • **MongoDB** – A NoSQL document-oriented database used for storing core do
main data with flexible schema design.
 • **Neo4j** – A native graph database designed to manage and efficiently query
 complex relationship-based data.
 • **Swagger UI** – Integrated for generating interactive API documentation, en
abling testing and exploration of REST endpoints.
 • **Maven 4.0** – Used for project build automation and managing dependencies
 across modules.
 • **Docker** – Employed to containerize application components, ensuring consis
tent environments across development and production.
 • **ApacheKafka 7.4.0** – A distributed event streaming platform used for han
dling asynchronous communication between services.
 • **Apache ZooKeeper 3.8.0** – Provides coordination and configuration man
agement for Kafka clusters.

---



## How to Contribute

1. **Fork the repository** on GitHub.
2. **Clone** the repository to your local machine.
3. **Create a branch** for your feature or fix.
4. **Commit** your changes and push them to your fork.
5. **Submit a pull request** with a detailed description of your changes.

---
