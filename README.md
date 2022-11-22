# Communication application

This is a simple communication application that allows you to send messages to other users.

| Class  |                                                                                                                                                                                     Description                                                                                                                                                                                     |
| ------ | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| Client | This class is used to handle the message from the server. The message from the server can be direct message or group message. The message will be stored in a HashMap. The key of the HashMap is the sender name or group name. The value of the HashMap is a stack of message. The message will be stored in the stack. The message will be pop out when the user view the message |
| Server |    It is used to handle the message from the client.    |

## How to run the application

At first, we can log in to the server by entering your username and password

`Input username:`

`Input password:`

After that, we can see the list of operation that we can do

1. Send direct message
2. Send group message
3. View direct message
4. View group message
5. Create group
6. Join group
7. Leave group
8. View group member
9. View group list
10. View user list
11. Upload file

## General function

| Option |     Second Header     |
| :----: | :-------------------: |
|   1    | send a direct message |
|   2    |    group function     |
|   3    |    check user list    |
|   4    |  view direct message  |
|   5    |  view group message   |
|   6    | exit the application  |

## Group function

| Option |    Second Header     |
| :----: | :------------------: |
|   1    |    create a group    |
|   2    |     join a group     |
|   3    |    leave a group     |
|   4    | send a group message |
|   5    |   show group list    |

### How to send direct message

`Enter a receiver name:`

`Input message and press ENTER`
