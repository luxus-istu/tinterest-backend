INSERT INTO interests (name) VALUES
    ('Board games'),
    ('Design'),
    ('Football'),
    ('Java'),
    ('Movies'),
    ('Music'),
    ('Photography'),
    ('Product management'),
    ('Running'),
    ('Travel')
ON CONFLICT DO NOTHING;
