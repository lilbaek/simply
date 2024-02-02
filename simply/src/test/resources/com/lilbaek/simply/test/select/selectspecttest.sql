DROP TABLE IF EXISTS Post;

CREATE TABLE Post (
                      id varchar(255) NOT NULL,
                      title varchar(255) NOT NULL,
                      slug varchar(255) NOT NULL,
                      enabled varchar(1) NOT NULL,
                      date date NOT NULL,
                      time_to_read int NOT NULL,
                      tags varchar(255),
                      type varchar(8) NOT NULL,
                      stars NUMERIC(20, 16) NOT NULL,
                      PRIMARY KEY (id)
);

INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('1', 'Post 1', 'Hello post 1', 'Y', '2024-02-02', 1, 'java, spring', 'ART', 8.876432);
INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('2', 'Post 2', 'Hello post 2', 'Y', '2024-02-02', 1, 'java, spring', 'OTH', 48.876432);
INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('3', 'Post 3', 'Hello post 3', 'Y', '2024-02-02', 1, 'java, spring', 'JOU', 38.876432);
INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('4', 'Post 4', 'Hello post 4', 'Y', '2024-02-02', 1, 'java, spring', 'ART', 28.876432);
INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('5', 'Post 5', 'Hello post 5', 'Y', '2024-02-02', 1, 'java, spring', 'OTH', 18.876432);
INSERT INTO Post (id, title, slug, enabled, date, time_to_read, tags, type, stars) VALUES ('6', 'Post 6', 'Hello post 6', 'N', '2024-02-02', 89, 'java, spring', 'ART', 118.876432);
