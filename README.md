# project-euler-web

This a full-blown Scala web app built around my <a href="https://github.com/msokoryansky2/ProjectEuler">ProjectEuler</a> solutions.

You should be able to start a new IntelliJ project out of it by importing build.sbt file.

Relevant technologies are Scala, Play framework, and Akka on the server. Javascript, jQuery, HTML, CSS on the front-end. Websockets 
used extensively for communication between the front- and back-ends. Docker and AWS ECS and Lambda used for deployment.

It is currently (as of November 2017) <a href="http://ec2-34-204-85-44.compute-1.amazonaws.com:9000/">deployed on AWS ECS</a>, 
though I will probably take it down once my free 12 months on AWS are up :) 

Note that nothing about this web app require the app to actually run on AWS, but AWS ECS 
(docker container manager) is a natural place to deploy such a web app and AWS Lambda provides a bit of extra computational oomph 
to solve the more demanding Project Euler problems.

For a good description of what the web app is all about, run it and click "What Is All This?" link in the upper left. 

Architecturally, the most interesting part of this web app is its use of Play Framework and Akka in conjunction with Websockets to 
easily build a fully reactive "push" notification service to broadcast real-time events from the backend to all the connected clients. 

While there have been several HTTP-based workarounds and even paid services to get the same "push" functionality, Websockets is the 
only truly standard, scalable, and elegant solution to this problem (not to mention one that provides full duplex communication as 
opposed to  mere "pushes"). A non-blocking, reactive backend is required (or at least highly recommended) to take full advantage of 
Websocket capabilities. I found that Play Framework with Akka provide just that.

In theory, one could build an entire single-page style (think Gmail) web app using Websocket communication only, with no form submits
or AJAX calls. Such an app would be architecturally closer to a typical desktop application than to a typical web application. 
While this would be over-engineering today, relevant framework developments may well make such approach practical and beneficial in the 
future. I explored this possibility in this web app and came away impressed with both this general 
approach and with Play Framework's integration with Akka and support for Websockets that make it possible and even easy.
