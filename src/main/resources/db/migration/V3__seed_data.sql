-- INSERT USERS (5)
INSERT INTO users (username, email, password, description, profile_pic_url, game_points, created_at, updated_at)
VALUES
('traveler1', 'traveler1@example.com', 'hashed_password1', 'Love exploring new places!', 'https://plus.unsplash.com/premium_photo-1665203415837-a3b389a6b33e?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8dHJhdmVsbGVyfGVufDB8fDB8fHww', 100, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('wanderer2', 'wanderer2@example.com', 'hashed_password2', 'Adventure seeker.', 'https://images.unsplash.com/photo-1682687219356-e820ca126c92?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8dHJhdmVsbGVyfGVufDB8fDB8fHww', 150, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('explorer3', 'explorer3@example.com', 'hashed_password3', 'Nature enthusiast.', 'https://images.unsplash.com/photo-1520466809213-7b9a56adcd45?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8dHJhdmVsbGVyfGVufDB8fDB8fHww', 200, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('nomad4', 'nomad4@example.com', 'hashed_password4', 'Always on the move.', 'https://plus.unsplash.com/premium_photo-1665203418163-52835a228723?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NXx8dHJhdmVsbGVyfGVufDB8fDB8fHww', 80, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('globetrotter5', 'globetrotter5@example.com', 'hashed_password5', 'Chasing sunsets.', 'https://images.unsplash.com/photo-1557652696-0fd8a35b0d62?w=600&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8dHJhdmVsbGVyfGVufDB8fDB8fHww', 120, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '5 days');

-- INSERT DESTINATIONS (20)
INSERT INTO destinations (name, description, image_url, created_at, updated_at)
VALUES
('Paris', 'City of love and lights.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/paris.png', CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('Tokyo', 'Vibrant metropolis with tradition.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/tokyo.jpg', CURRENT_TIMESTAMP - INTERVAL '59 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('New York', 'The city that never sleeps.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/nyc.jpg', CURRENT_TIMESTAMP - INTERVAL '58 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('Sydney', 'Iconic harbor and beaches.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/sydney.jpg', CURRENT_TIMESTAMP - INTERVAL '57 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('Rome', 'Eternal city of history.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rome.jpeg', CURRENT_TIMESTAMP - INTERVAL '56 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('Cape Town', 'Stunning coastal beauty.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/capeTown.jpeg', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP - INTERVAL '6 days'),
('Rio de Janeiro', 'Carnival and beaches.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rio+de+janeiro.jpeg', CURRENT_TIMESTAMP - INTERVAL '54 days', CURRENT_TIMESTAMP - INTERVAL '7 days'),
('Bangkok', 'Temples and street food.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/bangkok.jpeg', CURRENT_TIMESTAMP - INTERVAL '53 days', CURRENT_TIMESTAMP - INTERVAL '8 days'),
('London', 'Historic and modern charm.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/london.jpeg', CURRENT_TIMESTAMP - INTERVAL '52 days', CURRENT_TIMESTAMP - INTERVAL '9 days'),
('Amsterdam', 'Canals and culture.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/amsterdam.jpeg', CURRENT_TIMESTAMP - INTERVAL '51 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
('Dubai', 'Luxury and skyscrapers.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/dubai.jpeg', CURRENT_TIMESTAMP - INTERVAL '50 days', CURRENT_TIMESTAMP - INTERVAL '11 days'),
('Kyoto', 'Traditional Japanese beauty.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/kyoto.jpeg', CURRENT_TIMESTAMP - INTERVAL '49 days', CURRENT_TIMESTAMP - INTERVAL '12 days'),
('Barcelona', 'Art and architecture.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/barcelona.jpeg', CURRENT_TIMESTAMP - INTERVAL '48 days', CURRENT_TIMESTAMP - INTERVAL '13 days'),
('Istanbul', 'Bridge between continents.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/instanbul.jpeg', CURRENT_TIMESTAMP - INTERVAL '47 days', CURRENT_TIMESTAMP - INTERVAL '14 days'),
('Hawaii', 'Tropical paradise.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/hawaii.jpg', CURRENT_TIMESTAMP - INTERVAL '46 days', CURRENT_TIMESTAMP - INTERVAL '15 days'),
('Prague', 'Fairytale city.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/prague.jpeg', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP - INTERVAL '16 days'),
('Santorini', 'Whitewashed island views.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/santorini.jpeg', CURRENT_TIMESTAMP - INTERVAL '44 days', CURRENT_TIMESTAMP - INTERVAL '17 days'),
('Seoul', 'Dynamic K-pop culture.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/seoul.jpeg', CURRENT_TIMESTAMP - INTERVAL '43 days', CURRENT_TIMESTAMP - INTERVAL '18 days'),
('Vancouver', 'Nature and urban blend.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/vancouver.jpeg', CURRENT_TIMESTAMP - INTERVAL '42 days', CURRENT_TIMESTAMP - INTERVAL '19 days'),
('Marrakech', 'Vibrant markets and riads.', 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/marrakesh.jpeg', CURRENT_TIMESTAMP - INTERVAL '41 days', CURRENT_TIMESTAMP - INTERVAL '20 days');

-- Insert 50 Posts into posts table (without image_urls)
INSERT INTO posts (title, content, post_type, created_at, updated_at, likes_count, comments_count, creator_id, destination_id)
VALUES
('Eiffel Tower at Night', 'Stunning views!', 'post', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 2, 1, 1, 1),
('Sushi in Tokyo', 'Best sushi ever.', 'experience', CURRENT_TIMESTAMP - INTERVAL '29 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 3, 0, 1, 2),
('NYC Subway Tips?', 'How to navigate?', 'questions', CURRENT_TIMESTAMP - INTERVAL '28 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 1, 2, 1, 3),
('Sydney Beaches Guide', 'Top 5 beaches.', 'tips', CURRENT_TIMESTAMP - INTERVAL '27 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 2, 0, 1, 4),
('Colosseum History', 'Amazing ruins.', 'post', CURRENT_TIMESTAMP - INTERVAL '26 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 3, 1, 1, 5),
('Table Mountain Hike', 'Breathtaking!', 'experience', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 2, 0, 1, 6),
('Carnival in Rio', 'So colorful!', 'post', CURRENT_TIMESTAMP - INTERVAL '24 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 1, 1, 1, 7),
('Bangkok Temples', 'Must-visit spots.', 'tips', CURRENT_TIMESTAMP - INTERVAL '23 days', CURRENT_TIMESTAMP - INTERVAL '8 days', 2, 0, 1, 8),
('London Eye View', 'Worth it?', 'questions', CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP - INTERVAL '9 days', 3, 2, 1, 9),
('Amsterdam Canals', 'Peaceful ride.', 'experience', CURRENT_TIMESTAMP - INTERVAL '21 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 2, 0, 1, 10),
('Paris Cafes', 'Charming spots.', 'post', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '11 days', 1, 1, 2, 1),
('Tokyo Nightlife', 'Shibuya vibes.', 'experience', CURRENT_TIMESTAMP - INTERVAL '19 days', CURRENT_TIMESTAMP - INTERVAL '12 days', 3, 0, 2, 2),
('NYC Food Scene', 'Best pizza?', 'questions', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '13 days', 2, 2, 2, 3),
('Sydney Opera House', 'Iconic visit.', 'post', CURRENT_TIMESTAMP - INTERVAL '17 days', CURRENT_TIMESTAMP - INTERVAL '14 days', 1, 0, 2, 4),
('Roman Forum', 'History nerds!', 'tips', CURRENT_TIMESTAMP - INTERVAL '16 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 2, 1, 2, 5),
('Cape Town Sunset', 'Magical moment.', 'experience', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '16 days', 3, 0, 2, 6),
('Rio Beaches', 'Copacabana fun.', 'post', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '17 days', 2, 1, 2, 7),
('Bangkok Markets', 'Night market tips.', 'tips', CURRENT_TIMESTAMP - INTERVAL '13 days', CURRENT_TIMESTAMP - INTERVAL '18 days', 1, 0, 2, 8),
('London Museums', 'Free entry?', 'questions', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '19 days', 2, 2, 2, 9),
('Amsterdam Bikes', 'Cycling tips.', 'tips', CURRENT_TIMESTAMP - INTERVAL '11 days', CURRENT_TIMESTAMP - INTERVAL '20 days', 3, 0, 2, 10),
('Dubai Desert Safari', 'Thrilling ride!', 'experience', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 2, 1, 3, 11),
('Kyoto Temples', 'Serenity found.', 'post', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 1, 0, 3, 12),
('Barcelona Gaudi', 'Sagrada Familia.', 'post', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 3, 1, 3, 13),
('Istanbul Bazaars', 'Shopping tips.', 'tips', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 2, 0, 3, 14),
('Hawaii Surfing', 'Best beaches?', 'questions', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 1, 2, 3, 15),
('Prague Castle', 'Stunning views.', 'post', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 2, 0, 3, 16),
('Santorini Sunset', 'Oia magic.', 'experience', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 3, 1, 3, 17),
('Seoul Street Food', 'Tteokbokki love.', 'post', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '8 days', 2, 0, 3, 18),
('Vancouver Hiking', 'Top trails?', 'questions', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '9 days', 1, 2, 3, 19),
('Marrakech Riads', 'Where to stay?', 'questions', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '10 days', 2, 0, 3, 20),
('Paris Louvre', 'Must-see art.', 'tips', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '11 days', 3, 1, 4, 1),
('Tokyo Onsen', 'Relaxing soak.', 'experience', CURRENT_TIMESTAMP - INTERVAL '29 days', CURRENT_TIMESTAMP - INTERVAL '12 days', 2, 0, 4, 2),
('NYC Skyline', 'Best views?', 'questions', CURRENT_TIMESTAMP - INTERVAL '28 days', CURRENT_TIMESTAMP - INTERVAL '13 days', 1, 2, 4, 3),
('Sydney Harbour', 'Cruise tips.', 'tips', CURRENT_TIMESTAMP - INTERVAL '27 days', CURRENT_TIMESTAMP - INTERVAL '14 days', 2, 0, 4, 4),
('Rome Pasta', 'Carbonara heaven.', 'post', CURRENT_TIMESTAMP - INTERVAL '26 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 3, 1, 4, 5),
('Cape Town Wineries', 'Wine tasting.', 'experience', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '16 days', 2, 0, 4, 6),
('Rio Samba', 'Dance the night!', 'post', CURRENT_TIMESTAMP - INTERVAL '24 days', CURRENT_TIMESTAMP - INTERVAL '17 days', 1, 1, 4, 7),
('Bangkok Tuk-Tuk', 'Fun ride!', 'experience', CURRENT_TIMESTAMP - INTERVAL '23 days', CURRENT_TIMESTAMP - INTERVAL '18 days', 2, 0, 4, 8),
('London Pubs', 'Best ales?', 'questions', CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP - INTERVAL '19 days', 3, 2, 4, 9),
('Amsterdam Art', 'Van Gogh museum.', 'post', CURRENT_TIMESTAMP - INTERVAL '21 days', CURRENT_TIMESTAMP - INTERVAL '20 days', 2, 0, 4, 10),
('Dubai Mall', 'Shopping spree.', 'post', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 1, 1, 5, 11),
('Kyoto Gardens', 'Zen moments.', 'experience', CURRENT_TIMESTAMP - INTERVAL '19 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 2, 0, 5, 12),
('Barcelona Beach', 'Sun and sangria.', 'post', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 3, 1, 5, 13),
('Istanbul Mosques', 'Hagia Sophia.', 'post', CURRENT_TIMESTAMP - INTERVAL '17 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 2, 0, 5, 14),
('Hawaii Volcanoes', 'Lava trails.', 'experience', CURRENT_TIMESTAMP - INTERVAL '16 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 1, 0, 5, 15),
('Prague Bridges', 'Is Charles Bridge fun?', 'questions', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 2, 1, 5, 16),
('Santorini Views', 'Blue domes.', 'post', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 3, 0, 5, 17),
('Seoul K-Drama', 'Filming spots.', 'tips', CURRENT_TIMESTAMP - INTERVAL '13 days', CURRENT_TIMESTAMP - INTERVAL '8 days', 2, 0, 5, 18),
('Vancouver Parks', 'Stanley Park.', 'post', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '9 days', 1, 1, 5, 19),
('Marrakech Souks', 'Bargain tips.', 'tips', CURRENT_TIMESTAMP - INTERVAL '11 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 2, 0, 5, 20);

-- Insert image URLs into post_images table
INSERT INTO post_images (post_id, image_urls)
VALUES
(1, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/night+eifel+tower.jpeg'),
(2, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/sushi+in+tokyo.jpeg'),
(4, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/sydney+beaches.jpeg'),
(5, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/colloseum.jpeg'),
(6, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/mountain+hike.jpeg'),
(7, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/carnival+in+rio.jpeg'),
(8, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/bangkok+temple.jpeg'),
(9, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/london+eye+view.jpeg'),
(10, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/amsterdam+canal.jpeg'),
(11, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/paris+cafe.jpeg'),
(12, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/tokyo+night+life.jpeg'),
(14, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/sydney+opera+house.jpeg'),
(15, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/roman+forum.jpeg'),
(16, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/cape+town+sunset.jpeg'),
(17, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rio+beaches+1.jpeg'),
(17, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rio+beaches+2.jpeg'),
(18, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/bangkok+market+1.jpeg'),
(18, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/bangkok+market+2.jpeg'),
(20, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/amsterdam+bikes.jpeg'),
(21, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/dubai+desert+safari.jpeg'),
(22, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/kyoto+temples+1.jpeg'),
(22, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/kyoto+temples+2.jpeg'),
(23, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/barcelona+gaudi.jpeg'),
(25, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/hawaii+surfing+1.jpeg'),
(25, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/hawaii+surfing+2.jpeg'),
(26, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/prague+castl.jpeg'),
(27, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/santorini+sunsets.jpeg'),
(28, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/seoul+street+food.jpeg'),
(30, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/marrakechriad.jpg'),
(31, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/paris+louvre.jpeg'),
(32, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/tokyo+onsen.jpeg'),
(33, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/nyc+skyline.jpeg'),
(34, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/sydney+harbour.jpeg'),
(35, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rome+pasta.jpeg'),
(36, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/cape+town+wineries.jpeg'),
(36, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/cape+town+wineries+2.jpeg'),
(37, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/rio+samba.jpeg'),
(38, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/bangkok+tuk+tuk.jpeg'),
(39, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/london+pubs.jpeg'),
(40, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/amsterdam+art.jpeg'),
(41, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/dubai+mall.jpeg'),
(42, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/kyoto+gardens.jpeg'),
(43, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/barcelona+beach.jpeg'),
(44, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/istanbul+mosque.jpeg'),
(45, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/hawaii+volcano.jpeg'),
(47, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/santorini+view.jpeg'),
(48, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/seoul+k+drama.jpeg'),
(49, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/vancouver+park.jpeg'),
(50, 'https://wanderverse-cloud-bucket.s3.ap-southeast-1.amazonaws.com/marakesh+riads.jpeg');

-- Insert 20 Comments (10 top-level, 10 replies, across 10 posts)
INSERT INTO comments (content, post_id, user_id, parent_comment_id, created_at)
VALUES
('Wow, amazing view!', 1, 2, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day'), -- Post 1, top-level
('Totally agree!', 1, 3, 1, CURRENT_TIMESTAMP - INTERVAL '12 hours'), -- Reply to comment 1
('Best sushi spot?', 2, 4, NULL, CURRENT_TIMESTAMP - INTERVAL '2 days'), -- Post 2, top-level
('Try Sushi Zanmai!', 2, 1, 3, CURRENT_TIMESTAMP - INTERVAL '1 day'), -- Reply to comment 3
('Use the MetroCard.', 3, 5, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days'), -- Post 3, top-level
('Avoid rush hours!', 3, 2, 5, CURRENT_TIMESTAMP - INTERVAL '2 days'), -- Reply to comment 5
('Loved the Colosseum!', 5, 3, NULL, CURRENT_TIMESTAMP - INTERVAL '4 days'), -- Post 5, top-level
('Check the audio guide.', 5, 4, 7, CURRENT_TIMESTAMP - INTERVAL '3 days'), -- Reply to comment 7
('Carnival was wild!', 7, 1, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days'), -- Post 7, top-level
('Which samba school?', 7, 5, 9, CURRENT_TIMESTAMP - INTERVAL '4 days'), -- Reply to comment 9
('Great cafes!', 11, 4, NULL, CURRENT_TIMESTAMP - INTERVAL '6 days'), -- Post 11, top-level
('Try Cafe de Flore.', 11, 2, 11, CURRENT_TIMESTAMP - INTERVAL '5 days'), -- Reply to comment 11
('Free museums?', 19, 3, NULL, CURRENT_TIMESTAMP - INTERVAL '7 days'), -- Post 19, top-level
('British Museum is free!', 19, 1, 13, CURRENT_TIMESTAMP - INTERVAL '6 days'), -- Reply to comment 13
('Sagrada was stunning!', 23, 5, NULL, CURRENT_TIMESTAMP - INTERVAL '8 days'), -- Post 23, top-level
('Book tickets early!', 23, 4, 15, CURRENT_TIMESTAMP - INTERVAL '7 days'), -- Reply to comment 15
('Oia is magical!', 27, 2, NULL, CURRENT_TIMESTAMP - INTERVAL '9 days'), -- Post 27, top-level
('Best sunset spot?', 27, 3, 17, CURRENT_TIMESTAMP - INTERVAL '8 days'), -- Reply to comment 17
('Louvre tips?', 31, 1, NULL, CURRENT_TIMESTAMP - INTERVAL '10 days'), -- Post 31, top-level
('Get the audio tour!', 31, 5, 19, CURRENT_TIMESTAMP - INTERVAL '9 days'); -- Reply to comment 19

-- Insert 100 Likes (distributed across 50 posts, 5 users)
INSERT INTO likes (post_id, user_id, created_at)
VALUES
(1, 2, CURRENT_TIMESTAMP - INTERVAL '1 day'), (1, 3, CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(2, 4, CURRENT_TIMESTAMP - INTERVAL '2 days'), (2, 1, CURRENT_TIMESTAMP - INTERVAL '1 day'), (2, 5, CURRENT_TIMESTAMP - INTERVAL '18 hours'),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(4, 3, CURRENT_TIMESTAMP - INTERVAL '4 days'), (4, 4, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(5, 1, CURRENT_TIMESTAMP - INTERVAL '5 days'), (5, 5, CURRENT_TIMESTAMP - INTERVAL '4 days'), (5, 2, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(6, 3, CURRENT_TIMESTAMP - INTERVAL '6 days'), (6, 4, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(7, 5, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(8, 1, CURRENT_TIMESTAMP - INTERVAL '8 days'), (8, 2, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(9, 3, CURRENT_TIMESTAMP - INTERVAL '9 days'), (9, 4, CURRENT_TIMESTAMP - INTERVAL '8 days'), (9, 5, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(10, 2, CURRENT_TIMESTAMP - INTERVAL '10 days'), (10, 1, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(11, 4, CURRENT_TIMESTAMP - INTERVAL '11 days'),
(12, 5, CURRENT_TIMESTAMP - INTERVAL '12 days'), (12, 3, CURRENT_TIMESTAMP - INTERVAL '11 days'), (12, 2, CURRENT_TIMESTAMP - INTERVAL '10 days'),
(13, 1, CURRENT_TIMESTAMP - INTERVAL '13 days'), (13, 4, CURRENT_TIMESTAMP - INTERVAL '12 days'),
(14, 2, CURRENT_TIMESTAMP - INTERVAL '14 days'),
(15, 3, CURRENT_TIMESTAMP - INTERVAL '15 days'), (15, 5, CURRENT_TIMESTAMP - INTERVAL '14 days'),
(16, 4, CURRENT_TIMESTAMP - INTERVAL '16 days'), (16, 1, CURRENT_TIMESTAMP - INTERVAL '15 days'), (16, 2, CURRENT_TIMESTAMP - INTERVAL '14 days'),
(17, 5, CURRENT_TIMESTAMP - INTERVAL '17 days'), (17, 3, CURRENT_TIMESTAMP - INTERVAL '16 days'),
(18, 2, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(19, 1, CURRENT_TIMESTAMP - INTERVAL '19 days'), (19, 4, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(20, 5, CURRENT_TIMESTAMP - INTERVAL '20 days'), (20, 3, CURRENT_TIMESTAMP - INTERVAL '19 days'), (20, 2, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(21, 1, CURRENT_TIMESTAMP - INTERVAL '1 day'), (21, 4, CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(22, 2, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(23, 3, CURRENT_TIMESTAMP - INTERVAL '3 days'), (23, 5, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(24, 4, CURRENT_TIMESTAMP - INTERVAL '4 days'), (24, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(25, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(26, 3, CURRENT_TIMESTAMP - INTERVAL '6 days'), (26, 5, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(27, 4, CURRENT_TIMESTAMP - INTERVAL '7 days'), (27, 1, CURRENT_TIMESTAMP - INTERVAL '6 days'), (27, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(28, 3, CURRENT_TIMESTAMP - INTERVAL '8 days'), (28, 5, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(29, 4, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(30, 1, CURRENT_TIMESTAMP - INTERVAL '10 days'), (30, 2, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(31, 3, CURRENT_TIMESTAMP - INTERVAL '11 days'), (31, 5, CURRENT_TIMESTAMP - INTERVAL '10 days'), (31, 4, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(32, 2, CURRENT_TIMESTAMP - INTERVAL '12 days'), (32, 1, CURRENT_TIMESTAMP - INTERVAL '11 days'),
(33, 3, CURRENT_TIMESTAMP - INTERVAL '13 days'),
(34, 4, CURRENT_TIMESTAMP - INTERVAL '14 days'), (34, 5, CURRENT_TIMESTAMP - INTERVAL '13 days'),
(35, 1, CURRENT_TIMESTAMP - INTERVAL '15 days'), (35, 2, CURRENT_TIMESTAMP - INTERVAL '14 days'), (35, 3, CURRENT_TIMESTAMP - INTERVAL '13 days'),
(36, 4, CURRENT_TIMESTAMP - INTERVAL '16 days'), (36, 5, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(37, 1, CURRENT_TIMESTAMP - INTERVAL '17 days'),
(38, 2, CURRENT_TIMESTAMP - INTERVAL '18 days'), (38, 3, CURRENT_TIMESTAMP - INTERVAL '17 days'),
(39, 4, CURRENT_TIMESTAMP - INTERVAL '19 days'), (39, 5, CURRENT_TIMESTAMP - INTERVAL '18 days'), (39, 1, CURRENT_TIMESTAMP - INTERVAL '17 days'),
(40, 2, CURRENT_TIMESTAMP - INTERVAL '20 days'), (40, 3, CURRENT_TIMESTAMP - INTERVAL '19 days'),
(41, 4, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(42, 5, CURRENT_TIMESTAMP - INTERVAL '2 days'), (42, 1, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(43, 2, CURRENT_TIMESTAMP - INTERVAL '3 days'), (43, 3, CURRENT_TIMESTAMP - INTERVAL '2 days'), (43, 4, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(44, 5, CURRENT_TIMESTAMP - INTERVAL '4 days'), (44, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(45, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(46, 3, CURRENT_TIMESTAMP - INTERVAL '6 days'), (46, 4, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(47, 5, CURRENT_TIMESTAMP - INTERVAL '7 days'), (47, 1, CURRENT_TIMESTAMP - INTERVAL '6 days'), (47, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(48, 3, CURRENT_TIMESTAMP - INTERVAL '8 days'), (48, 4, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(49, 5, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(50, 1, CURRENT_TIMESTAMP - INTERVAL '10 days'), (50, 2, CURRENT_TIMESTAMP - INTERVAL '9 days');