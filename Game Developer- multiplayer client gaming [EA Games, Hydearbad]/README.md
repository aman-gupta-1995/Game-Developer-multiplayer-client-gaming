# Shoddy Sense

Shoddy Sense is a cross between _Battleship_ and _Guess Who?_ and is similar
to the esoteric board game [Sixth Sense][].

Shoddy Sense was created by [Cathy J. Fitzpatrick][cathyjf] in March 2007. It is
a client-server multiplayer game.

## Game description

Shoddy Sense supports games containing arbitrarily many players (minimum two
players). At the start of the game, each player selects a 10x10 monochrome
image from among a collection of images presented to all players. Each player
keeps her or his selection secret.

Gameplay proceeds from one player to the next, one at a time, looping back
around to the first player afterward. Each turn, a player can take one of
the following two actions:

1. A player can "attack" a square on the 10x10 board. This reveals whether each
   other player's image is black or white at that square.

2. A player can guess what image another player has. If the guess is correct,
   the player whose image was correctly guessed is removed from the game. If
   the guess is incorrect, the player who made the guess is removed from the
   game. On a given turn, a player can opt to guess one or more other players'
   images (though of course, if one of them is wrong, the player will not be 
   able to make any further guesses). If a player guesses wrongly, the
   specific image that was wrongly guessed is not disclosed to the other
   players (i.e. the other players are only told that the guess was wrong, not
   specifically what the guess was).
 
The goal is to be the last player remaining, by correctly guessing other
players' images, or by being the beneficiary of incorrect guesses by other
players.

The main game window looks like this:

<a href="https://raw.github.com/cathyjf/ShoddySense/master/raw/screenshots/main-game-window.png">
<img alt="Shoddy Sense - main game window"
src="https://raw.github.com/cathyjf/ShoddySense/master/raw/screenshots/main-game-window-preview.png" />
</a>

This screenshot is from the perspective of the player "Cathy". The orange
highlight in the Opponent's grid shows that Cathy is hovering over that square
and would attack it if she clicked.

The array of images to the right of the Opponent's grid is private to Cathy; it
is not shown to the Opponent. This grid has no direct effect on the gameplay;
the program allows you to toggle whether an image in the grid is crossed off
by clicking on it. This allows you to keep track of which images it would be
impossible for the Opponent to have, based on what you know so far.

The Xs in Cathy's space at the bottom are places where the Opponent has
attacked on his previous two turns. The black square revealed on the Opponent's
grid is the place where Cathy attacked on her previous turn, and this revealed
a black square. If Cathy had attacked a blank square, the grid would show an X
on the space she attacked.

Clicking the "Guess" button under the Opponent's name would allow Cathy to
guess at what image the Opponent has. If this guess is correct, she will win
the game, but if it is wrong, she would instantly lose.

## How to run

### Dependencies

+ You will need a JDK to compile the program, such as [OpenJDK][].
+ [Apache Ant][] is used to build the program.

There are NetBeans project files in the repository. However, NetBeans is _not_
required to build or use the program.

### Running the server

The server is contained in the `ShoddySenseServer` directory of the repository.
From the directory where you cloned the repository, try

```bash
# Change into the server directory.
cd ShoddySenseServer
# Build the program.
ant jar
# Run the server.
java -jar dist/ShoddySenseServer.jar  PORT  IMAGE_ARCHIVE
```

The server parameters are as follows:

+ `PORT` specifies the TCP port on which to run the sever.
+ `IMAGE_ARCHIVE` specifies the publicly-accessible URI of a `jar` archive containing a
   collection of 10x10 images to use for the game. Black pixels in the image
   are treated as black; any other colour is treated as white.

The `PORT` and `IMAGE_ARCHIVE` parameters are both optional. If omitted,
the server will run on port 9944. The default image collection is the
[raw/ShoddySenseImages.jar](https://raw.github.com/cathyjf/ShoddySense/master/raw/ShoddySenseImages.jar) archive from the repository, which contains the
images from the `images` directory of the repository.

### Running the client

The client is contained in the `ShoddySense` directory of the repository.
From the directory where you cloned the repository, try

```bash
# Change into the client directory.
cd ShoddySense
# Build the program.
ant jar
# Run the client.
java -jar dist/ShoddySense.jar
```

The client does not accept any command line parameters.

## Licence

This program is licensed under the [GNU Affero General Public License][agpl3],
version 3 or later.

## Credits

+ [Cathy J. Fitzpatrick][cathyjf] (cathyjf) created this program.
+ Peter Fenner (Asimir) created the collection of 10x10 images (contained in
  the `images` directory of the repository).

[Sixth Sense]: http://boardgamegeek.com/boardgame/6786/sixth-sense
[OpenJDK]: http://openjdk.java.net
[Apache Ant]: https://ant.apache.org/
[agpl3]: http://www.fsf.org/licensing/licenses/agpl-3.0.html
[cathyjf]: https://cathyjf.com

