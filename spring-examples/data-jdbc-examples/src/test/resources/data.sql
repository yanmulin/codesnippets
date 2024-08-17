INSERT INTO minion (name) VALUES ('Bob'), ('Kevin'), ('Stuart');
INSERT INTO toy (name, minion) VALUES ('Teddy Bear', 1);
INSERT INTO person (name) VALUES ('Felonious Gru');
UPDATE minion SET master=1 WHERE name = 'Kevin';

INSERT INTO friendship VALUES (3, 1), (3, 2);