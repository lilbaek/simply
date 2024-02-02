DROP TABLE IF EXISTS Post;

CREATE TABLE Post (
                      id varchar(255) NOT NULL,
                      title varchar(255) NOT NULL,
                      enabled varchar(1) NOT NULL,
                      date date NOT NULL,
                      type varchar(8) NOT NULL,
                      stars NUMERIC(20, 16) NOT NULL,
                      PRIMARY KEY (id)
);

INSERT INTO Post (id, title, enabled, date, type, stars) VALUES ('1', 'Post 1', 'Y', '2024-02-02', 'ART', 8.876432);
INSERT INTO Post (id, title, enabled, date, type, stars) VALUES ('2', 'Post 1', 'Y', '2024-02-02', 'ART', 8.876432);
INSERT INTO Post (id, title, enabled, date, type, stars) VALUES ('3', 'Post 1', 'Y', '2024-02-02', 'ART', 8.876432);
INSERT INTO Post (id, title, enabled, date, type, stars) VALUES ('4', 'Post 1', 'N', '2024-02-02', 'ART', 8.876432);
