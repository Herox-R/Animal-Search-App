/*
* Programmer: Sylvester N
* File: This_file_creates_tables_and_uses_database
*/
-- Create the database (AnimalDatabase)


-- Use the database
USE AnimalDatabase;

-- Create the tables
CREATE TABLE Species (
    speciesId INT PRIMARY KEY,
    speciesName VARCHAR(50)
);

CREATE TABLE Animal (
    animalId INT PRIMARY KEY IDENTITY(1,1),
    animalName VARCHAR(50),
    description VARCHAR(255),
    speciesId INT,
    FOREIGN KEY (speciesId) REFERENCES Species(speciesId)
);

CREATE TABLE Users (
    userId INT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(50)
);

-- Insert Species Data
INSERT INTO Species (speciesId, speciesName)
VALUES 
(1, 'Pig'), 
(2, 'Cat'), 
(3, 'Bird'), 
(4, 'Duck'),
(5, 'Bunny');

-- Insert Animal Data
INSERT INTO Animal (animalName, description, speciesId)
VALUES 
('Porky Pig', 'A friendly pig that loves eating.', 1), 
('Sylvester The Cat', 'Always trying to catch and eat Tweety The Bird.', 2), 
('Tweety The Bird', 'Likes to play with Sylvester The Cat.', 3), 
('Duffy Duck', 'Is a black duck.', 4), 
('Bugs Bunny', 'A very happy-looking Bunny known for liking carrots.', 5);

-- Insert User Data
INSERT INTO Users (userId, username, password)
VALUES 
(1, 'Sylvester', 'sylvester@12');