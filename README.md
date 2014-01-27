# Additional cleanup actions for Eclipse

## Features
- Remove redundant modifiers
 - static on interface declaration
 - public, abstract on methods in interfaces
 - final on private methods
- Remove redundant throws
 - unchecked exceptions declared in `throws` statement
 - Exception class that inherits from another Exception class already declared in `throws` statement
- Can be used both as cleanup action and save action

## Requirements
- Eclipse 4.x (only tested on 4.3.x)
- Java 6

