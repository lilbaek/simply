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

