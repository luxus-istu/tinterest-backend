CREATE TABLE chats (
                       id SERIAL PRIMARY KEY,
                       type VARCHAR(10) NOT NULL CHECK (type IN ('PRIVATE', 'GROUP')),
                       title VARCHAR(255),
                       created_by INT REFERENCES users(id)
);

CREATE TABLE chat_members (
                              id SERIAL PRIMARY KEY,
                              chat_id INT NOT NULL REFERENCES chats(id),
                              user_id INT NOT NULL REFERENCES users(id),
                              role VARCHAR(10) NOT NULL CHECK (role IN ('MEMBER', 'OWNER'))
);