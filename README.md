<h1 align="center" id="title">booking application</h1>

<p id="description">An Airbnb-like application. Utilized the MapReduce framework to process listings and implemented TCP communication for system interactions.</p>

<h2>Project Screenshots:</h2>

<h3>Manager</h3>

<img src="https://github.com/ChristosPekos/booking-application/blob/0b715fcd21dc6f06d955fdc219f9b9f39206d966/booking-images/manager-screenshot-1.png" alt="manager-screenshot" width="800" height="500/">

<img src="https://github.com/ChristosPekos/booking-application/blob/0b715fcd21dc6f06d955fdc219f9b9f39206d966/booking-images/manager-screenshot-2.png" alt="project-screenshot" width="800" height="1000/">

<img src="https://github.com/ChristosPekos/booking-application/blob/0b715fcd21dc6f06d955fdc219f9b9f39206d966/booking-images/manager-screenshot-3.png" alt="project-screenshot" width="800" height="500/">

<h3>User cli</h3>

<img src="https://github.com/ChristosPekos/booking-application/blob/1fa146fdc893d963add3a46c1b07679e60e7499c/booking-images/user-cli-1.png" alt="project-screenshot" width="800" height="1000/">

<img src="https://github.com/ChristosPekos/booking-application/blob/1fa146fdc893d963add3a46c1b07679e60e7499c/booking-images/user-cli-2.png" alt="project-screenshot" width="800" height="1000/">

<h3>User Android</h3>
<img src="https://github.com/ChristosPekos/booking-application/blob/67568449b5c73af87a357421b2a694665cac774e/booking-images/user-android-1.png" alt="project-screenshot" width="500" height = "1000">

<img src="https://github.com/ChristosPekos/booking-application/blob/67568449b5c73af87a357421b2a694665cac774e/booking-images/user-android-2.png" alt="project-screenshot" width="500" height = "1000">

<img src="https://github.com/ChristosPekos/booking-application/blob/67568449b5c73af87a357421b2a694665cac774e/booking-images/user-android-3.png" alt="project-screenshot" width="500" height = "1000">

<img src="https://github.com/ChristosPekos/booking-application/blob/67568449b5c73af87a357421b2a694665cac774e/booking-images/user-android-4.png" alt="project-screenshot" width="500" height = "1000">

<img src="https://github.com/ChristosPekos/booking-application/blob/67568449b5c73af87a357421b2a694665cac774e/booking-images/user-android-5.png" alt="project-screenshot" width="500" height = "1000">


  
  
<h2>🧐 Features</h2>

Manager:
*  Adding Rooms
*  Showing Bookings
*  Showing all the rooms that have been added

User:
*  Book a room
*  Look at rooms
*  Review rooms that you have previously booked


<h2>How it Works</h2>
The system servers (Master, Workers, Reducer) are multithreaded and can handle different requests from users simultaneously. The system utilizes the possibilities provided by the MapReduce framework. When a user makes a request the request is passed down to the workers where each worker handles the task given based on the rooms stored locally.
All the results from the worker computations are sent directly to the Reducer where the reducer merges all the results from every worker.
The reducer then sends the merged results back to the master so the master can show the user his desired results.
The system also supports active replication in case of a worker failure. If a worker fails during runtime, requests that were executed by that worker are sent again to an active Worker in order to handle the request. Workers also hold replica rooms from other workers so they can handle those unexpected requests.

<h2>🛠️ Installation Steps:</h2>

<p>1. In order to run the system in multiple machines all machines must exist inside the same network</p>

<p>2. First and foremost you need to go the config file and change the IP's of every server to the IP of the machine that server is about to run from. To find the IP of the machine you can type "ipconfig" in the local cmd.</p>

<p>3. If the workers are about to run in different machines you dont have to do anything. If they are going to run from the same machine you need to pass a parameter just so its clear to the system which worker is which (You can do that through your IDE by pressing run configuration and adding the number of worker as the parameter (1 for worker 1 2 for worker 2 etc.). You also have to make sure that they run in different ports</p>

<p>4. The system can run the servers in many different combinations. Workers Master and the Reducer can run in different machines. That is up to you</p>

<h2>HOW TO RUN</h2>
irstly build the backend element of the project with your IDE Then before a user can look book or review any views the manager must run first in order to add at least one room. In the rooms folder you can find some rooms that can be added by the manager.
Once the manager has added at least one room the user has the ability to execute every energy on demand. The manager runs exclusively through the command line The user can run through the command line or through the android app. Just make sure that the ipconfig is correctly set in each case.
