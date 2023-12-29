# Elevator challenge

## Installing the environment:
* Install JDK 17 (Define JAVA_HOME + Add bin folder to the path)
* Install Maven 3.6.3+ (Add bin folder to the path)

## How to use it
        
Define requests to be processed with the following format:
"{ElevatorType};{currentFloor};{desiredFloor};{Direction};{Location};{keycode};"

***Note***: For requests from INSIDE elevator, currentFloor is not needed, therefore, set to 0;
Example of requests:

      String[] requests = new String[]{
        "PUBLIC;0;50;UP;INSIDE;50;12345",
        "PUBLIC;0;5;UP;INSIDE;5;12345",
        "PUBLIC;0;3;UP;INSIDE;3;12345",
        "PUBLIC;3;4;UP;OUTSIDE;350;12345",
        "PUBLIC;0;2;UP;INSIDE;25;12345",
        "PUBLIC;0;35;UP;INSIDE;120;12345",
        "PUBLIC;0;1;DOWN;INSIDE;100;12345",
        "PUBLIC;0;2;DOWN;INSIDE;200;12345",
        "PUBLIC;4;-1;DOWN;OUTSIDE;400;12345",
        "FREIGHT;4;32;UP;OUTSIDE;400;12345",
        "PUBLIC;2;50;DOWN;INSIDE;400;12345",
      };

These requests simulate the following situation:
1. Users inside elevator pressed buttons to go floor 3,5 and 50.
2. Then users from floor 3 want to go floor 4 so they called the elevator to go up.
3. Then other people inside elevator pressed buttons to stop in floors 2 and 35
4. Then people from inside elevator want to go down so they pressed buttons to go floor 1 and 2.
5. Another user waiting in floor 4 (Outside elevator) wants to go down to floor -1

6. And, independently, elevator FREIGHT goes to floor 32 picking up users at 4 first.

### Expected results: 
**Public Elevator** should do the following movements:
+ Start direction to UP
+ Stopped at floor 2
+ Stopped at floor 3 (And pick up people here)
+ Stopped at floor 4
+ Stopped at floor 5
+ Stopped at floor 35
+ Stopped at floor 50
+ Change direction to DOWN
+ Stopped at floor 4
+ Stopped at floor 2
+ Stopped at floor -1

**Freight Elevator** should do the following movements:
+ Stopped at floor 4
+ Stopped at floor 32

Assumptions:
- The elevator first will handle all UP requests before changing the direction do DOWN (Unless there are no up requests so it starts going down first)
- Elevator will stop in ascending order floors and it will pick users up during the movement only if those users from outside generate request to go in the same direction. If not, they will be picked up when the elevator changes the direction
- If the elevator is stopped and it has requests from going up and down, it will prioritize UP first.


Run the following command to start processing the requests:
```mvn spring-boot:run "-Dspring-boot.run.arguments=PUBLIC;0;5;UP;INSIDE;50;12345 PUBLIC;0;12;UP;INSIDE;17;12345 FREIGHT;0;5;UP;INSIDE;500"```
Eg with weight limit reached: (Request to floor 8 is ignored after a while)
```mvn spring-boot:run "-Dspring-boot.run.arguments=PUBLIC;0;5;UP;INSIDE;50;12345 PUBLIC;0;8;UP;INSIDE;995;12345 PUBLIC;0;12;UP;INSIDE;17;12345"```
Eg with invalid keycard when going to a restricted floor (Request to floor 50 is ignored after a while)
```mvn spring-boot:run "-Dspring-boot.run.arguments=PUBLIC;0;5;UP;INSIDE;50;12345 PUBLIC;0;50;UP;INSIDE;220;999 PUBLIC;0;12;UP;INSIDE;17;12345"```

mvn spring-boot:run "-Dspring-boot.run.arguments=PUBLIC;0;5;UP;INSIDE;50;12345 PUBLIC;0;12;UP;INSIDE;17;12345 FREIGHT;0;5;UP;INSIDE;500"
At any moment you could check the status of each elevator using endpoint:
```{url}/api/v1/elevators/public``` or ```{url}/api/v1/elevators/freight```
eg: http://localhost:8080/api/v1/elevators/freight

## Handling key access and weight limit situations
When weight limit is reached or keycard is invalid, the elevator keeps in a stuck situation. In order to simulate a real world scenario I decided to handle those situations adding a timer and simulating the users getting out from the elevator.
Situation: A new user who was waiting outside the elevator enters. The weight limit is reached and the elevator is stuck and cannot proceed. After some seconds, user gets out from the elevator and it continues processing the requests.
