# Mancala Game Service

### How to startup service
* make sure you have mongo in local or use docker to setup mongo.
* run: MancalaGameApplication
* website in local: [https://localhost:8443/mancalagame](https://localhost:8443/mancalagame)

### How to play:
* Input your username, click "Login"
* waiting for other join in. you can open another window and input a different username login.
* then click start the game will be started.
* the red color is your pits and big pits. click on the pit which you want to sow.

### Clarification
Because no expectation on Non-Functional requirements, most of the tech choice criteria is easy to set up code
some defects:
* mixed backed and frontend code in same service.


### Tech Involved:

| Used Tech   |            Version             |                                                                       Comments |
|-------------|:------------------------------:|-------------------------------------------------------------------------------:|
| JDK         |          1.8 or later          |                                                                                |
| SpringBoot  |             2.4.5              |                                                    only for set up code easily |
| Mango Cache |  depend on springboot version  | will use In Memory non sql db if in actual project. Embed mongo for easy setup |
| Websocket   |  depend on springboot version  |                Implement in http restful. But will give Tech Selection Detail. |

### TechSelection
Why use WebSocket instead of Http1.X for MancalaGameController

| Compare Aspect                                | Websocket |              Http1.x | our requirement |
|-----------------------------------------------|:---------:|---------------------:|----------------:|
| Is bidirectional                              |    Yes    |                   No |             Yes |
| web application Support                       |    Yes    |                  Yes |             Yes |
| continuously received by the server           |    Yes    | No.Not automatically |             Yes |
| Fetch Old Data                                |    No     |                  Yes |              No |
| Frequently Update                             |    No     |                  Yes |              No |
| multiple client communication in same channel |    Yes    |                   No |             Yes |

Defect of websocket: not supported by some web browser.

For the Game is real time support, Websocket is faster than HTTP Connection.

### Model
models in package:com.bol.interview.mancala.model

| Model Name        | Attribute            | Type            | Description                                 |
|-------------------|:---------------------|:----------------|:--------------------------------------------|
| BoardSegment      | segmentId            | String          | unique info to identify segment             |
|                   | palyer               | String          | the payer this board Segment belong to      |
|                   | house                | Pit             | the big Pit                                 |
|                   | pits                 | List<Pit>       | the 6 Pits which board segment has          |
| MancalaGame       | gameId               | String          | unique id to identify the current game      |
|                   | activeBoardSegment   | BoardSegment    | the current turn player owned board segment |
|                   | inactiveBoardSegment | BoardSegment    | the player not in turn owned board segment  |
| Pit               |                      | pit and big pit | each small pit and house                    |
| SegmentSowResult  | leftStone            | int             | left stone number after sow                 |
|                   | sowRequestPlayer     | String          | who is sowing                               |
|                   | lastSowPitIndex      | int             | last sowed pit index                        |
|                   | lastPitOwner         | String          | the last sowed Pit owner player name        |


### Game Rules
Game Rules in package:com.bol.interview.mancala.rule
* AnotherRoundRule:  the active player will have another round sowing when last stone sowing her own house
* MoveBothPitsStone2ActivePlayerHouseRule: move both pits stones to active player house when the last stone sowing in active player empty pit.
* SwitchTurnRule: if the first two rules not hit. with turn.
* GameOverRule: after sow, check if the game is over. if one player pits all empty. then move the pits stones to player owned house. and show game over.
 

