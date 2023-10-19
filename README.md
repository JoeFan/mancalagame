# Mancala Game Service

### Clarification
Because no expectation on Non-Functional requirements, most of the tech choice criteria is easy to set up code
some defects:
* Code is Not Runnable, only draft design and function implementation
* Some api is only for setup code easily. But not proper in acutal project. eg. like create api [MancalaGameController.create](./src/main/java/com/bol/interview/mancala/controller/MancalaGameController.java) 
* Some tech choice only add in pom dependency, not implement in code. 
  * Websocket
* Using object defined in module directly in different layers. For actual usage will define vo, dto, do accordingly.
  


### Tech Involved:

| Used Tech   |            Version             |                                                                       Comments |
|-------------|:------------------------------:|-------------------------------------------------------------------------------:|
| JDK         |          1.8 or later          |                                                                                |
| SpringBoot  |             2.4.5              |                                                    only for set up code easily |
| Mango Cache |  depend on springboot version  | will use In Memory non sql db if in actual project. Embed mongo for easy setup |
| Web socket  |  depend on springboot version  |                Implement in http restful. But will give Tech Selection Detail. |

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

For the Game is real time support, Websocket is faster than HTTP Connection.

### Model
models in package:com.bol.interview.mancala.model


| Model Name   | Attribute            | Type            | Description                                 |
|--------------|:---------------------|:----------------|:--------------------------------------------|
| BoardSegment | segmentId            | String          | unique info to identify segment             |
|              | palyer               | Player          | the payer thisboard Segment belong to       |
|              | house                | Pit             | the big Pit                                 |
|              | pits                 | List<Pit>       | the 6 Pits which board segment has          |
| MancalaGame  | gameId               | String          | unique id to identify the current game      |
|              | activeBoardSegment   | BoardSegment    | the current turn player owned board segment |
|              | inactiveBoardSegment | BoardSegment    | the player not in turn owned board segment  |
| Pit          |                      | pit and big pit | each small pit and house                    |
| Player       |                      | the game player | the game player                             |
| SowResult    | leftStone            | int             | left stone number after sow                 |
|              | currentPitIdx        | int             | last sowed pit index                        |
|              | action               | Sowaction       | the action after segment sow                |

### Comments



