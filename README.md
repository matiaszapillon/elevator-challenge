

Example of requests:

      String[] requests = new String[]{
        "0;50;UP;INSIDE;50;12345",
        "0;5;UP;INSIDE;5;12345",
        "0;3;UP;INSIDE;3;12345",
        "3;4;UP;OUTSIDE;350;12345",
        "0;2;UP;INSIDE;25;12345",
        "0;35;UP;INSIDE;980;12345",
        "0;1;DOWN;INSIDE;100;12345",
        "0;2;DOWN;INSIDE;200;12345",
        "4;-1;DOWN;OUTSIDE;400;12345",
      };

These requests simulate the following situation:
1 - Users inside elevator pressed buttons to go floor 3,5 and 50.
2 - Then users from floor 3 want to go floor 4 so they called the elevator to go up.
3 - Then other people inside elevator pressed buttons to stop in floors 2 and 35
4 - Then people from inside elevator want to go down so they pressed buttons to go floor 1 and 2.
5 - Another user waiting in floor 4 (Outside elevator) wants to go down to floor -1

Assumptions:
- The elevator first will handle all UP requests before changing the direction do DOWN (Unless there are no up requests so it starts going down first)
- Elevator will stop in ascending order floors and it will pick users up during the movement only if those users from outside generate request to go in the same direction. If not, they will be picked up when the elevator changes the direction
- If the elevator is stopped and it has requests from going up and down, it will prioritize UP first.
