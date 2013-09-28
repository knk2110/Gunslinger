There are two tournaments, one without dumb players and the other with two dumb players.

Dumb player 1: Randomly shoot a player that is not its friend. If such player exists, always shoot. (There is no shoot rate in this dumb player, which is different from the dumb player before)

Dumb player 2: Consistently shoot the first player on its enemy list until that player is dead. When the first player is dead, switch to shoot the second and so on.

gunslinger.sql is the tournament with dumb players. In the tournament, each configuration is run 1000 iterations.

gunslinger1.sql is the tournament without dumb players. Every config is run only 10 iterations, because almost every game is a tie.


1. Import the database
mysql -p -u username database_name < file.sql 

2. Table schema:

CREATE TABLE game (
  id int NOT NULL AUTO_INCREMENT,
  players int NOT NULL,
  enemies int NOT NULL,
  friends int NOT NULL,
  self tinyint NOT NULL,
  dumb1 int) NOT NULL,
  dumb2 int NOT NULL,
  PRIMARY KEY (id)
)

CREATE TABLE result (
  game_id int NOT NULL,
  group_id int NOT NULL,
  score int DEFAULT NULL,
  rank int NOT NULL,
  KEY game_id (game_id,group_id),
  FOREIGN KEY (game_id) REFERENCES game (id)
) 

self: if players are play against himself. if self is 1, all players in that game are from the same team.
dumb1, dumb2 is number of that dumb players in the game


3. Parameters
See config.txt for parameters without dumb players.
See config2.txt for parameters with dumb players.

