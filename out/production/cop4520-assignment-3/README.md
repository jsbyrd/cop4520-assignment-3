# cop4520-assignment-3
# Assignment 3 for COP4520
## Problem 1: Birthday Presents
### Compilation Instructions:
1) Ensure you have Java 17 or higher installed
2) Download the code and locate BirthdayPresents.java in your terminal
3) To compile, run the command
   > javac BirthdayPresents.java
4) After compilation, you can execute the code by running
   > java BirthdayPresents
5) If you want to change the number of presents, simply change the variable named `NUM_PRESENTS` that is located towards the start of
   BirthdayPresents.java
### Code Explanation:
My code uses a custom Linked List with fine-grained locking which was inspired from the
LazyList from Chapter 9.7 of the textbook. The servants (threads) will alternate between adding presents to the linked list
and removing from the linked list to create a "thank you" note, with a small random chance of looking to see if a particular gift
is contained in the list (which is printed in stdout). I write all of the "Thank You" messages in a file called presents.txt,
which is created after running the program.

For correctness, I've implemented a counter that counts the number of presents added and the number of presents removed from
the linked list. At the end of the program, they print to stdout and both of these numbers, which will confirm the accuracy of
the program when both numbers (added and removed) are equal to `NUM_PRESENTS`, as it should be.
In addition, I also print the contents of the linked list, and if everything went well, the only two numbers that should print
are the two sentinel nodes (Integer.MIN_VALUE and Integer.MAX_VALUE).

The program is efficient since it uses fine-grained locking, ensure that every thread has access to the linked list at the same
time without causing any problems.

From my experimentation, the program takes on average 200ms when there are 500,000 presents.

## Problem 1: Atmospheric Temperatures
### Compilation Instructions:
1) Ensure you have Java 17 or higher installed
2) Download the code and locate AtmosphericTemperature.java in your terminal
3) To compile, run the command
   > javac AtmosphericTemperature.java
4) After compilation, you can execute the code by running
   > java AtmosphericTemperature
5) If you want to change the number of hours to be recorded, simply change the variable named `NUM_HOURS` that is located towards the start of
   AtmosphericTemperature.java
### Code Explanation:
My code uses a simple counter that creates mutual exclusion for a getAndIncrement method with Java's synchronized keyword. Every thread
gets a minute to analyze, generates a random temperature, and adds it to an array that keeps track of all temperatures. When this process
is finished, the threads die and the program iterates through the temperature records by hour, making note of the top 5 highest
temperatures, top 5 lowest temperatures, and the largest temperature difference in a 10 minute interval in that hour.
These findings are printed to stdout.

For correctness, I've ensured mutual exclusion with Java's synchronized keyword. Actually putting the temperature into the
`temperatureReadings` array does not require mutual exclusion since each thread will have a unique index, so there is no
risk of threads overriding each other.

The program is efficient since it uses multiple threads to collect the temperatures concurrently instead of a single
thread collecting it sequentially.

From my experimentation, the program takes on average 50ms when there it analyzes 100 hours (6000 minutes).
